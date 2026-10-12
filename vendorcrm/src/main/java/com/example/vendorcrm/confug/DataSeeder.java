package com.example.vendorcrm.confug;


import com.example.vendorcrm.entity.Role;
import com.example.vendorcrm.entity.User;
import com.example.vendorcrm.entity.Vendor;
import com.example.vendorcrm.repository.UserRepository;
import com.example.vendorcrm.repository.VendorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DEV ONLY: Creates initial vendor and admin users.
 * Keep app.seed.enabled=false in production.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public DataSeeder(
            VendorRepository vendorRepository,
            UserRepository userRepository,
            PasswordEncoder encoder) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // Avoid creating duplicate seed users.
        if (userRepository.count() > 0) {
            return;
        }

        List<Vendor> vendors = vendorRepository.saveAll(List.of(
                new Vendor(
                        "VND-8942",
                        "Chandran Retailers Ltd",
                        "vendor@chandranretailers.com",
                        "Pet Food & Nutrition"),
                new Vendor(
                        "VND-5120",
                        "BlueCross Animal Pharma",
                        "contact@bluecrosspharma.com",
                        "Veterinary Medicines"),
                new Vendor(
                        "VND-3401",
                        "Zenve Companion Supplies",
                        "sales@zenvecompanion.com",
                        "Pet Beds & Accessories"),
                new Vendor(
                        "VND-7712",
                        "PetVitals Co.",
                        "admin@petvitals.co",
                        "Coat Cleanser & Grooming")
        ));

        for (Vendor vendor : vendors) {
            userRepository.save(new User(
                    vendor.getEmail(),
                    encoder.encode("password123"),
                    Role.VENDOR,
                    vendor
            ));
        }

        userRepository.save(new User(
                "admin@vendorcrm.internal",
                encoder.encode("adminPass2026"),
                Role.ADMIN,
                null
        ));
    }
}
