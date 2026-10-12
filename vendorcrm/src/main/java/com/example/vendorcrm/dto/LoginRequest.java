package com.example.vendorcrm.dto;

import com.example.vendorcrm.entity.Role;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Password is required") String password,
        Role role
) {}