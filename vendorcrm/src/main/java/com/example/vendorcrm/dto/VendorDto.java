package com.example.vendorcrm.dto;

import com.example.vendorcrm.entity.Vendor;

public record VendorDto(
        Long id,
        String vendorId,
        String name,
        String email,
        String category
) {
    public static VendorDto from(Vendor v) {
        return new VendorDto(
                v.getId(),
                v.getVendorCode(),
                v.getName(),
                v.getEmail(),
                v.getCategory()
        );
    }
}