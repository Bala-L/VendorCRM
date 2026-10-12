package com.example.vendorcrm.service;


import com.example.vendorcrm.entity.Settlement;
import com.example.vendorcrm.repository.SettlementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;

    public SettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    // Create settlement
    public Settlement saveSettlement(Settlement settlement) {

        if (settlement.getSettlementDate() == null) {
            settlement.setSettlementDate(LocalDateTime.now());
        }

        if (settlement.getTotalSales() == null ||
                settlement.getTotalSales().signum() < 0) {
            throw new RuntimeException(
                    "Total sales cannot be negative");
        }

        if (settlement.getTotalCommission() == null ||
                settlement.getTotalCommission().signum() < 0) {
            throw new RuntimeException(
                    "Total commission cannot be negative");
        }

        if (settlement.getSettlementAmount() == null ||
                settlement.getSettlementAmount().signum() < 0) {
            throw new RuntimeException(
                    "Settlement amount cannot be negative");
        }

        return settlementRepository.save(settlement);
    }

    // Get all settlements
    public List<Settlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    // Get settlement by ID
    public Optional<Settlement> getSettlementById(Long id) {
        return settlementRepository.findById(id);
    }

    // Get settlements by vendor
    public List<Settlement> getSettlementsByVendor(Long vendorId) {
        return settlementRepository.findByVendorId(vendorId);
    }

    // Get settlements by status
    public List<Settlement> getSettlementsByStatus(
            String settlementStatus) {

        return settlementRepository
                .findBySettlementStatus(settlementStatus);
    }

    // Get settlements between dates
    public List<Settlement> getSettlementsBetweenDates(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return settlementRepository
                .findBySettlementDateBetween(
                        startDate,
                        endDate);
    }

    // Get vendor settlements between dates
    public List<Settlement> getVendorSettlementsBetweenDates(
            Long vendorId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return settlementRepository
                .findByVendorIdAndSettlementDateBetween(
                        vendorId,
                        startDate,
                        endDate);
    }

    // Get vendor settlements by status
    public List<Settlement> getVendorSettlementsByStatus(
            Long vendorId,
            String settlementStatus) {

        return settlementRepository
                .findByVendorIdAndSettlementStatus(
                        vendorId,
                        settlementStatus);
    }

    // Update settlement
    public Settlement updateSettlement(
            Long id,
            Settlement updatedSettlement) {

        Settlement existingSettlement =
                settlementRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Settlement not found with ID: " + id));

        existingSettlement.setSettlementDate(
                updatedSettlement.getSettlementDate());

        existingSettlement.setTotalSales(
                updatedSettlement.getTotalSales());

        existingSettlement.setTotalCommission(
                updatedSettlement.getTotalCommission());

        existingSettlement.setSettlementAmount(
                updatedSettlement.getSettlementAmount());

        existingSettlement.setSettlementStatus(
                updatedSettlement.getSettlementStatus());

        existingSettlement.setTransactionId(
                updatedSettlement.getTransactionId());

        existingSettlement.setVendor(
                updatedSettlement.getVendor());

        return settlementRepository.save(existingSettlement);
    }

    // Delete settlement
    public void deleteSettlement(Long id) {

        if (!settlementRepository.existsById(id)) {
            throw new RuntimeException(
                    "Settlement not found with ID: " + id);
        }

        settlementRepository.deleteById(id);
    }
}
