
package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.Order;
import com.example.vendorcrm.entity.OrderStatus;
import com.example.vendorcrm.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Create order
    public Order saveOrder(Order order) {

        if (order.getTotalAmount() == null ||
                order.getTotalAmount().signum() < 0) {

            throw new RuntimeException(
                    "Order amount cannot be negative");
        }

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Get orders by vendor
    public List<Order> getOrdersByVendor(Long vendorId) {
        return orderRepository.findByVendorId(vendorId);
    }

    // Get orders by customer
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // Get orders by status
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    // Get orders by vendor and status
    public List<Order> getOrdersByVendorAndStatus(
            Long vendorId,
            OrderStatus status) {

        return orderRepository.findByVendorIdAndStatus(
                vendorId,
                status);
    }

    // Get orders between dates
    public List<Order> getOrdersBetweenDates(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return orderRepository.findByOrderDateBetween(
                startDate,
                endDate);
    }

    // Get vendor orders between dates
    public List<Order> getVendorOrdersBetweenDates(
            Long vendorId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return orderRepository
                .findByVendorIdAndOrderDateBetween(
                        vendorId,
                        startDate,
                        endDate);
    }

    // Update order
    public Order updateOrder(
            Long id,
            Order updatedOrder) {

        Order existingOrder =
                orderRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found with ID: " + id));

        existingOrder.setOrderDate(
                updatedOrder.getOrderDate());

        existingOrder.setTotalAmount(
                updatedOrder.getTotalAmount());

        existingOrder.setStatus(
                updatedOrder.getStatus());

        existingOrder.setCustomer(
                updatedOrder.getCustomer());

        existingOrder.setVendor(
                updatedOrder.getVendor());

        existingOrder.setTotalCost(
                updatedOrder.getTotalCost());

        existingOrder.setCommissionAmount(
                updatedOrder.getCommissionAmount());

        existingOrder.setSettled(
                updatedOrder.isSettled());

        return orderRepository.save(existingOrder);
    }

    // Delete order
    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new RuntimeException(
                    "Order not found with ID: " + id);
        }

        orderRepository.deleteById(id);
    }
}

