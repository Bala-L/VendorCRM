package com.example.vendorcrm.repository;

import com.example.vendorcrm.entity.ActivityEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, Long> {

    @Query("select e from ActivityEvent e where (:vendorId is null or e.vendor.id = :vendorId) " +
           "order by e.createdAt desc")
    List<ActivityEvent> findLatest(@Param("vendorId") Long vendorId, Pageable pageable);
}
