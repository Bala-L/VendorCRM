package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.ActivityEvent;
import com.example.vendorcrm.entity.ActivityType;
import com.example.vendorcrm.entity.Vendor;
import com.example.vendorcrm.repository.ActivityEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Call record(...) from your order / dispatch / payout / return / stock flows
 * so the "Live Operations Feed" shows real events.
 */
@Service
public class ActivityService {

    private final ActivityEventRepository repository;
    private final ZoneId zone;

    public ActivityService(ActivityEventRepository repository,
                           @Value("${app.timezone:Asia/Kolkata}") String timezone) {
        this.repository = repository;
        this.zone = ZoneId.of(timezone);
    }

    @Transactional
    public void record(Vendor vendor, ActivityType type, String message) {
        repository.save(new ActivityEvent(vendor, type, message, LocalDateTime.now(zone)));
    }
}
