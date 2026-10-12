package com.example.vendorcrm.confug;

import com.example.vendorcrm.entity.*;
import com.example.vendorcrm.repository.CustomerRepository;
import com.example.vendorcrm.repository.OrderRepository;
import com.example.vendorcrm.repository.ProductRepository;
import com.example.vendorcrm.repository.UserRepository;
import com.example.vendorcrm.repository.VendorRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            VendorRepository vendorRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // =====================================================
        // ADMIN
        // =====================================================

        if (!userRepository.existsByEmailIgnoreCase("admin@gmail.com")) {

            User admin = new User();

            admin.setEmail("admin@gmail.com");
            admin.setPasswordHash(
                    passwordEncoder.encode("admin123")
            );
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            System.out.println("Default ADMIN created");
        }


        // =====================================================
        // VENDORS
        // =====================================================

        Vendor vendor1 = createVendor(
                "V001",
                "VetCare Distributors",
                "vendor@gmail.com",
                "Veterinary Medicines",
                "10.00"
        );

        Vendor vendor2 = createVendor(
                "V002",
                "Animal Health Supplies",
                "animalhealth@gmail.com",
                "Animal Medical Supplies",
                "8.00"
        );

        Vendor vendor3 = createVendor(
                "V003",
                "Pet Nutrition Hub",
                "nutrition@gmail.com",
                "Animal Nutrition",
                "7.50"
        );


        // =====================================================
        // VENDOR USERS
        // =====================================================

        if (!userRepository.existsByEmailIgnoreCase("vendor@gmail.com")) {

            User vendorUser = new User();

            vendorUser.setEmail("vendor@gmail.com");
            vendorUser.setPasswordHash(
                    passwordEncoder.encode("vendor123")
            );
            vendorUser.setRole(Role.VENDOR);
            vendorUser.setVendor(vendor1);
            vendorUser.setActive(true);

            userRepository.save(vendorUser);

            System.out.println("Default VENDOR created");
        }


        // =====================================================
        // PRODUCTS
        // =====================================================

        Product p1 = createProduct(
                "VET-001",
                "Amoxicillin Injection",
                "Veterinary Medicines",
                vendor1,
                "450.00",
                "300.00",
                120
        );

        Product p2 = createProduct(
                "VET-002",
                "Deworming Tablets",
                "Veterinary Medicines",
                vendor1,
                "180.00",
                "110.00",
                250
        );

        Product p3 = createProduct(
                "VET-003",
                "Rabies Vaccine",
                "Vaccines",
                vendor1,
                "650.00",
                "480.00",
                80
        );

        Product p4 = createProduct(
                "VET-004",
                "Calcium Supplement",
                "Supplements",
                vendor2,
                "320.00",
                "210.00",
                150
        );

        Product p5 = createProduct(
                "VET-005",
                "Pet Wound Spray",
                "Pet Care",
                vendor2,
                "275.00",
                "170.00",
                65
        );

        Product p6 = createProduct(
                "VET-006",
                "Dog Multivitamin",
                "Supplements",
                vendor3,
                "400.00",
                "260.00",
                12
        );

        Product p7 = createProduct(
                "VET-007",
                "Cat Dewormer",
                "Veterinary Medicines",
                vendor3,
                "220.00",
                "140.00",
                0
        );

        Product p8 = createProduct(
                "VET-008",
                "Surgical Gloves",
                "Medical Supplies",
                vendor2,
                "150.00",
                "90.00",
                300
        );


        // =====================================================
        // CUSTOMERS
        // =====================================================

        Customer c1 = createCustomer(
                "Dr. Arun Veterinary Clinic",
                "arunclinic@gmail.com"
        );

        Customer c2 = createCustomer(
                "Happy Paws Pet Clinic",
                "happypaws@gmail.com"
        );

        Customer c3 = createCustomer(
                "Green Valley Veterinary Hospital",
                "greenvalley@gmail.com"
        );

        Customer c4 = createCustomer(
                "City Animal Care Centre",
                "cityanimalcare@gmail.com"
        );

        Customer c5 = createCustomer(
                "Pet Life Veterinary Clinic",
                "petlife@gmail.com"
        );


        // =====================================================
        // ORDERS
        // =====================================================

        createOrder(
                "ORD-1001",
                vendor1,
                c1,
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(25),
                p1, 3,
                p2, 2
        );

        createOrder(
                "ORD-1002",
                vendor1,
                c2,
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(20),
                p3, 2,
                p2, 4
        );

        createOrder(
                "ORD-1003",
                vendor2,
                c3,
                OrderStatus.PENDING,
                LocalDateTime.now().minusDays(10),
                p4, 5,
                p5, 2
        );

        createOrder(
                "ORD-1004",
                vendor2,
                c4,
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(7),
                p8, 10,
                p5, 3
        );

        createOrder(
                "ORD-1005",
                vendor3,
                c5,
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(5),
                p6, 5
        );

        createOrder(
                "ORD-1006",
                vendor3,
                c2,
                OrderStatus.CANCELLED,
                LocalDateTime.now().minusDays(3),
                p7, 4
        );

        createOrder(
                "ORD-1007",
                vendor1,
                c3,
                OrderStatus.DELIVERED,
                LocalDateTime.now().minusDays(2),
                p1, 2,
                p3, 1
        );

        createOrder(
                "ORD-1008",
                vendor2,
                c1,
                OrderStatus.RETURNED,
                LocalDateTime.now().minusDays(1),
                p5, 2
        );

        System.out.println("Veterinary sample data created successfully!");
    }


    // =========================================================
    // VENDOR
    // =========================================================

    private Vendor createVendor(
            String code,
            String name,
            String email,
            String category,
            String commission) {

        return vendorRepository
                .findByVendorCode(code)
                .orElseGet(() -> {

                    Vendor vendor = new Vendor();

                    vendor.setVendorCode(code);
                    vendor.setName(name);
                    vendor.setEmail(email);
                    vendor.setCategory(category);
                    vendor.setCommissionRate(
                            new BigDecimal(commission)
                    );

                    return vendorRepository.save(vendor);
                });
    }


    // =========================================================
    // PRODUCT
    // =========================================================

    private Product createProduct(
            String sku,
            String name,
            String category,
            Vendor vendor,
            String price,
            String costPrice,
            int stock) {

        return productRepository
                .findBySku(sku)
                .orElseGet(() -> {

                    Product product = new Product();

                    product.setSku(sku);
                    product.setName(name);
                    product.setCategory(category);
                    product.setVendor(vendor);

                    product.setPrice(
                            new BigDecimal(price)
                    );

                    product.setCostPrice(
                            new BigDecimal(costPrice)
                    );

                    product.setStock(stock);
                    product.setLowStockThreshold(15);
                    product.setActive(true);

                    return productRepository.save(product);
                });
    }


    // =========================================================
    // CUSTOMER
    // =========================================================

    private Customer createCustomer(
            String name,
            String email) {

        return customerRepository
                .findByEmailIgnoreCase(email)
                .orElseGet(() -> {

                    Customer customer = new Customer();

                    customer.setName(name);
                    customer.setEmail(email);

                    return customerRepository.save(customer);
                });
    }


    // =========================================================
    // ORDER
    // =========================================================

    private void createOrder(
            String orderNo,
            Vendor vendor,
            Customer customer,
            OrderStatus status,
            LocalDateTime date,

            Product product1,
            int qty1,

            Product product2,
            int qty2) {

        if (orderRepository.existsByOrderNo(orderNo)) {
            return;
        }

        Order order = new Order();

        order.setOrderNo(orderNo);
        order.setVendor(vendor);
        order.setCustomer(customer);
        order.setStatus(status);
        order.setOrderDate(date);

        OrderItem item1 =
                new OrderItem(product1, qty1);

        order.addItem(item1);

        if (product2 != null) {

            OrderItem item2 =
                    new OrderItem(product2, qty2);

            order.addItem(item2);
        }

        order.recalculateTotals();

        if (status == OrderStatus.DELIVERED) {
            order.setSettled(true);
        }

        orderRepository.save(order);
    }


    private void createOrder(
            String orderNo,
            Vendor vendor,
            Customer customer,
            OrderStatus status,
            LocalDateTime date,

            Product product1,
            int qty1) {

        createOrder(
                orderNo,
                vendor,
                customer,
                status,
                date,
                product1,
                qty1,
                null,
                0
        );
    }
}