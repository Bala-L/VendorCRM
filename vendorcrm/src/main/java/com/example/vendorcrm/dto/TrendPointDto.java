package com.example.vendorcrm.dto;


import java.math.BigDecimal;

public record TrendPointDto(String label, BigDecimal revenue, BigDecimal profit, long orders) {}
