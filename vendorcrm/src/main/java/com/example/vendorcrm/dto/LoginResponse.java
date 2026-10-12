package com.example.vendorcrm.dto;

import com.example.vendorcrm.entity.Role;


public record LoginResponse(
        String token,
        Role role,
        String vendorId,
        String vendorName,
        String email
) {}