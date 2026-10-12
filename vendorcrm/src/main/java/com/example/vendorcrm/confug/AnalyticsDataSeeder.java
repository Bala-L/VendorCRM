package com.example.vendorcrm.confug;

import com.example.vendorcrm.entity.*;
import com.example.vendorcrm.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/** DEV ONLY: sample products, customers, orders, returns and activity so the dashboards have data. */
@Component
@org.springframework.core.annotation.Order(2)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AnalyticsDataSeeder implements CommandLineRunner {

    private static final int SLA_HOURS = 4; // dispatch deadline after order placement
    private static final String[] RETURN_REASONS = {
            "Damaged Package Seal", "Wrong Size Selected", "Leakage on Delivery",
            "Expired Bag Dispatched", "Product Not as Described", "Received Wrong Item"
    };

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final ReturnRequestRepository returnRepository;
    private final ActivityEventRepository activityRepository;
    private final ZoneId zone;

    public AnalyticsDataSeeder(VendorRepository vendorRepository, ProductRepository productRepository,
                               CustomerRepository customerRepository, OrderRepository orderRepository,
                               CourierRepository courierRepository, ReturnRequestRepository returnRepository,
                               ActivityEventRepository activityRepository,
                               @Value("${app.timezone:Asia/Kolkata}") String timezone) {
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.returnRepository = returnRepository;
        this.activityRepository = activityRepository;
        this.zone = ZoneId.of(timezone);
    }

    private Vendor vendor(String code) {
        return vendorRepository.findByVendorCode(code)
                .orElseThrow(() -> new IllegalStateException("Seed vendor missing: " + code));
    }

    private Product product(String sku, String name, String cat, Vendor v, String price, String cost, int stock) {
        return new Product(sku, name, cat, v, new BigDecimal(price), new BigDecimal(cost), stock);
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        Vendor chandran = vendor("VND-8942");
        Vendor bluecross = vendor("VND-5120");
        Vendor zenve = vendor("VND-3401");
        Vendor petvitals = vendor("VND-7712");

        Product whiskas = product("PRD-03", "Whiskas Ocean Fish Adult Cat Food (7kg)", "Pet Food", chandran, "220", "165", 12);
        whiskas.setLowStockThreshold(30);

        List<Product> products = productRepository.saveAll(List.of(
                product("PRD-01", "Royal Canin Maxi Adult Dog Food (15kg)", "Pet Food", chandran, "350", "270", 45),
                product("PRD-02", "Bravecto Chewable Tick & Flea Tablet", "Medicines", bluecross, "250", "170", 3),
                whiskas,
                product("PRD-04", "Pet Care Orthopedic Memory Foam Bed", "Accessories", zenve, "250", "160", 0),
                product("PRD-05", "Himalaya Erina Plus Coat Cleanser Shampoo", "Grooming", petvitals, "200", "130", 64),
                product("PRD-06", "Pedigree Dentastix Daily Dental Chews", "Pet Food", chandran, "150", "105", 30),
                product("PRD-07", "Heads Up For Tails Rope Toy", "Accessories", zenve, "120", "70", 150),
                product("PRD-08", "NexGard Spectra Chewables", "Medicines", bluecross, "900", "700", 40)
        ));

        List<Courier> couriers = courierRepository.saveAll(List.of(
                new Courier("Delhivery Express", 48),
                new Courier("BlueDart Aviation", 36),
                new Courier("Shadowfax Local", 48)
        ));

        List<Customer> customers = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            customers.add(new Customer("Customer " + i, "customer" + i + "@example.com"));
        }
        customers = customerRepository.saveAll(customers);

        Map<Long, List<Product>> byVendor = new HashMap<>();
        for (Product p : products) {
            byVendor.computeIfAbsent(p.getVendor().getId(), k -> new ArrayList<>()).add(p);
        }

        Random rnd = new Random(42);
        LocalDateTime now = LocalDateTime.now(zone);
        List<Order> orders = new ArrayList<>();

        for (int i = 0; i < 600; i++) {
            Product first = products.get(rnd.nextInt(products.size()));
            Vendor v = first.getVendor();
            int daysAgo = (int) (200 * Math.pow(rnd.nextDouble(), 1.6)); // recent-heavy
            LocalDateTime date = now.minusDays(daysAgo).minusMinutes(rnd.nextInt(1440));
            if (date.isAfter(now)) date = now.minusMinutes(5);
            long ageMin = Duration.between(date, now).toMinutes();
            OrderStatus status = pickStatus(rnd, ageMin);

            Order o = new Order(String.format("ORD-%05d", i + 1), v,
                    customers.get(rnd.nextInt(customers.size())), status, date);

            int courierIdx = rnd.nextInt(couriers.size());
            o.setCourier(couriers.get(courierIdx));
            o.setSlaDueAt(date.plusHours(SLA_HOURS));

            boolean shipped = status == OrderStatus.IN_TRANSIT || status == OrderStatus.DELIVERED
                    || status == OrderStatus.RETURNED;
            if (shipped) {
                long delay = rnd.nextDouble() < 0.92 ? 30 + rnd.nextInt(200) : 250 + rnd.nextInt(120);
                LocalDateTime dispatched = date.plusMinutes(delay);
                if (dispatched.isAfter(now)) dispatched = now.minusMinutes(1 + rnd.nextInt(10));
                if (!dispatched.isAfter(date)) dispatched = date.plusMinutes(1);
                o.setDispatchedAt(dispatched);
                o.setAwbNumber("AWB" + (10000000 + rnd.nextInt(89999999)));

                if (status != OrderStatus.IN_TRANSIT) {
                    LocalDateTime deliveredAt = dispatched.plusMinutes(transitMinutes(rnd, courierIdx));
                    if (deliveredAt.isAfter(now)) deliveredAt = now.minusMinutes(1 + rnd.nextInt(30));
                    if (!deliveredAt.isAfter(dispatched)) deliveredAt = dispatched.plusMinutes(1);
                    o.setDeliveredAt(deliveredAt);
                }
            }

            List<Product> pool = new ArrayList<>(byVendor.get(v.getId()));
            Collections.shuffle(pool, rnd);
            int itemCount = Math.min(pool.size(), 1 + rnd.nextInt(3));
            o.addItem(new OrderItem(first, 1 + rnd.nextInt(4)));
            for (Product extra : pool) {
                if (o.getItems().size() >= itemCount) break;
                if (extra.getId().equals(first.getId())) continue;
                o.addItem(new OrderItem(extra, 1 + rnd.nextInt(3)));
            }
            o.recalculateTotals();
            o.setSettled(status == OrderStatus.DELIVERED && o.getDeliveredAt() != null
                    && o.getDeliveredAt().isBefore(now.minusDays(7)));
            orders.add(o);
        }
        List<Order> saved = orderRepository.saveAll(orders);

        // ---------------- returns ----------------
        List<Return> returns = new ArrayList<>();
        int n = 1;
        for (Order o : saved) {
            boolean returned = o.getStatus() == OrderStatus.RETURNED;
            boolean recentDelivered = o.getStatus() == OrderStatus.DELIVERED && o.getDeliveredAt() != null
                    && o.getDeliveredAt().isAfter(now.minusDays(10)) && rnd.nextDouble() < 0.12;
            if (!returned && !recentDelivered) continue;

            ReturnStatus rs = returned ? ReturnStatus.COMPLETED : pickReturnStatus(rnd);
            LocalDateTime base = o.getDeliveredAt() != null ? o.getDeliveredAt() : o.getOrderDate();
            LocalDateTime created = base.plusHours(2 + rnd.nextInt(24));
            if (created.isAfter(now)) created = now.minusMinutes(5 + rnd.nextInt(120));
            Product item = o.getItems().get(0).getProduct();
            returns.add(new Return(String.format("RET-%04d", 1000 + n++), o, item, o.getVendor(),
                    RETURN_REASONS[rnd.nextInt(RETURN_REASONS.length)], rs, created));
        }
        returnRepository.saveAll(returns);

        // ---------------- activity feed ----------------
        List<ActivityEvent> events = new ArrayList<>();

        saved.stream().sorted(Comparator.comparing(Order::getOrderDate).reversed()).limit(8)
                .forEach(o -> events.add(new ActivityEvent(o.getVendor(), ActivityType.ORDER_PLACED,
                        String.format("New Order #%s placed by %s for %s", o.getOrderNo(),
                                o.getCustomer().getName(), o.getItems().get(0).getProduct().getName()),
                        o.getOrderDate())));

        saved.stream().filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT && o.getDispatchedAt() != null)
                .sorted(Comparator.comparing(Order::getDispatchedAt).reversed()).limit(4)
                .forEach(o -> events.add(new ActivityEvent(o.getVendor(), ActivityType.SHIPMENT_UPDATE,
                        String.format("Shipment #%s marked Out for Delivery via %s", o.getAwbNumber(),
                                o.getCourier().getName()),
                        o.getDispatchedAt())));

        int idx = 0;
        for (Vendor v : List.of(chandran, bluecross, zenve, petvitals)) {
            BigDecimal net = BigDecimal.ZERO;
            for (Order o : saved) {
                if (o.getVendor().getId().equals(v.getId()) && o.isSettled()
                        && o.getDeliveredAt().isAfter(now.minusDays(14))) {
                    net = net.add(o.getTotalAmount().subtract(o.getCommissionAmount()));
                }
            }
            if (net.signum() > 0) {
                events.add(new ActivityEvent(v, ActivityType.PAYOUT,
                        String.format("Settlement payout batch ₹%,d initiated to bank account", net.longValue()),
                        now.minusMinutes(12L + 30L * idx)));
            }
            idx++;
        }

        returns.stream().filter(r -> r.getStatus().isActionRequired()).limit(3)
                .forEach(r -> events.add(new ActivityEvent(r.getVendor(), ActivityType.RETURN_REQUESTED,
                        String.format("Return request submitted for Order #%s (%s)",
                                r.getCustomerOrder().getOrderNo(), r.getReason()),
                        r.getCreatedAt())));

        events.add(new ActivityEvent(chandran, ActivityType.STOCK_UPDATED,
                "Whiskas Ocean Fish Adult Cat Food (7kg) stock updated (+50 units added)", now.minusMinutes(42)));

        activityRepository.saveAll(events);
    }

    /** Status depends on how old the order is, so recent orders are open and old ones are delivered. */
    private OrderStatus pickStatus(Random r, long ageMin) {
        double x = r.nextDouble();
        double ageH = ageMin / 60.0;
        if (ageH < 2) return x < 0.7 ? OrderStatus.NEW : OrderStatus.PROCESSING;
        if (ageH < 8) {
            if (x < 0.15) return OrderStatus.NEW;
            if (x < 0.40) return OrderStatus.PROCESSING;
            if (x < 0.65) return OrderStatus.READY_TO_DISPATCH;
            if (x < 0.95) return OrderStatus.IN_TRANSIT;
            return OrderStatus.CANCELLED;
        }
        if (ageH < 24) {
            if (x < 0.05) return OrderStatus.READY_TO_DISPATCH; // stuck -> overdue SLA
            if (x < 0.95) return OrderStatus.IN_TRANSIT;
            return OrderStatus.CANCELLED;
        }
        if (ageH < 72) {
            if (x < 0.05) return OrderStatus.READY_TO_DISPATCH;
            if (x < 0.60) return OrderStatus.IN_TRANSIT;
            if (x < 0.95) return OrderStatus.DELIVERED;
            return OrderStatus.CANCELLED;
        }
        if (x < 0.03) return OrderStatus.RETURNED;
        if (x < 0.05) return OrderStatus.CANCELLED;
        return OrderStatus.DELIVERED;
    }

    private ReturnStatus pickReturnStatus(Random r) {
        double x = r.nextDouble();
        if (x < 0.30) return ReturnStatus.DISPUTE_REVIEW;
        if (x < 0.55) return ReturnStatus.REFUND_PENDING_APPROVAL;
        if (x < 0.80) return ReturnStatus.IN_TRANSIT_TO_WAREHOUSE;
        return ReturnStatus.REPLACEMENT_DISPATCHED;
    }

    /** Delhivery ~90% within SLA, BlueDart always, Shadowfax often late (demo "Delayed Alert") */
    private long transitMinutes(Random r, int courierIdx) {
        int hours;
        if (courierIdx == 0) hours = 22 + r.nextInt(30);
        else if (courierIdx == 1) hours = 16 + r.nextInt(20);
        else hours = 30 + r.nextInt(40);
        return hours * 60L + r.nextInt(60);
    }
}
