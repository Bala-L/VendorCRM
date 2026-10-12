package com.example.vendorcrm.dto;




import java.util.List;

/** vendors is empty for VENDOR role */
public record FilterOptionsResponse(List<String> categories, List<VendorDto> vendors) {}
