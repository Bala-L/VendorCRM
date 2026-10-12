package com.example.vendorcrm.repository;

import com.example.vendorcrm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Products by vendor
    List<Product> findByVendorId(Long vendorId);

    // Products by category
    List<Product> findByCategory(String category);

    // Product by SKU
    Optional<Product> findBySku(String sku);

    // Products by vendor and category
    List<Product> findByVendorIdAndCategory(
            Long vendorId,
            String category
    );

    // Low stock products
    List<Product> findByStockLessThan(Integer quantity);

    // Active products
    List<Product> findByActiveTrue();

    // Active products scoped by vendor; null vendorId returns all vendors
    @Query("""
        select p
        from Product p
        join fetch p.vendor
        where p.active = true
          and (:vendorId is null or p.vendor.id = :vendorId)
        """)
    List<Product> findActiveScoped(
            @Param("vendorId") Long vendorId
    );

    // Active product categories scoped by vendor
    @Query("""
        select distinct p.category
        from Product p
        where p.active = true
          and (:vendorId is null or p.vendor.id = :vendorId)
        order by p.category
        """)
    List<String> findCategories(
            @Param("vendorId") Long vendorId
    );
}