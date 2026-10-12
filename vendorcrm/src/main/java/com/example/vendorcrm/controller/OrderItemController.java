package com.example.vendorcrm.controller;



import com.example.vendorcrm.entity.OrderItem;
import com.example.vendorcrm.service.OrderItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    // Create order item
    @PostMapping
    public ResponseEntity<OrderItem> createOrderItem(
            @RequestBody OrderItem orderItem) {

        return ResponseEntity.ok(
                orderItemService.saveOrderItem(orderItem)
        );
    }

    // Get all order items
    @GetMapping
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {

        return ResponseEntity.ok(
                orderItemService.getAllOrderItems()
        );
    }

    // Get order item by ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long id) {

        return orderItemService.getOrderItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get items by order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getItemsByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderItemService.getItemsByOrder(orderId)
        );
    }

    // Get items by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderItem>> getItemsByProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                orderItemService.getItemsByProduct(productId)
        );
    }

    // Get item by order and product
    @GetMapping("/order/{orderId}/product/{productId}")
    public ResponseEntity<List<OrderItem>> getItemsByOrderAndProduct(
            @PathVariable Long orderId,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                orderItemService.getItemsByOrderAndProduct(
                        orderId,
                        productId
                )
        );
    }

    // Update order item
    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(
            @PathVariable Long id,
            @RequestBody OrderItem orderItem) {

        return ResponseEntity.ok(
                orderItemService.updateOrderItem(id, orderItem)
        );
    }

    // Delete order item
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderItem(
            @PathVariable Long id) {

        if (orderItemService.getOrderItemById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        orderItemService.deleteOrderItem(id);

        return ResponseEntity.ok(
                "Order item deleted successfully"
        );
    }
}