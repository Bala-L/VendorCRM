
package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.Product;
import com.example.vendorcrm.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create product
    public Product saveProduct(Product product) {

        if (product.getPrice() == null ||
                product.getPrice().signum() < 0) {

            throw new RuntimeException(
                    "Product price cannot be negative");
        }

        if (product.getCostPrice() == null ||
                product.getCostPrice().signum() < 0) {

            throw new RuntimeException(
                    "Product cost price cannot be negative");
        }

        if (product.getStock() < 0) {
            throw new RuntimeException(
                    "Stock quantity cannot be negative");
        }

        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Get products by vendor
    public List<Product> getProductsByVendor(Long vendorId) {
        return productRepository.findByVendorId(vendorId);
    }

    // Get products by category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // Get products by vendor and category
    public List<Product> getProductsByVendorAndCategory(
            Long vendorId,
            String category) {

        return productRepository
                .findByVendorIdAndCategory(
                        vendorId,
                        category);
    }

    // Get low stock products
    public List<Product> getLowStockProducts(Integer quantity) {

        return productRepository
                .findByStockLessThan(quantity);
    }

    // Get active products
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    // Get active products by vendor
    public List<Product> getActiveProductsByVendor(Long vendorId) {
        return productRepository.findActiveScoped(vendorId);
    }

    // Get categories
    public List<String> getCategories(Long vendorId) {
        return productRepository.findCategories(vendorId);
    }

    // Update product
    public Product updateProduct(
            Long id,
            Product updatedProduct) {

        Product existingProduct =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found with ID: " + id));

        if (updatedProduct.getPrice() == null ||
                updatedProduct.getPrice().signum() < 0) {

            throw new RuntimeException(
                    "Product price cannot be negative");
        }

        if (updatedProduct.getCostPrice() == null ||
                updatedProduct.getCostPrice().signum() < 0) {

            throw new RuntimeException(
                    "Product cost price cannot be negative");
        }

        if (updatedProduct.getStock() < 0) {
            throw new RuntimeException(
                    "Stock quantity cannot be negative");
        }

        existingProduct.setSku(
                updatedProduct.getSku());

        existingProduct.setName(
                updatedProduct.getName());

        existingProduct.setCategory(
                updatedProduct.getCategory());

        existingProduct.setPrice(
                updatedProduct.getPrice());

        existingProduct.setCostPrice(
                updatedProduct.getCostPrice());

        existingProduct.setStock(
                updatedProduct.getStock());

        existingProduct.setLowStockThreshold(
                updatedProduct.getLowStockThreshold());

        existingProduct.setActive(
                updatedProduct.isActive());

        existingProduct.setVendor(
                updatedProduct.getVendor());

        return productRepository.save(existingProduct);
    }

    // Delete product
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new RuntimeException(
                    "Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
    }
}

