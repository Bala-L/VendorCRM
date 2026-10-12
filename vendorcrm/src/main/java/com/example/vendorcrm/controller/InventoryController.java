package com.example.vendorcrm.controller;

import com.example.vendorcrm.entity.Inventory;
import com.example.vendorcrm.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Create inventory
    @PostMapping
    public ResponseEntity<Inventory> createInventory(
            @RequestBody Inventory inventory) {

        return ResponseEntity.ok(
                inventoryService.saveInventory(inventory)
        );
    }

    // Get all inventory
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }

    // Get inventory by ID
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(
            @PathVariable Long id) {

        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get inventory by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProduct(
            @PathVariable Long productId) {

        return inventoryService.getInventoryByProduct(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get inventory by stock status
    @GetMapping("/status/{stockStatus}")
    public ResponseEntity<List<Inventory>> getInventoryByStockStatus(
            @PathVariable String stockStatus) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByStockStatus(stockStatus)
        );
    }

    // Get low stock inventory
    @GetMapping("/low-stock/{quantity}")
    public ResponseEntity<List<Inventory>> getLowStockInventory(
            @PathVariable Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.getLowStockInventory(quantity)
        );
    }

    // Get inventory above minimum level
    @GetMapping("/minimum-level/{quantity}")
    public ResponseEntity<List<Inventory>> getInventoryAboveMinimumLevel(
            @PathVariable Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.getInventoryAboveMinimumLevel(quantity)
        );
    }

    // Update inventory
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable Long id,
            @RequestBody Inventory inventory) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(id, inventory)
        );
    }

    // Delete inventory
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInventory(
            @PathVariable Long id) {

        if (inventoryService.getInventoryById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        inventoryService.deleteInventory(id);

        return ResponseEntity.ok(
                "Inventory deleted successfully"
        );
    }
}