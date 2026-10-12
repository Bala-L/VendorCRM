package com.example.vendorcrm.repository;


import com.example.vendorcrm.entity.Return;
import com.example.vendorcrm.entity.ReturnStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<Return, Long> {

    @Query("select r from Return r join fetch r.customerOrder co join fetch r.product p join fetch r.vendor v " +
           "where r.status in :statuses and (:vendorId is null or v.id = :vendorId) " +
           "order by r.createdAt desc")
    List<Return> findLatest(@Param("statuses") Collection<ReturnStatus> statuses,
                                   @Param("vendorId") Long vendorId,
                                   Pageable pageable);

    @Query("select count(r) from Return r " +
           "where r.status in :statuses and (:vendorId is null or r.vendor.id = :vendorId)")
    long countByStatuses(@Param("statuses") Collection<ReturnStatus> statuses,
                         @Param("vendorId") Long vendorId);
}
