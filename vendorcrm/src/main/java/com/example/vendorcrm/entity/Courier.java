package com.example.vendorcrm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "couriers")
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Promised pickup-to-delivery time; used for on-time delivery rate */
    @Column(nullable = false)
    private Integer slaTransitHours;

    public Courier() {}

    public Courier(String name, Integer slaTransitHours) {
        this.name = name;
        this.slaTransitHours = slaTransitHours;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSlaTransitHours() { return slaTransitHours; }
    public void setSlaTransitHours(Integer slaTransitHours) { this.slaTransitHours = slaTransitHours; }
}
