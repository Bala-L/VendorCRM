package com.example.vendorcrm.repository;

import com.example.vendorcrm.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByName(String name);
}
