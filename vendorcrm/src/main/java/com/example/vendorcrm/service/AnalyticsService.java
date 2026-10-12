package com.example.vendorcrm.service;

import com.example.vendorcrm.dto.*;
import com.example.vendorcrm.entity.*;
import com.example.vendorcrm.repository.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    /** Cancelled and returned orders do not count as revenue. */
    private static final Set<OrderStatus> NON_REVENUE =
            EnumSet.of(OrderStatus.CANCELLED, OrderStatus.RETURNED);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private enum Granularity {
        HOUR, DAY, MONTH
    }

    private record Period(
            LocalDateTime from,
            LocalDateTime to,
            LocalDateTime prevFrom,
            LocalDateTime prevTo,
            Granularity granularity
    ) {}

    private record Sales(long units, BigDecimal revenue) {}

    private record Totals(
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal commission,
            long orders,
            long revenueOrders,
            long delivered,
            long returned,
            long cancelled,
            long customers
    ) {
        BigDecimal profit() {
            return revenue.subtract(cost).subtract(commission);
        }

        BigDecimal avgOrderValue() {
            return revenueOrders == 0
                    ? BigDecimal.ZERO
                    : revenue.divide(
                            BigDecimal.valueOf(revenueOrders),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        long finalized() {
            return delivered + returned + cancelled;
        }

        BigDecimal deliveryRate() {
            return rate(delivered, finalized());
        }

        BigDecimal returnRate() {
            return rate(returned, finalized());
        }
    }

    private static class Bucket {
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal profit = BigDecimal.ZERO;
        private long orders;
    }

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ZoneId zone;

    public AnalyticsService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            VendorRepository vendorRepository,
            @Value("${app.timezone:Asia/Kolkata}") String timezone
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.zone = ZoneId.of(timezone);
    }

    // =========================================================
    // Dashboard: KPIs, sales trend, category share, order status
    // =========================================================

    public DashboardResponse dashboard(
            Authentication auth,
            String range,
            String vendorCode
    ) {
        Long vendorId = resolveVendorId(auth, vendorCode);
        Period period = period(range);

        List<OrderRow> currentRows = orderRepository.findRows(
                period.from(), period.to(), vendorId
        );

        List<OrderRow> previousRows = orderRepository.findRows(
                period.prevFrom(), period.prevTo(), vendorId
        );

        Totals current = totals(currentRows);
        Totals previous = totals(previousRows);

        Map<Long, Sales> currentSales = salesByProduct(
                period.from(), period.to(), vendorId
        );

        Map<Long, Sales> previousSales = salesByProduct(
                period.prevFrom(), period.prevTo(), vendorId
        );

        long currentUnits = currentSales.values()
                .stream()
                .mapToLong(Sales::units)
                .sum();

        long previousUnits = previousSales.values()
                .stream()
                .mapToLong(Sales::units)
                .sum();

        List<Product> products = productRepository.findActiveScoped(vendorId);

        BigDecimal pendingSettlement =
                orderRepository.pendingSettlement(vendorId);

        if (pendingSettlement == null) {
            pendingSettlement = BigDecimal.ZERO;
        }

        String commissionNote = current.revenue().signum() == 0
                ? null
                : current.commission()
                        .multiply(HUNDRED)
                        .divide(current.revenue(), 1, RoundingMode.HALF_UP)
                        .stripTrailingZeros()
                        .toPlainString() + "% rate";

        boolean hasPreviousFinalizedOrders =
                previous.finalized() > 0 && current.finalized() > 0;

        List<KpiDto> kpis = List.of(
                new KpiDto(
                        "grossRevenue", "Gross Revenue",
                        current.revenue(), "CURRENCY",
                        pct(current.revenue(), previous.revenue()),
                        true, null
                ),
                new KpiDto(
                        "netProfit", "Net Profit",
                        current.profit(), "CURRENCY",
                        pct(current.profit(), previous.profit()),
                        true, null
                ),
                new KpiDto(
                        "salesCount", "Total Sales Count",
                        BigDecimal.valueOf(currentUnits), "NUMBER",
                        pct(
                                BigDecimal.valueOf(currentUnits),
                                BigDecimal.valueOf(previousUnits)
                        ),
                        true, null
                ),
                new KpiDto(
                        "totalOrders", "Total Orders",
                        BigDecimal.valueOf(current.orders()), "NUMBER",
                        pct(
                                BigDecimal.valueOf(current.orders()),
                                BigDecimal.valueOf(previous.orders())
                        ),
                        true, null
                ),
                new KpiDto(
                        "activeProducts", "Active Products",
                        BigDecimal.valueOf(products.size()), "NUMBER",
                        null, true, null
                ),
                new KpiDto(
                        "totalCustomers", "Total Customers",
                        BigDecimal.valueOf(current.customers()), "NUMBER",
                        pct(
                                BigDecimal.valueOf(current.customers()),
                                BigDecimal.valueOf(previous.customers())
                        ),
                        true, null
                ),
                new KpiDto(
                        "commission", "Commission Deducted",
                        current.commission(), "CURRENCY",
                        null, false, commissionNote
                ),
                new KpiDto(
                        "deliverySuccessRate", "Delivery Success Rate",
                        current.deliveryRate(), "PERCENT",
                        hasPreviousFinalizedOrders
                                ? pp(current.deliveryRate(), previous.deliveryRate())
                                : null,
                        true, null
                ),
                new KpiDto(
                        "returnRate", "Return Rate",
                        current.returnRate(), "PERCENT",
                        hasPreviousFinalizedOrders
                                ? pp(current.returnRate(), previous.returnRate())
                                : null,
                        false, null
                ),
                new KpiDto(
                        "avgOrderValue", "Avg Order Value",
                        current.avgOrderValue(), "CURRENCY",
                        pct(current.avgOrderValue(), previous.avgOrderValue()),
                        true, null
                ),
                new KpiDto(
                        "pendingSettlement", "Pending Settlement",
                        pendingSettlement, "CURRENCY",
                        null, true, null
                ),
                new KpiDto(
                        "cancelledOrders", "Cancelled Orders",
                        BigDecimal.valueOf(current.cancelled()), "NUMBER",
                        pct(
                                BigDecimal.valueOf(current.cancelled()),
                                BigDecimal.valueOf(previous.cancelled())
                        ),
                        false, null
                )
        );

        return new DashboardResponse(
                range == null ? "30d" : range.toLowerCase(Locale.ROOT),
                period.from().toLocalDate(),
                period.to().minusDays(1).toLocalDate(),
                kpis,
                trend(currentRows, period),
                categoryShare(products, currentSales),
                orderStatus(currentRows)
        );
    }

    // =========================================================
    // Products: filtering, sorting and pagination
    // =========================================================

    public PageResponse<ProductAnalyticsDto> products(
            Authentication auth,
            String range,
            String vendorCode,
            String category,
            String stockStatus,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Long vendorId = resolveVendorId(auth, vendorCode);
        Period period = period(range);

        StockStatus statusFilter = StockStatus.fromLabel(stockStatus);

        String query = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        boolean allCategories = category == null
                || category.isBlank()
                || category.equalsIgnoreCase("All");

        Map<Long, Sales> sales = salesByProduct(
                period.from(), period.to(), vendorId
        );

        List<ProductAnalyticsDto> rows = new ArrayList<>();

        for (Product product : productRepository.findActiveScoped(vendorId)) {
            if (!allCategories
                    && !product.getCategory().equalsIgnoreCase(category)) {
                continue;
            }

            if (statusFilter != null
                    && product.stockStatus() != statusFilter) {
                continue;
            }

            if (!query.isEmpty()
                    && !product.getName().toLowerCase(Locale.ROOT).contains(query)
                    && !product.getSku().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }

            Sales productSales = sales.get(product.getId());

            rows.add(new ProductAnalyticsDto(
                    product.getSku(),
                    product.getName(),
                    product.getCategory(),
                    product.getVendor().getName(),
                    productSales == null ? 0 : productSales.units(),
                    productSales == null
                            ? BigDecimal.ZERO
                            : productSales.revenue(),
                    product.getStock(),
                    product.stockStatus().getLabel()
            ));
        }

        String sortKey = sortBy == null
                ? "revenue"
                : sortBy.toLowerCase(Locale.ROOT);

        Comparator<ProductAnalyticsDto> comparator;

        switch (sortKey) {
            case "name" ->
                    comparator = Comparator.comparing(
                            ProductAnalyticsDto::name,
                            String.CASE_INSENSITIVE_ORDER
                    );
            case "sold" ->
                    comparator = Comparator.comparingLong(
                            ProductAnalyticsDto::sold
                    );
            case "stock" ->
                    comparator = Comparator.comparingInt(
                            ProductAnalyticsDto::stock
                    );
            default ->
                    comparator = Comparator.comparing(
                            ProductAnalyticsDto::revenue
                    );
        }

        if (!"asc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        rows.sort(comparator.thenComparing(ProductAnalyticsDto::id));

        int safeSize = Math.min(Math.max(size, 1), 5000);
        long total = rows.size();

        int totalPages = (int) Math.max(
                1, Math.ceil(total / (double) safeSize)
        );

        int safePage = Math.min(Math.max(page, 1), totalPages);

        int fromIndex = (int) Math.min(
                (long) (safePage - 1) * safeSize,
                total
        );

        int toIndex = (int) Math.min(
                (long) fromIndex + safeSize,
                total
        );

        return new PageResponse<>(
                new ArrayList<>(rows.subList(fromIndex, toIndex)),
                safePage,
                safeSize,
                total,
                totalPages
        );
    }

    // =========================================================
    // Filter dropdown options
    // =========================================================

    public FilterOptionsResponse filters(Authentication auth) {
        User user = currentUser(auth);

        if (user.getRole() == Role.VENDOR) {
            Long vendorId = vendorOf(user).getId();

            return new FilterOptionsResponse(
                    productRepository.findCategories(vendorId),
                    List.of()
            );
        }

        List<VendorDto> vendors = vendorRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        Vendor::getName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(VendorDto::from)
                .toList();

        return new FilterOptionsResponse(
                productRepository.findCategories(null),
                vendors
        );
    }

    // =========================================================
    // Security and user/vendor helpers
    // =========================================================

    private User currentUser(Authentication auth) {
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("User not found"));
    }

    private Vendor vendorOf(User user) {
        if (user.getVendor() == null) {
            throw new AccessDeniedException(
                    "No vendor linked to this account"
            );
        }

        return user.getVendor();
    }

    /**
     * Vendor users can access only their own data.
     * Admin users can access all vendors or select one by vendor code.
     */
    private Long resolveVendorId(
            Authentication auth,
            String vendorCode
    ) {
        User user = currentUser(auth);

        boolean noVendorSelected = vendorCode == null
                || vendorCode.isBlank()
                || vendorCode.equalsIgnoreCase("All");

        if (user.getRole() == Role.VENDOR) {
            Vendor ownVendor = vendorOf(user);

            if (!noVendorSelected
                    && !vendorCode.equals(ownVendor.getVendorCode())) {
                throw new AccessDeniedException(
                        "You can only view your own vendor data"
                );
            }

            return ownVendor.getId();
        }

        if (noVendorSelected) {
            return null;
        }

        return vendorRepository.findByVendorCode(vendorCode)
                .map(Vendor::getId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Vendor not found: " + vendorCode
                        ));
    }

    // =========================================================
    // Date range handling
    // =========================================================

    private Period period(String range) {
        LocalDate today = LocalDate.now(zone);
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        String selectedRange = range == null || range.isBlank()
                ? "30d"
                : range.toLowerCase(Locale.ROOT);

        LocalDateTime from;
        Granularity granularity;

        switch (selectedRange) {
            case "today" -> {
                from = today.atStartOfDay();
                granularity = Granularity.HOUR;
            }
            case "7d" -> {
                from = today.minusDays(6).atStartOfDay();
                granularity = Granularity.DAY;
            }
            case "30d" -> {
                from = today.minusDays(29).atStartOfDay();
                granularity = Granularity.DAY;
            }
            case "90d" -> {
                from = today.minusDays(89).atStartOfDay();
                granularity = Granularity.MONTH;
            }
            case "ytd" -> {
                from = today.withDayOfYear(1).atStartOfDay();
                granularity = Granularity.MONTH;
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid range: " + range
            );
        }

        Duration length = Duration.between(from, to);

        return new Period(
                from,
                to,
                from.minus(length),
                from,
                granularity
        );
    }

    // =========================================================
    // Product sales aggregation
    // =========================================================

    private Map<Long, Sales> salesByProduct(
            LocalDateTime from,
            LocalDateTime to,
            Long vendorId
    ) {
        Map<Long, Sales> sales = new HashMap<>();

        for (Object[] row : orderItemRepository.salesByProduct(
                from, to, vendorId, NON_REVENUE
        )) {
            sales.put(
                    ((Number) row[0]).longValue(),
                    new Sales(
                            ((Number) row[1]).longValue(),
                            new BigDecimal(row[2].toString())
                    )
            );
        }

        return sales;
    }

    // =========================================================
    // KPI totals
    // =========================================================

    private Totals totals(List<OrderRow> rows) {
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal commission = BigDecimal.ZERO;

        long revenueOrders = 0;
        long delivered = 0;
        long returned = 0;
        long cancelled = 0;

        Set<Long> customers = new HashSet<>();

        for (OrderRow row : rows) {
            if (row.status() == OrderStatus.DELIVERED) {
                delivered++;
            } else if (row.status() == OrderStatus.RETURNED) {
                returned++;
            } else if (row.status() == OrderStatus.CANCELLED) {
                cancelled++;
            }

            if (!NON_REVENUE.contains(row.status())) {
                revenue = revenue.add(row.totalAmount());
                cost = cost.add(row.totalCost());
                commission = commission.add(row.commissionAmount());
                revenueOrders++;
            }

            if (row.status() != OrderStatus.CANCELLED) {
                customers.add(row.customerId());
            }
        }

        return new Totals(
                revenue,
                cost,
                commission,
                rows.size(),
                revenueOrders,
                delivered,
                returned,
                cancelled,
                customers.size()
        );
    }

    // =========================================================
    // Sales trend chart
    // =========================================================

    private List<TrendPointDto> trend(
            List<OrderRow> rows,
            Period period
    ) {
        Granularity granularity = period.granularity();

        Map<LocalDateTime, Bucket> buckets = new TreeMap<>();

        LocalDateTime cursor = truncate(period.from(), granularity);

        while (cursor.isBefore(period.to())) {
            buckets.put(cursor, new Bucket());
            cursor = next(cursor, granularity);
        }

        for (OrderRow row : rows) {
            if (NON_REVENUE.contains(row.status())) {
                continue;
            }

            Bucket bucket = buckets.get(
                    truncate(row.orderDate(), granularity)
            );

            if (bucket == null) {
                continue;
            }

            bucket.revenue = bucket.revenue.add(row.totalAmount());

            bucket.profit = bucket.profit.add(
                    row.totalAmount()
                            .subtract(row.totalCost())
                            .subtract(row.commissionAmount())
            );

            bucket.orders++;
        }

        DateTimeFormatter formatter = switch (granularity) {
            case HOUR -> DateTimeFormatter.ofPattern(
                    "HH:00", Locale.ENGLISH
            );
            case DAY -> DateTimeFormatter.ofPattern(
                    "dd MMM", Locale.ENGLISH
            );
            case MONTH -> DateTimeFormatter.ofPattern(
                    "MMM yy", Locale.ENGLISH
            );
        };

        List<TrendPointDto> result = new ArrayList<>();

        for (Map.Entry<LocalDateTime, Bucket> entry : buckets.entrySet()) {
            Bucket bucket = entry.getValue();

            result.add(new TrendPointDto(
                    entry.getKey().format(formatter),
                    bucket.revenue,
                    bucket.profit,
                    bucket.orders
            ));
        }

        return result;
    }

    private static LocalDateTime truncate(
            LocalDateTime dateTime,
            Granularity granularity
    ) {
        return switch (granularity) {
            case HOUR -> dateTime.truncatedTo(ChronoUnit.HOURS);
            case DAY -> dateTime.toLocalDate().atStartOfDay();
            case MONTH -> dateTime.toLocalDate()
                    .withDayOfMonth(1)
                    .atStartOfDay();
        };
    }

    private static LocalDateTime next(
            LocalDateTime dateTime,
            Granularity granularity
    ) {
        return switch (granularity) {
            case HOUR -> dateTime.plusHours(1);
            case DAY -> dateTime.plusDays(1);
            case MONTH -> dateTime.plusMonths(1);
        };
    }

    // =========================================================
    // Category sales share
    // =========================================================

    private List<CategoryShareDto> categoryShare(
            List<Product> products,
            Map<Long, Sales> sales
    ) {
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();

        for (Product product : products) {
            Sales productSales = sales.get(product.getId());

            if (productSales != null) {
                revenueByCategory.merge(
                        product.getCategory(),
                        productSales.revenue(),
                        BigDecimal::add
                );
            }
        }

        BigDecimal totalRevenue = revenueByCategory.values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRevenue.signum() == 0) {
            return List.of();
        }

        return revenueByCategory.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, BigDecimal>comparingByValue()
                                .reversed()
                )
                .map(entry -> new CategoryShareDto(
                        entry.getKey(),
                        entry.getValue()
                                .multiply(HUNDRED)
                                .divide(
                                        totalRevenue,
                                        1,
                                        RoundingMode.HALF_UP
                                )
                ))
                .toList();
    }

    // =========================================================
    // Order status chart
    // =========================================================

    private List<OrderStatusDto> orderStatus(List<OrderRow> rows) {
        Map<OrderStatus, Long> counts =
                new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0L);
        }

        for (OrderRow row : rows) {
            counts.merge(row.status(), 1L, Long::sum);
        }

        List<OrderStatusDto> result = new ArrayList<>();

        for (OrderStatus status : OrderStatus.values()) {
            result.add(new OrderStatusDto(
                    status.getLabel(),
                    counts.get(status)
            ));
        }

        return result;
    }

    // =========================================================
    // Percentage calculations
    // =========================================================

    /** Percentage change compared with the previous period. */
    private static Double pct(
            BigDecimal current,
            BigDecimal previous
    ) {
        if (previous == null || previous.signum() == 0) {
            return null;
        }

        return current.subtract(previous)
                .multiply(HUNDRED)
                .divide(previous.abs(), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** Percentage-point difference between two rates. */
    private static Double pp(
            BigDecimal current,
            BigDecimal previous
    ) {
        return current.subtract(previous)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static BigDecimal rate(long part, long whole) {
        if (whole == 0) {
            return BigDecimal.ZERO.setScale(1);
        }

        return BigDecimal.valueOf(part)
                .multiply(HUNDRED)
                .divide(
                        BigDecimal.valueOf(whole),
                        1,
                        RoundingMode.HALF_UP
                );
    }
}