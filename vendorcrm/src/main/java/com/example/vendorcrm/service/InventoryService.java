package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.Inventory;
import com.example.vendorcrm.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // Create inventory
    public Inventory saveInventory(Inventory inventory) {

        if (inventory.getAvailableQuantity() == null ||
                inventory.getAvailableQuantity() < 0) {

            throw new RuntimeException(
                    "Available quantity cannot be negative");
        }

        if (inventory.getReservedQuantity() == null ||
                inventory.getReservedQuantity() < 0) {

            throw new RuntimeException(
                    "Reserved quantity cannot be negative");
        }

        if (inventory.getMinimumStockLevel() == null ||
                inventory.getMinimumStockLevel() < 0) {

            throw new RuntimeException(
                    "Minimum stock level cannot be negative");
        }

        return inventoryRepository.save(inventory);
    }

    // Get all inventory
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Get inventory by ID
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    // Get inventory by product
    public Optional<Inventory> getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    // Get inventory by stock status
    public List<Inventory> getInventoryByStockStatus(
            String stockStatus) {

        return inventoryRepository.findByStockStatus(stockStatus);
    }

    // Get inventory with available quantity below limit
    public List<Inventory> getLowStockInventory(
            Integer quantity) {

        return inventoryRepository
                .findByAvailableQuantityLessThan(quantity);
    }

    // Get inventory where minimum stock level is greater
    public List<Inventory> getInventoryAboveMinimumLevel(
            Integer quantity) {

        return inventoryRepository
                .findByMinimumStockLevelGreaterThan(quantity);
    }

    // Update inventory
    public Inventory updateInventory(
            Long id,
            Inventory updatedInventory) {

        Inventory existingInventory =
                inventoryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Inventory not found with ID: " + id));

        existingInventory.setAvailableQuantity(
                updatedInventory.getAvailableQuantity());

        existingInventory.setReservedQuantity(
                updatedInventory.getReservedQuantity());

        existingInventory.setMinimumStockLevel(
                updatedInventory.getMinimumStockLevel());

        existingInventory.setStockStatus(
                updatedInventory.getStockStatus());

        existingInventory.setProduct(
                updatedInventory.getProduct());

        return inventoryRepository.save(existingInventory);
    }

    // Delete inventory
    public void deleteInventory(Long id) {

        if (!inventoryRepository.existsById(id)) {
            throw new RuntimeException(
                    "Inventory not found with ID: " + id);
        }

        inventoryRepository.deleteById(id);
    }
}