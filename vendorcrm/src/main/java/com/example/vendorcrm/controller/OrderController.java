
package com.example.vendorcrm.controller;

import com.example.vendorcrm.entity.Order;
import com.example.vendorcrm.entity.OrderStatus;
import com.example.vendorcrm.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create order
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody Order order) {

        return ResponseEntity.ok(
                orderService.saveOrder(order)
        );
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }

    // Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id) {

        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get orders by vendor
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Order>> getOrdersByVendor(
            @PathVariable Long vendorId) {

        return ResponseEntity.ok(
                orderService.getOrdersByVendor(vendorId)
        );
    }

    // Get orders by customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                orderService.getOrdersByCustomer(customerId)
        );
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @PathVariable String status) {

        OrderStatus orderStatus;

        try {
            orderStatus = OrderStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                orderService.getOrdersByStatus(orderStatus)
        );
    }

    // Get orders by vendor and status
    @GetMapping("/vendor/{vendorId}/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByVendorAndStatus(
            @PathVariable Long vendorId,
            @PathVariable String status) {

        OrderStatus orderStatus;

        try {
            orderStatus = OrderStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                orderService.getOrdersByVendorAndStatus(
                        vendorId,
                        orderStatus
                )
        );
    }

    // Get orders between dates
    @GetMapping("/date-range")
    public ResponseEntity<List<Order>> getOrdersBetweenDates(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                orderService.getOrdersBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // Get vendor orders between dates
    @GetMapping("/vendor/{vendorId}/date-range")
    public ResponseEntity<List<Order>> getVendorOrdersBetweenDates(
            @PathVariable Long vendorId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                orderService.getVendorOrdersBetweenDates(
                        vendorId,
                        startDate,
                        endDate
                )
        );
    }

    // Update order
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @RequestBody Order order) {

        if (orderService.getOrderById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                orderService.updateOrder(id, order)
        );
    }

    // Delete order
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(
            @PathVariable Long id) {

        if (orderService.getOrderById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        orderService.deleteOrder(id);

        return ResponseEntity.ok(
                "Order deleted successfully"
        );
    }	
    
 // Cancel order
 @PutMapping("/{id}/cancel")
 public ResponseEntity<?> cancelOrder(
         @PathVariable Long id) {

     var orderOptional = orderService.getOrderById(id);

     if (orderOptional.isEmpty()) {
         return ResponseEntity.notFound().build();
     }

     Order order = orderOptional.get();

     order.setStatus(OrderStatus.CANCELLED);

     Order updatedOrder = orderService.updateOrder(id, order);

     return ResponseEntity.ok(updatedOrder);
 }
 

}

