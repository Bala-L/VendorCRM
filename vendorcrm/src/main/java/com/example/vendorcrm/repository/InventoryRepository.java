package com.example.vendorcrm.repository;



import com.example.vendorcrm.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findByStockStatus(String stockStatus);

    List<Inventory> findByAvailableQuantityLessThan(
            Integer quantity
    );

    List<Inventory> findByMinimumStockLevelGreaterThan(
            Integer quantity
    );
}