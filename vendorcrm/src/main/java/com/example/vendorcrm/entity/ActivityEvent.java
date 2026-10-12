package com.example.vendorcrm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Row for the "Live Operations Feed". Create via ActivityService.record(...) when something happens. */
@Entity
@Table(name = "activity_events", indexes = {
        @Index(name = "idx_activity_created", columnList = "created_at")
})
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ActivityEvent() {}

    public ActivityEvent(Vendor vendor, ActivityType type, String message, LocalDateTime createdAt) {
        this.vendor = vendor;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Vendor getVendor() { return vendor; }
    public ActivityType getType() { return type; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
