package com.example.vendorcrm.controller;

import com.example.vendorcrm.dto.VendorDto;
import com.example.vendorcrm.service.VendorService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /** VENDOR role: own profile */
    @GetMapping("/api/vendors/me")
    public VendorDto me(Authentication auth) {
        return vendorService.findByUserEmail(auth.getName());
    }

    /** ADMIN role: all vendors */
    @GetMapping("/api/admin/vendors")
    public List<VendorDto> all() {
        return vendorService.findAll();
    }
}
