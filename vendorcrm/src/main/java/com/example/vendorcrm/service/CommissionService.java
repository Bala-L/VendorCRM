package com.example.vendorcrm.service;


import com.example.vendorcrm.entity.Commission;
import com.example.vendorcrm.repository.CommissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommissionService {

    private final CommissionRepository commissionRepository;

    public CommissionService(CommissionRepository commissionRepository) {
        this.commissionRepository = commissionRepository;
    }

    // Create commission
    public Commission saveCommission(Commission commission) {

        if (commission.getCommissionRate() == null ||
                commission.getCommissionRate().signum() < 0) {

            throw new RuntimeException(
                    "Commission rate cannot be negative");
        }

        if (commission.getCommissionAmount() == null ||
                commission.getCommissionAmount().signum() < 0) {

            throw new RuntimeException(
                    "Commission amount cannot be negative");
        }

        if (commission.getCommissionDate() == null) {
            commission.setCommissionDate(LocalDateTime.now());
        }

        return commissionRepository.save(commission);
    }

    // Get all commissions
    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll();
    }

    // Get commission by ID
    public Optional<Commission> getCommissionById(Long id) {
        return commissionRepository.findById(id);
    }

    // Get commissions by vendor
    public List<Commission> getCommissionsByVendor(Long vendorId) {
        return commissionRepository.findByVendorId(vendorId);
    }

    // Get commissions by order
    public List<Commission> getCommissionsByOrder(Long orderId) {
        return commissionRepository.findByOrderId(orderId);
    }

    // Get commissions by status
    public List<Commission> getCommissionsByStatus(
            String commissionStatus) {

        return commissionRepository
                .findByCommissionStatus(commissionStatus);
    }

    // Get commissions between dates
    public List<Commission> getCommissionsBetweenDates(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return commissionRepository
                .findByCommissionDateBetween(
                        startDate,
                        endDate);
    }

    // Get vendor commissions between dates
    public List<Commission> getVendorCommissionsBetweenDates(
            Long vendorId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return commissionRepository
                .findByVendorIdAndCommissionDateBetween(
                        vendorId,
                        startDate,
                        endDate);
    }

    // Get vendor commissions by status
    public List<Commission> getVendorCommissionsByStatus(
            Long vendorId,
            String commissionStatus) {

        return commissionRepository
                .findByVendorIdAndCommissionStatus(
                        vendorId,
                        commissionStatus);
    }

    // Update commission
    public Commission updateCommission(
            Long id,
            Commission updatedCommission) {

        Commission existingCommission =
                commissionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Commission not found with ID: " + id));

        existingCommission.setCommissionRate(
                updatedCommission.getCommissionRate());

        existingCommission.setCommissionAmount(
                updatedCommission.getCommissionAmount());

        existingCommission.setCommissionDate(
                updatedCommission.getCommissionDate());

        existingCommission.setCommissionStatus(
                updatedCommission.getCommissionStatus());

        existingCommission.setVendor(
                updatedCommission.getVendor());

        existingCommission.setOrder(
                updatedCommission.getOrder());

        return commissionRepository.save(existingCommission);
    }

    // Delete commission
    public void deleteCommission(Long id) {

        if (!commissionRepository.existsById(id)) {
            throw new RuntimeException(
                    "Commission not found with ID: " + id);
        }

        commissionRepository.deleteById(id);
    }
}