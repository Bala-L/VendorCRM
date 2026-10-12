 package com.example.vendorcrm.entity;



 import jakarta.persistence.*;
 import java.math.BigDecimal;
 import java.time.LocalDateTime;

 @Entity
 @Table(name = "commissions")
 public class Commission {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false)
     private BigDecimal commissionRate;

     @Column(nullable = false)
     private BigDecimal commissionAmount;

     @Column(nullable = false)
     private LocalDateTime commissionDate;

     @Column(nullable = false)
     private String commissionStatus;

     @ManyToOne
     @JoinColumn(name = "vendor_id", nullable = false)
     private Vendor vendor;

     @ManyToOne
     @JoinColumn(name = "order_id", nullable = false)
     private Order order;

     public Commission() {
     }

     public Long getId() {
         return id;
     }

     public void setId(Long id) {
         this.id = id;
     }

     public BigDecimal getCommissionRate() {
         return commissionRate;
     }

     public void setCommissionRate(BigDecimal commissionRate) {
         this.commissionRate = commissionRate;
     }

     public BigDecimal getCommissionAmount() {
         return commissionAmount;
     }

     public void setCommissionAmount(BigDecimal commissionAmount) {
         this.commissionAmount = commissionAmount;
     }

     public LocalDateTime getCommissionDate() {
         return commissionDate;
     }

     public void setCommissionDate(LocalDateTime commissionDate) {
         this.commissionDate = commissionDate;
     }

     public String getCommissionStatus() {
         return commissionStatus;
     }

     public void setCommissionStatus(String commissionStatus) {
         this.commissionStatus = commissionStatus;
     }

     public Vendor getVendor() {
         return vendor;
     }

     public void setVendor(Vendor vendor) {
         this.vendor = vendor;
     }

     public Order getOrder() {
         return order;
     }

     public void setOrder(Order order) {
         this.order = order;
     }
 }