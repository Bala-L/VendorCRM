package com.example.vendorcrm.repository;

import com.example.vendorcrm.dto.OperationalRow;
import com.example.vendorcrm.dto.OrderRow;
import com.example.vendorcrm.entity.Order;
import com.example.vendorcrm.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ---------------- Basic queries ----------------

    List<Order> findByVendorId(Long vendorId);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByVendorIdAndStatus(
            Long vendorId,
            OrderStatus status
    );

    List<Order> findByOrderDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Order> findByVendorIdAndOrderDateBetween(
            Long vendorId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    boolean existsByOrderNo(String orderNo);

  
    @Query("""
        select new com.example.vendorcrm.dto.OrderRow(
            o.status,
            o.orderDate,
            o.totalAmount,
            o.totalCost,
            o.commissionAmount,
            o.customer.id
        )
        from Order o
        where o.orderDate >= :fromDate
          and o.orderDate < :toDate
          and (:vendorId is null or o.vendor.id = :vendorId)
        """)
    List<OrderRow> findRows(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("vendorId") Long vendorId
    );

  
    @Query("""
        select sum(o.totalAmount - o.commissionAmount)
        from Order o
        where o.status = com.example.vendorcrm.entity.OrderStatus.DELIVERED
          and o.settled = false
          and (:vendorId is null or o.vendor.id = :vendorId)
        """)
    BigDecimal pendingSettlement(
            @Param("vendorId") Long vendorId
    );


    @Query("""
        select new com.example.vendorcrm.dto.OperationalRow(
            o.status,
            o.orderDate,
            o.slaDueAt,
            o.dispatchedAt,
            o.deliveredAt,
            c.name,
            c.slaTransitHours,
            o.totalAmount,
            o.commissionAmount
        )
        from Order o
        left join o.courier c
        where o.orderDate >= :fromDate
          and o.orderDate < :toDate
          and (:vendorId is null or o.vendor.id = :vendorId)
        """)
    List<OperationalRow> findOperationalRows(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("vendorId") Long vendorId
    );

   
    @Query("""
        select new com.example.vendorcrm.dto.OperationalRow(
            o.status,
            o.orderDate,
            o.slaDueAt,
            o.dispatchedAt,
            o.deliveredAt,
            c.name,
            c.slaTransitHours,
            o.totalAmount,
            o.commissionAmount
        )
        from Order o
        left join o.courier c
        where o.status in :statuses
          and (:vendorId is null or o.vendor.id = :vendorId)
        """)
    List<OperationalRow> findOperationalRowsByStatus(
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("vendorId") Long vendorId
    );

   
    @Query("""
        select new com.example.vendorcrm.dto.OperationalRow(
            o.status,
            o.orderDate,
            o.slaDueAt,
            o.dispatchedAt,
            o.deliveredAt,
            c.name,
            c.slaTransitHours,
            o.totalAmount,
            o.commissionAmount
        )
        from Order o
        left join o.courier c
        where o.status = com.example.vendorcrm.entity.OrderStatus.DELIVERED
          and o.deliveredAt >= :fromDate
          and o.deliveredAt < :toDate
          and (:vendorId is null or o.vendor.id = :vendorId)
        """)
    List<OperationalRow> findDeliveredRows(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("vendorId") Long vendorId
    );

    @Query("""
        select count(o)
        from Order o
        where o.orderDate >= :since
          and o.status <> com.example.vendorcrm.entity.OrderStatus.CANCELLED
          and (:vendorId is null or o.vendor.id = :vendorId)
        """)
    long countPlacedSince(
            @Param("since") LocalDateTime since,
            @Param("vendorId") Long vendorId
    );

  
    @Query("""
        select o
        from Order o
        join fetch o.vendor v
        join fetch o.customer cu
        left join fetch o.courier c
        where (
            (o.orderDate >= :fromDate and o.orderDate < :toDate)
            or o.status in :openStatuses
        )
          and o.status in :statuses
          and (:vendorId is null or v.id = :vendorId)
          and (
              lower(o.orderNo) like :q
              or lower(cu.name) like :q
              or lower(c.name) like :q
              or lower(o.awbNumber) like :q
              or exists (
                  select 1
                  from OrderItem i
                  where i.customerOrder = o
                    and (
                        lower(i.product.sku) like :q
                        or lower(i.product.name) like :q
                    )
              )
          )
        order by o.orderDate desc
        """)
    List<Order> findForTriage(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("openStatuses") Collection<OrderStatus> openStatuses,
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("vendorId") Long vendorId,
            @Param("q") String q,
            Pageable pageable
    );
}
