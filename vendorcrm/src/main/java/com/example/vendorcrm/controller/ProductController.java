package com.example.vendorcrm.controller;

import com.example.vendorcrm.entity.Product;
import com.example.vendorcrm.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Create product
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        return ResponseEntity.ok(
                productService.saveProduct(product)
        );
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get products by vendor
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Product>> getProductsByVendor(
            @PathVariable Long vendorId) {

        return ResponseEntity.ok(
                productService.getProductsByVendor(vendorId)
        );
    }

    // Get products by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(category)
        );
    }

    // Get products by vendor and category
    @GetMapping("/vendor/{vendorId}/category/{category}")
    public ResponseEntity<List<Product>> getProductsByVendorAndCategory(
            @PathVariable Long vendorId,
            @PathVariable String category) {

        return ResponseEntity.ok(
                productService.getProductsByVendorAndCategory(
                        vendorId,
                        category
                )
        );
    }

    // Get low stock products
    @GetMapping("/low-stock/{quantity}")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @PathVariable Integer quantity) {

        return ResponseEntity.ok(
                productService.getLowStockProducts(quantity)
        );
    }

    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        if (productService.getProductById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                productService.updateProduct(id, product)
        );
    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long id) {

        if (productService.getProductById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                "Product deleted successfully"
        );
    }
}