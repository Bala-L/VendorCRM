package com.example.vendorcrm.service;


import com.example.vendorcrm.entity.Return;
import com.example.vendorcrm.entity.ReturnStatus;
import com.example.vendorcrm.repository.ReturnRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReturnService {

    private final ReturnRepository returnRepository;

    public ReturnService(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    // Create return
    public Return saveReturn(Return returnEntity) {

        if (returnEntity.getCreatedAt() == null) {
            returnEntity.setCreatedAt(LocalDateTime.now());
        }

        return returnRepository.save(returnEntity);
    }

    // Get all returns
    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }

    // Get return by ID
    public Optional<Return> getReturnById(Long id) {
        return returnRepository.findById(id);
    }

    // Get return by order
    public Optional<Return> getReturnByOrder(Long orderId) {
        return returnRepository.findByCustomerOrderId(orderId);
    }

    // Get returns by status
    public List<Return> getReturnsByStatus(String returnStatus) {
        return returnRepository.findByStatus(parseStatus(returnStatus));
    }

    // Get returns by refund status
    public List<Return> getReturnsByRefundStatus(String refundStatus) {
        return returnRepository.findByRefundStatus(refundStatus);
    }

    // Get returns between dates
    public List<Return> getReturnsBetweenDates(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return returnRepository.findByCreatedAtBetween(
                startDate,
                endDate);
    }

    // Get returns by status and date
    public List<Return> getReturnsByStatusAndDate(
            String returnStatus,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return returnRepository
                .findByStatusAndCreatedAtBetween(
                        parseStatus(returnStatus),
                        startDate,
                        endDate);
    }

    // Update return
    public Return updateReturn(
            Long id,
            Return updatedReturn) {

        Return existingReturn =
                returnRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Return not found with ID: " + id));

        if (updatedReturn.getReason() != null) {
            existingReturn.setReason(updatedReturn.getReason());
        }

        if (updatedReturn.getStatus() != null) {
            existingReturn.setStatus(updatedReturn.getStatus());
        }

        existingReturn.setRefundStatus(
                updatedReturn.getRefundStatus());

        return returnRepository.save(existingReturn);
    }

    // Delete return
    public void deleteReturn(Long id) {

        if (!returnRepository.existsById(id)) {
            throw new RuntimeException(
                    "Return not found with ID: " + id);
        }

        returnRepository.deleteById(id);
    }

    private ReturnStatus parseStatus(String status) {
        try {
            return ReturnStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Invalid return status: " + status, ex);
        }
    }
}
