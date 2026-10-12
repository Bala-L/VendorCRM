package com.example.vendorcrm.repository;

import com.example.vendorcrm.entity.OrderItem;
import com.example.vendorcrm.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Get items by order
    List<OrderItem> findByCustomerOrderId(Long orderId);

    // Get items by product
    List<OrderItem> findByProductId(Long productId);

    // Get items by order and product
    List<OrderItem> findByCustomerOrderIdAndProductId(
            Long orderId,
            Long productId
    );

    /**
     * Row = [productId, unitsSold, revenue]
     * excluding the given order statuses.
     */
    @Query("""
        select i.product.id,
               sum(i.quantity),
               sum(i.unitPrice * i.quantity)
        from OrderItem i
        where i.customerOrder.orderDate >= :fromDate
          and i.customerOrder.orderDate < :toDate
          and i.customerOrder.status not in :excluded
          and (:vendorId is null
               or i.customerOrder.vendor.id = :vendorId)
        group by i.product.id
        """)
    List<Object[]> salesByProduct(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("vendorId") Long vendorId,
            @Param("excluded") Collection<OrderStatus> excluded
    );

    /**
     * Count distinct orders in the given statuses
     * that contain any of the specified products.
     */
    @Query("""
        select count(distinct i.customerOrder.id)
        from OrderItem i
        where i.customerOrder.status in :statuses
          and i.product.id in :productIds
        """)
    long countOrdersContainingProducts(
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("productIds") Collection<Long> productIds
    );
}