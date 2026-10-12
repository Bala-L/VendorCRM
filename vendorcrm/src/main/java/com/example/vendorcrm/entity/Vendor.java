package com.example.vendorcrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Business code, e.g. VND-8942 */
    @Column(nullable = false, unique = true, length = 30)
    private String vendorCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String category;

    /** Marketplace commission % (e.g. 8.00) */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("8.00");

    public Vendor() {
    }

    public Vendor(String vendorCode, String name, String email, String category) {
        this.vendorCode = vendorCode;
        this.name = name;
        this.email = email;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
}