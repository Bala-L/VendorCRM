package com.example.vendorcrm.controller;

import com.example.vendorcrm.dto.DashboardResponse;
import com.example.vendorcrm.dto.FilterOptionsResponse;
import com.example.vendorcrm.dto.PageResponse;
import com.example.vendorcrm.dto.ProductAnalyticsDto;
import com.example.vendorcrm.service.AnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accessible by VENDOR and ADMIN (any authenticated user).
 * Vendor scoping is enforced server-side in AnalyticsService.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(
            Authentication auth,
            @RequestParam(defaultValue = "30d") String range,
            @RequestParam(required = false) String vendorId) {
        return analyticsService.dashboard(auth, range, vendorId);
    }

    @GetMapping("/products")
    public PageResponse<ProductAnalyticsDto> products(
            Authentication auth,
            @RequestParam(defaultValue = "30d") String range,
            @RequestParam(required = false) String vendorId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "revenue") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return analyticsService.products(
                auth, range, vendorId, category, stockStatus, search,
                page, size, sortBy, sortDir);
    }

    @GetMapping("/filters")
    public FilterOptionsResponse filters(Authentication auth) {
        return analyticsService.filters(auth);
    }
}
