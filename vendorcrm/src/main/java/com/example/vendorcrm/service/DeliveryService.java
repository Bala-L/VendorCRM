package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.Delivery;
import com.example.vendorcrm.repository.DeliveryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    // Create delivery
    public Delivery saveDelivery(Delivery delivery) {

        if (delivery.getDeliveryDate() == null) {
            delivery.setDeliveryDate(LocalDateTime.now());
        }

        return deliveryRepository.save(delivery);
    }

    // Get all deliveries
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    // Get delivery by ID
    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    // Get delivery by order
    public Optional<Delivery> getDeliveryByOrder(Long orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }

    // Get delivery by tracking number
    public Optional<Delivery> getDeliveryByTrackingNumber(
            String trackingNumber) {

        return deliveryRepository
                .findByTrackingNumber(trackingNumber);
    }

    // Get deliveries by status
    public List<Delivery> getDeliveriesByStatus(
            String deliveryStatus) {

        return deliveryRepository
                .findByDeliveryStatus(deliveryStatus);
    }

    // Get deliveries between dates
    public List<Delivery> getDeliveriesBetweenDates(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return deliveryRepository.findByDeliveryDateBetween(
                startDate,
                endDate);
    }

    // Get deliveries by status and date
    public List<Delivery> getDeliveriesByStatusAndDate(
            String deliveryStatus,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return deliveryRepository
                .findByDeliveryStatusAndDeliveryDateBetween(
                        deliveryStatus,
                        startDate,
                        endDate);
    }

    // Update delivery
    public Delivery updateDelivery(
            Long id,
            Delivery updatedDelivery) {

        Delivery existingDelivery =
                deliveryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery not found with ID: " + id));

        existingDelivery.setDeliveryDate(
                updatedDelivery.getDeliveryDate());

        existingDelivery.setDeliveryStatus(
                updatedDelivery.getDeliveryStatus());

        existingDelivery.setTrackingNumber(
                updatedDelivery.getTrackingNumber());

        existingDelivery.setDeliveryAddress(
                updatedDelivery.getDeliveryAddress());

        existingDelivery.setOrder(
                updatedDelivery.getOrder());

        return deliveryRepository.save(existingDelivery);
    }

    // Delete delivery
    public void deleteDelivery(Long id) {

        if (!deliveryRepository.existsById(id)) {
            throw new RuntimeException(
                    "Delivery not found with ID: " + id);
        }

        deliveryRepository.deleteById(id);
    }
}