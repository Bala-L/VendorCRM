package com.example.vendorcrm.service;

import com.example.vendorcrm.dto.LoginRequest;
import com.example.vendorcrm.dto.LoginResponse;

import com.example.vendorcrm.entity.Role;
import com.example.vendorcrm.entity.User;
import com.example.vendorcrm.repository.UserRepository;
import com.example.vendorcrm.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        // Use a generic message to avoid revealing whether an email exists.
        BadCredentialsException invalidCredentials =
                new BadCredentialsException("Invalid email or password.");

        User user = userRepository
                .findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> invalidCredentials);

        if (!user.isActive()
                || !passwordEncoder.matches(
                        req.password(),
                        user.getPasswordHash()
                )) {
            throw invalidCredentials;
        }

        if (req.role() != null && req.role() != user.getRole()) {
            throw invalidCredentials;
        }

        String vendorId = null;
        String displayName = "Super Admin Operations";

        if (user.getRole() == Role.VENDOR) {
            if (user.getVendor() == null) {
                throw invalidCredentials;
            }

            vendorId = user.getVendor().getVendorCode();
            displayName = user.getVendor().getName();
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getRole(),
                vendorId,
                displayName,
                user.getEmail()
        );
    }
}