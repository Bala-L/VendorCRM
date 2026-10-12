
package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.OrderItem;
import com.example.vendorcrm.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    // Create order item
    public OrderItem saveOrderItem(OrderItem orderItem) {

        if (orderItem.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        if (orderItem.getUnitPrice() == null ||
                orderItem.getUnitPrice().signum() < 0) {
            throw new RuntimeException(
                    "Unit price cannot be negative");
        }

        return orderItemRepository.save(orderItem);
    }

    // Get all order items
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    // Get order item by ID
    public Optional<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    // Get items by order
    public List<OrderItem> getItemsByOrder(Long orderId) {
        return orderItemRepository.findByCustomerOrderId(orderId);
    }

    // Get items by product
    public List<OrderItem> getItemsByProduct(Long productId) {
        return orderItemRepository.findByProductId(productId);
    }

    // Get item by order and product
    public List<OrderItem> getItemsByOrderAndProduct(
            Long orderId,
            Long productId) {

        return orderItemRepository
                .findByCustomerOrderIdAndProductId(
                        orderId,
                        productId);
    }

    // Update order item
    public OrderItem updateOrderItem(
            Long id,
            OrderItem updatedItem) {

        OrderItem existingItem =
                orderItemRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order item not found with ID: " + id));

        if (updatedItem.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }

        if (updatedItem.getUnitPrice() == null ||
                updatedItem.getUnitPrice().signum() < 0) {
            throw new RuntimeException(
                    "Unit price cannot be negative");
        }

        existingItem.setQuantity(
                updatedItem.getQuantity());

        existingItem.setUnitPrice(
                updatedItem.getUnitPrice());

        existingItem.setUnitCost(
                updatedItem.getUnitCost());

        existingItem.setCustomerOrder(
                updatedItem.getCustomerOrder());

        existingItem.setProduct(
                updatedItem.getProduct());

        return orderItemRepository.save(existingItem);
    }

    // Delete order item
    public void deleteOrderItem(Long id) {

        if (!orderItemRepository.existsById(id)) {
            throw new RuntimeException(
                    "Order item not found with ID: " + id);
        }

        orderItemRepository.deleteById(id);
    }
}

