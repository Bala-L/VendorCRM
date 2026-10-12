package com.example.vendorcrm.service;

import com.example.vendorcrm.dto.*;
import com.example.vendorcrm.entity.*;
import com.example.vendorcrm.repository.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CommandCentreService {

    private static final Set<OrderStatus> PENDING_DISPATCH =
            EnumSet.of(
                    OrderStatus.NEW,
                    OrderStatus.PROCESSING,
                    OrderStatus.READY_TO_DISPATCH
            );

    private static final Set<OrderStatus> OPEN_AND_TRANSIT =
            EnumSet.of(
                    OrderStatus.NEW,
                    OrderStatus.PROCESSING,
                    OrderStatus.READY_TO_DISPATCH,
                    OrderStatus.IN_TRANSIT
            );

    private static final Set<OrderStatus> NON_REVENUE =
            EnumSet.of(OrderStatus.CANCELLED, OrderStatus.RETURNED);

    private static final Set<ReturnStatus> ACTION_RETURNS =
            EnumSet.of(
                    ReturnStatus.DISPUTE_REVIEW,
                    ReturnStatus.REFUND_PENDING_APPROVAL
            );

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int INVENTORY_LIMIT = 8;

    private enum Health {
        OUT_OF_STOCK("Out of Stock"),
        CRITICAL_LOW("Critical Low"),
        LOW_STOCK("Low Stock"),
        HEALTHY("Healthy");

        private final String label;

        Health(String label) {
            this.label = label;
        }
    }

    private record Period(
            String range,
            LocalDateTime from,
            LocalDateTime to,
            LocalDateTime prevFrom,
            LocalDateTime prevTo,
            String salesTitle,
            String ordersTitle,
            String compareLabel,
            String deliveredLabel
    ) {}

    private record SlaInfo(String text, boolean breached) {}

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ReturnRequestRepository returnRepository;
    private final ActivityEventRepository activityRepository;
    private final UserRepository userRepository;

    private final ZoneId zone;
    private final int urgentHours;
    private final int dispatchHours;
    private final BigDecimal dispatchTarget;
    private final BigDecimal carrierTarget;
    private final int payoutHour;

    public CommandCentreService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            ReturnRequestRepository returnRepository,
            ActivityEventRepository activityRepository,
            UserRepository userRepository,
            @Value("${app.timezone:Asia/Kolkata}") String timezone,
            @Value("${app.sla.urgent-hours:2}") int urgentHours,
            @Value("${app.sla.dispatch-hours:4}") int dispatchHours,
            @Value("${app.sla.dispatch-target:95}") BigDecimal dispatchTarget,
            @Value("${app.sla.carrier-target:95}") BigDecimal carrierTarget,
            @Value("${app.settlement.payout-hour:11}") int payoutHour
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.returnRepository = returnRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.zone = ZoneId.of(timezone);
        this.urgentHours = urgentHours;
        this.dispatchHours = dispatchHours;
        this.dispatchTarget = dispatchTarget;
        this.carrierTarget = carrierTarget;
        this.payoutHour = payoutHour;
    }

    // ---------------------------------------------------------------------
    // Command centre overview
    // ---------------------------------------------------------------------

    public CommandCentreResponse overview(Authentication auth, String range) {
        Long vendorId = resolveVendorId(auth);
        Period period = period(range);
        LocalDateTime now = LocalDateTime.now(zone);

        List<OperationalRow> currentRows =
                orderRepository.findOperationalRows(
                        period.from(), period.to(), vendorId
                );

        List<OperationalRow> previousRows =
                orderRepository.findOperationalRows(
                        period.prevFrom(), period.prevTo(), vendorId
                );

        List<OperationalRow> openRows =
                orderRepository.findOperationalRowsByStatus(
                        OPEN_AND_TRANSIT, vendorId
                );

        List<OperationalRow> deliveredRows =
                orderRepository.findDeliveredRows(
                        period.from(), period.to(), vendorId
                );

        List<OperationalRow> pending = openRows.stream()
        		.filter(row -> row.status().isPendingDispatch())
                .toList();

        List<OperationalRow> inTransit = openRows.stream()
                .filter(row -> row.status() == OrderStatus.IN_TRANSIT)
                .toList();

        LocalDateTime urgentLimit = now.plusHours(urgentHours);

        List<OperationalRow> urgent = pending.stream()
                .filter(row -> row.slaDueAt() != null
                        && !row.slaDueAt().isAfter(urgentLimit))
                .toList();

        long overdue = urgent.stream()
                .filter(row -> row.slaDueAt().isBefore(now))
                .count();

        BigDecimal currentSales = revenue(currentRows);
        BigDecimal previousSales = revenue(previousRows);

        long liveOrders = currentRows.stream()
                .filter(row -> row.status() != OrderStatus.CANCELLED)
                .count();

        long lastHour = orderRepository.countPlacedSince(
                now.minusHours(1), vendorId
        );

        BigDecimal pendingPayout =
                orderRepository.pendingSettlement(vendorId);

        if (pendingPayout == null) {
            pendingPayout = BigDecimal.ZERO;
        }

        long[] onTime = onTimeStats(deliveredRows);

        long actionReturns =
                returnRepository.countByStatuses(ACTION_RETURNS, vendorId);

        long disputes = returnRepository.countByStatuses(
                EnumSet.of(ReturnStatus.DISPUTE_REVIEW), vendorId
        );

        List<Product> products = productRepository.findActiveScoped(vendorId);

        long attentionSkus = products.stream()
                .filter(product -> health(product) != Health.HEALTHY)
                .count();

        long outOfStock = products.stream()
                .filter(product -> health(product) == Health.OUT_OF_STOCK)
                .count();

        BigDecimal slaHealth = dispatchSlaHealth(currentRows, now);

        List<MetricDto> metrics = List.of(
                new MetricDto(
                        "grossSales",
                        period.salesTitle(),
                        currentSales,
                        "CURRENCY",
                        period.compareLabel(),
                        previousSales
                ),
                new MetricDto(
                        "liveOrders",
                        period.ordersTitle(),
                        BigDecimal.valueOf(liveOrders),
                        "NUMBER",
                        lastHour + " in last hour",
                        null
                ),
                new MetricDto(
                        "pendingDispatch",
                        "Pending Dispatch",
                        BigDecimal.valueOf(pending.size()),
                        "NUMBER",
                        urgent.size() + " urgent SLA (<" + urgentHours + "h)",
                        null
                ),
                new MetricDto(
                        "inTransit",
                        "Shipments In-Transit",
                        BigDecimal.valueOf(inTransit.size()),
                        "NUMBER",
                        onTime[1] == 0
                                ? "No deliveries in period"
                                : pct1(onTime[0], onTime[1])
                                        .toPlainString() + "% on-time delivery",
                        null
                ),
                new MetricDto(
                        "returnsAwaiting",
                        "Returns Awaiting Action",
                        BigDecimal.valueOf(actionReturns),
                        "NUMBER",
                        disputes + (disputes == 1
                                ? " dispute pending"
                                : " disputes pending"),
                        null
                ),
                new MetricDto(
                        "lowStock",
                        "Low Stock Alert",
                        BigDecimal.valueOf(attentionSkus),
                        "SKU_COUNT",
                        outOfStock + " out of stock",
                        null
                ),
                new MetricDto(
                        "unsettledPayout",
                        "Unsettled Payout",
                        pendingPayout,
                        "CURRENCY",
                        pendingPayout.signum() > 0
                                ? "Next payout: Tomorrow"
                                : "No pending payout",
                        null
                ),
                new MetricDto(
                        "dispatchSla",
                        "Dispatch SLA Health",
                        slaHealth,
                        "PERCENT",
                        "Target: " + dispatchTarget
                                .setScale(1, RoundingMode.HALF_UP)
                                .toPlainString() + "%",
                        null
                )
        );

        long newOrders = openRows.stream()
                .filter(row -> row.status() == OrderStatus.NEW)
                .count();

        long packed = openRows.stream()
                .filter(row ->
                        row.status() == OrderStatus.PROCESSING
                        || row.status() == OrderStatus.READY_TO_DISPATCH
                )
                .count();

        List<PipelineStageDto> pipeline = List.of(
                new PipelineStageDto(
                        "newOrders",
                        "New Orders",
                        newOrders,
                        "Awaiting Confirmation",
                        "#3B82F6"
                ),
                new PipelineStageDto(
                        "packedReady",
                        "Packed / Ready",
                        packed,
                        "Awaiting Courier Pickup",
                        "#F59E0B"
                ),
                new PipelineStageDto(
                        "inTransit",
                        "In-Transit",
                        inTransit.size(),
                        "On-Route Delivery",
                        "#8B5CF6"
                ),
                new PipelineStageDto(
                        "delivered",
                        period.deliveredLabel(),
                        deliveredRows.size(),
                        "Completed Orders",
                        "#10B981"
                )
        );
        List<MetricDto> settlement =
                settlement(currentRows, now, pendingPayout);
        List<InventoryHealthDto> inventory = products.stream()
                .sorted(
                        Comparator.comparing((Product product) -> health(product))
                                .thenComparingInt(Product::getStock)
                                .thenComparing(Product::getName)
                )
                .limit(INVENTORY_LIMIT)
                .map(product -> new InventoryHealthDto(
                        product.getSku(),
                        product.getVendor().getName(),
                        product.getName(),
                        product.getStock(),
                        product.getLowStockThreshold(),
                        health(product).label
                ))
                .toList();

        List<ReturnMonitoringDto> returns = returnRepository.findLatest(
                EnumSet.allOf(ReturnStatus.class),
                vendorId,
                PageRequest.of(0, 6)
        )
        .stream()
        .map(request -> new ReturnMonitoringDto(
                String.valueOf(request.getReturnNo()),
                String.valueOf(request.getCustomerOrder().getOrderNo()),
                request.getProduct().getName(),
                request.getVendor().getName(),
                request.getReason(),
                request.getStatus().getLabel(),
                request.getStatus().isActionRequired()
        ))
        .toList();
        List<ActivityFeedDto> activity = activityRepository.findLatest(
                vendorId,
                PageRequest.of(0, 8)
        )
        .stream()
        .map(event -> new ActivityFeedDto(
                event.getId(),
                event.getType().name(),
                event.getMessage(),
                toInstant(event.getCreatedAt()),
                "",  // time
                "",  // icon
                ""   // color
        ))
        .toList();
        List<KpiDto> kpis = metrics.stream()
                .map(metric -> new KpiDto(
                        metric.key(),
                        metric.title(),
                        metric.value(),
                        metric.format(),
                        null,  // changePercent
                        true,  // positiveIsGood
                        metric.subtext()
                ))
                .toList();
        List<ActionItemDto> actions =
                actionItems(vendorId, urgent, overdue, products);

        CommandCentreResponse response = new CommandCentreResponse();

        response.setUserRole(
                vendorId == null ? "ADMIN" : "VENDOR"
        );

        response.setLiveMetrics(kpis);
        response.setActionRequiredItems(actions);
        response.setOrderPipeline(pipeline);
        response.setDeliveryMonitoring(
                carriers(deliveredRows, inTransit)
        );
        response.setInventory(inventory);
        response.setReturns(returns);
        response.setActivityFeed(activity);

        return response;
        }
    public List<OperationalOrderDto> orders(
            Authentication auth,
            String range,
            String tab,
            String search,
            int limit
    ) {
        Long vendorId = resolveVendorId(auth);
        Period period = period(range);
        LocalDateTime now = LocalDateTime.now(zone);

        Set<OrderStatus> statuses;

        if ("pending".equalsIgnoreCase(tab)) {
            statuses = PENDING_DISPATCH;
        } else if ("transit".equalsIgnoreCase(tab)) {
            statuses = EnumSet.of(OrderStatus.IN_TRANSIT);
        } else {
            statuses = EnumSet.allOf(OrderStatus.class);
        }

        String query = "%"
                + (search == null ? "" : search.trim().toLowerCase(Locale.ROOT))
                + "%";

        int safeLimit = Math.min(Math.max(limit, 1), 200);

        List<OperationalOrderDto> result = new ArrayList<>();

        for (Order order : orderRepository.findForTriage(
                period.from(),
                period.to(),
                OPEN_AND_TRANSIT,
                statuses,
                vendorId,
                query,
                PageRequest.of(0, safeLimit)
        )) {
            SlaInfo slaInfo = sla(order, now);

            result.add(new OperationalOrderDto(
                    order.getOrderNo(),
                    order.getVendor().getName(),
                    order.getCustomer().getName(),
                    itemsText(order),
                    order.getTotalAmount(),
                    order.getCourier() == null
                            ? "—"
                            : order.getCourier().getName(),
                    slaInfo.text(),
                    slaInfo.breached(),
                    order.getStatus().getLabel()
            ));
        }

        return result;
    }

    // ---------------------------------------------------------------------
    // Vendor authorization
    // ---------------------------------------------------------------------

    private Long resolveVendorId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }

        User user = userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getRole() == Role.VENDOR) {
            if (user.getVendor() == null) {
                throw new AccessDeniedException(
                        "No vendor linked to this account"
                );
            }

            return user.getVendor().getId();
        }

        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        return null;
    }

    // ---------------------------------------------------------------------
    // Date ranges
    // ---------------------------------------------------------------------

    private Period period(String range) {
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDate today = now.toLocalDate();

        String normalizedRange = range == null || range.isBlank()
                ? "today"
                : range.trim().toLowerCase(Locale.ROOT);

        LocalDateTime from;
        LocalDateTime to;
        String salesTitle;
        String ordersTitle;
        String compareLabel;
        String deliveredLabel;

        switch (normalizedRange) {
            case "today" -> {
                from = today.atStartOfDay();
                to = today.plusDays(1).atStartOfDay();
                salesTitle = "Today's Gross Sales";
                ordersTitle = "Live Orders Today";
                compareLabel = "vs yesterday";
                deliveredLabel = "Delivered Today";
            }
            case "yesterday" -> {
                from = today.minusDays(1).atStartOfDay();
                to = today.atStartOfDay();
                salesTitle = "Yesterday's Gross Sales";
                ordersTitle = "Orders Yesterday";
                compareLabel = "vs previous day";
                deliveredLabel = "Delivered Yesterday";
            }
            case "24h" -> {
                from = now.minusHours(24);
                to = now.plusSeconds(1);
                salesTitle = "Gross Sales (24h)";
                ordersTitle = "Orders (24h)";
                compareLabel = "vs prior 24h";
                deliveredLabel = "Delivered (24h)";
            }
            case "week" -> {
                from = today.minusDays(6).atStartOfDay();
                to = today.plusDays(1).atStartOfDay();
                salesTitle = "Gross Sales (7 days)";
                ordersTitle = "Orders (7 days)";
                compareLabel = "vs previous 7 days";
                deliveredLabel = "Delivered (7 days)";
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid range: " + range
            );
        }

        Duration duration = Duration.between(from, to);

        return new Period(
                normalizedRange,
                from,
                to,
                from.minus(duration),
                from,
                salesTitle,
                ordersTitle,
                compareLabel,
                deliveredLabel
        );
    }

    // ---------------------------------------------------------------------
    // Sales and SLA calculations
    // ---------------------------------------------------------------------

    private static BigDecimal revenue(List<OperationalRow> rows) {
        BigDecimal total = BigDecimal.ZERO;

        for (OperationalRow row : rows) {
            if (!NON_REVENUE.contains(row.status())
                    && row.totalAmount() != null) {
                total = total.add(row.totalAmount());
            }
        }

        return total;
    }

    private static long[] onTimeStats(List<OperationalRow> deliveredRows) {
        long onTime = 0;
        long total = 0;

        for (OperationalRow row : deliveredRows) {
            if (row.dispatchedAt() == null
                    || row.deliveredAt() == null
                    || row.courierSlaHours() == null) {
                continue;
            }

            total++;

            if (Duration.between(
                    row.dispatchedAt(),
                    row.deliveredAt()
            ).toMinutes() <= row.courierSlaHours() * 60L) {
                onTime++;
            }
        }

        return new long[]{onTime, total};
    }

    private BigDecimal dispatchSlaHealth(
            List<OperationalRow> rows,
            LocalDateTime now
    ) {
        long successful = 0;
        long breached = 0;

        for (OperationalRow row : rows) {
            if (row.slaDueAt() == null
                    || row.status() == OrderStatus.CANCELLED) {
                continue;
            }

            if (row.dispatchedAt() != null) {
                if (row.dispatchedAt().isAfter(row.slaDueAt())) {
                    breached++;
                } else {
                    successful++;
                }
            } else if (now.isAfter(row.slaDueAt())) {
                breached++;
            }
        }

        long total = successful + breached;

        return total == 0
                ? new BigDecimal("100.0")
                : pct1(successful, total);
    }

    // ---------------------------------------------------------------------
    // Settlement metrics
    // ---------------------------------------------------------------------

    private List<MetricDto> settlement(
            List<OperationalRow> rows,
            LocalDateTime now,
            BigDecimal pendingPayout
    ) {
        long delivered = rows.stream()
                .filter(row -> row.status() == OrderStatus.DELIVERED)
                .count();

        long returned = rows.stream()
                .filter(row -> row.status() == OrderStatus.RETURNED)
                .count();

        long cancelled = rows.stream()
                .filter(row -> row.status() == OrderStatus.CANCELLED)
                .count();

        long finalized = delivered + returned + cancelled;

        BigDecimal fulfilment = finalized == 0
                ? BigDecimal.ZERO.setScale(1)
                : pct1(delivered, finalized);

        String fulfilmentHint = finalized == 0
                ? "No completed orders yet"
                : fulfilment.compareTo(new BigDecimal("95")) >= 0
                        ? "Top tier ranking"
                        : fulfilment.compareTo(new BigDecimal("90")) >= 0
                                ? "Good standing"
                                : "Needs improvement";

        long dispatchedCount = 0;
        long totalMinutes = 0;

        for (OperationalRow row : rows) {
            if (row.dispatchedAt() == null || row.orderDate() == null) {
                continue;
            }

            dispatchedCount++;

            totalMinutes += Math.max(
                    0,
                    Duration.between(
                            row.orderDate(),
                            row.dispatchedAt()
                    ).toMinutes()
            );
        }

        BigDecimal averageHours = dispatchedCount == 0
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(totalMinutes)
                        .divide(
                                BigDecimal.valueOf(dispatchedCount * 60L),
                                1,
                                RoundingMode.HALF_UP
                        );

        String averageHint = dispatchedCount == 0
                ? "No dispatches yet"
                : averageHours.compareTo(
                        BigDecimal.valueOf(dispatchHours)
                ) <= 0
                        ? "Within SLA benchmark"
                        : "Above SLA benchmark";

        BigDecimal totalRevenue = revenue(rows);
        BigDecimal commission = BigDecimal.ZERO;

        for (OperationalRow row : rows) {
            if (!NON_REVENUE.contains(row.status())
                    && row.commissionAmount() != null) {
                commission = commission.add(row.commissionAmount());
            }
        }

        String commissionHint = totalRevenue.signum() == 0
                ? "No sales in period"
                : commission.multiply(HUNDRED)
                        .divide(totalRevenue, 1, RoundingMode.HALF_UP)
                        .stripTrailingZeros()
                        .toPlainString() + "% Platform fee rate";

        String payoutTime = LocalTime.of(payoutHour, 0)
                .format(DateTimeFormatter.ofPattern("h a", Locale.ENGLISH));

        return List.of(
                new MetricDto(
                        "fulfilmentRate",
                        "Fulfillment Rate",
                        fulfilment,
                        "PERCENT",
                        fulfilmentHint,
                        null
                ),
                new MetricDto(
                        "avgDispatchTime",
                        "Avg Dispatch Time",
                        averageHours,
                        "HOURS",
                        averageHint,
                        null
                ),
                new MetricDto(
                        "availablePayout",
                        "Available Payout",
                        pendingPayout,
                        "CURRENCY",
                        pendingPayout.signum() > 0
                                ? "Scheduled: Tomorrow " + payoutTime
                                : "Nothing scheduled",
                        null
                ),
                new MetricDto(
                        "commission",
                        "Commission Deducted",
                        commission,
                        "CURRENCY",
                        commissionHint,
                        null
                )
        );
    }

    // ---------------------------------------------------------------------
    // Carrier performance
    // ---------------------------------------------------------------------

    private List<CarrierMonitoringDto> carriers(
            List<OperationalRow> deliveredRows,
            List<OperationalRow> inTransitRows
    ) {
        Map<String, List<OperationalRow>> deliveredByCarrier =
                deliveredRows.stream().collect(
                        Collectors.groupingBy(
                                row -> courierName(row.courierName())
                        )
                );

        Map<String, Long> activeByCarrier = inTransitRows.stream()
                .collect(
                        Collectors.groupingBy(
                                row -> courierName(row.courierName()),
                                Collectors.counting()
                        )
                );

        Set<String> carrierNames = new TreeSet<>();
        carrierNames.addAll(deliveredByCarrier.keySet());
        carrierNames.addAll(activeByCarrier.keySet());

        List<CarrierMonitoringDto> result = new ArrayList<>();

        for (String carrier : carrierNames) {
            List<OperationalRow> rows = deliveredByCarrier.getOrDefault(
                    carrier,
                    List.of()
            );

            long[] stats = onTimeStats(rows);

            long transitCount = 0;
            long transitMinutes = 0;

            for (OperationalRow row : rows) {
                if (row.dispatchedAt() == null || row.deliveredAt() == null) {
                    continue;
                }

                transitCount++;

                transitMinutes += Duration.between(
                        row.dispatchedAt(),
                        row.deliveredAt()
                ).toMinutes();
            }

            BigDecimal onTimeRate = stats[1] == 0
                    ? null
                    : pct1(stats[0], stats[1]);

            BigDecimal averageDays = transitCount == 0
                    ? null
                    : BigDecimal.valueOf(transitMinutes)
                            .divide(
                                    BigDecimal.valueOf(transitCount * 1440L),
                                    1,
                                    RoundingMode.HALF_UP
                            );

            String status = onTimeRate == null
                    ? "No Data"
                    : onTimeRate.compareTo(carrierTarget) >= 0
                            ? "Optimal"
                            : "Delayed Alert";

            result.add(new CarrierMonitoringDto(
                    carrier,
                    activeByCarrier.getOrDefault(carrier, 0L),
                    onTimeRate,
                    averageDays,
                    status
            ));
        }

        
        result.sort(
            Comparator.comparingLong(CarrierMonitoringDto::getActiveShipments)
                .reversed()
                .thenComparing(CarrierMonitoringDto::getCarrier)
        );
        

        return result;
    }

    private static String courierName(String name) {
        return name == null ? "Unassigned" : name;
    }

    // ---------------------------------------------------------------------
    // Action items
    // ---------------------------------------------------------------------

    private List<ActionItemDto> actionItems(
            Long vendorId,
            List<OperationalRow> urgent,
            long overdue,
            List<Product> products
    ) {
        List<ActionItemDto> actions = new ArrayList<>();
        boolean vendorScoped = vendorId != null;

        if (!urgent.isEmpty()) {
            LocalDateTime earliest = urgent.stream()
                    .map(OperationalRow::slaDueAt)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            String description = overdue > 0
                    ? overdue + " already past SLA. Hand over to the courier as soon as possible."
                    : "Due within " + urgentHours
                            + " hours. Hand over to the courier before the deadline.";

            actions.add(new ActionItemDto(
                    String.format("ACT-%02d", actions.size() + 1),
                    "CRITICAL",
                    urgent.size()
                            + (urgent.size() == 1
                                    ? " Order"
                                    : " Orders")
                            + " Pending Dispatch, SLA at risk"
                            + (vendorScoped ? "" : " (Multi-Vendor)"),
                    description,
                    toInstant(earliest),
                    "pending",
                    null,
                    vendorScoped ? "Review My Orders" : "Review Orders"
            ));
        }

        List<Product> outOfStockProducts = products.stream()
                .filter(product -> product.getStock() <= 0)
                .toList();

        if (!outOfStockProducts.isEmpty()) {
            long queuedOrders =
                    orderItemRepository.countOrdersContainingProducts(
                            PENDING_DISPATCH,
                            outOfStockProducts.stream()
                                    .map(Product::getId)
                                    .toList()
                    );

            String title = outOfStockProducts.size() == 1
                    ? "Stockout Alert: "
                            + outOfStockProducts.get(0).getName()
                    : "Stockout Alert across "
                            + outOfStockProducts.size() + " SKUs";

            String description = queuedOrders > 0
                    ? "Inventory reached 0 units while "
                            + queuedOrders + " open "
                            + (queuedOrders == 1 ? "order is" : "orders are")
                            + " in queue"
                    : "Inventory reached 0 units";

            actions.add(new ActionItemDto(
                    String.format("ACT-%02d", actions.size() + 1),
                    "WARNING",
                    title,
                    description,
                    null,
                    null,
                    "inventory-section",
                    vendorScoped ? "Update Stock" : "Review Stockouts"
            ));
        }

        long disputes = returnRepository.countByStatuses(
                EnumSet.of(ReturnStatus.DISPUTE_REVIEW),
                vendorId
        );

        if (disputes > 0) {
            List<Return> latest = returnRepository.findLatest(
                    EnumSet.of(ReturnStatus.DISPUTE_REVIEW),
                    vendorId,
                    PageRequest.of(0, 1)
            );

            if (!latest.isEmpty()) {
                Return first = latest.get(0);

                String title = disputes == 1
                        ? "Return Dispute: Order #"
                                + first.getCustomerOrder().getOrderNo()
                        : disputes + " Return Disputes need review";

                actions.add(new ActionItemDto(
                        String.format("ACT-%02d", actions.size() + 1),
                        "ATTENTION",
                        title,
                        first.getReason() + ", "
                                + first.getProduct().getName(),
                        toInstant(first.getCreatedAt()),
                        null,
                        "returns-section",
                        disputes == 1
                                ? "Inspect Dispute"
                                : "Inspect Disputes"
                ));
            }
        }

        return actions;
    }

    // ---------------------------------------------------------------------
    // Inventory health
    // ---------------------------------------------------------------------

    private static Health health(Product product) {
        int stock = product.getStock();
        int reorderThreshold = product.getLowStockThreshold();

        if (stock <= 0) {
            return Health.OUT_OF_STOCK;
        }

        if (stock <= reorderThreshold / 4) {
            return Health.CRITICAL_LOW;
        }

        if (stock <= reorderThreshold) {
            return Health.LOW_STOCK;
        }

        return Health.HEALTHY;
    }

    // ---------------------------------------------------------------------
    // Order SLA display
    // ---------------------------------------------------------------------

    private SlaInfo sla(Order order, LocalDateTime now) {
        OrderStatus status = order.getStatus();

        if (status == OrderStatus.DELIVERED) {
            return new SlaInfo("Completed", false);
        }

        if (status == OrderStatus.CANCELLED
                || status == OrderStatus.RETURNED) {
            return new SlaInfo(status.getLabel(), false);
        }

        if (status == OrderStatus.IN_TRANSIT) {
            Courier courier = order.getCourier();

            if (order.getDispatchedAt() == null
                    || courier == null
                    || courier.getSlaTransitHours() == null) {
                return new SlaInfo("In transit", false);
            }

            LocalDateTime due = order.getDispatchedAt()
                    .plusHours(courier.getSlaTransitHours());

            if (now.isAfter(due)) {
                return new SlaInfo(
                        "Delayed " + duration(Duration.between(due, now)),
                        true
                );
            }

            return new SlaInfo(
                    due.toLocalDate().equals(now.toLocalDate())
                            ? "Delivery Today"
                            : "Due in " + duration(Duration.between(now, due)),
                    false
            );
        }

        if (order.getSlaDueAt() == null) {
            return new SlaInfo("No SLA", false);
        }

        if (now.isAfter(order.getSlaDueAt())) {
            return new SlaInfo(
                    "Overdue " + duration(
                            Duration.between(order.getSlaDueAt(), now)
                    ),
                    true
            );
        }

        return new SlaInfo(
                duration(Duration.between(now, order.getSlaDueAt()))
                        + " remaining",
                false
        );
    }

    private static String duration(Duration duration) {
        long minutes = Math.max(0, duration.toMinutes());

        if (minutes >= 1440) {
            return (minutes / 1440) + "d "
                    + ((minutes % 1440) / 60) + "h";
        }

        if (minutes >= 60) {
            return (minutes / 60) + "h " + (minutes % 60) + "m";
        }

        return minutes + "m";
    }

    // ---------------------------------------------------------------------
    // Order item summary
    // ---------------------------------------------------------------------

    private static String itemsText(Order order) {
        List<OrderItem> items = order.getItems();

        if (items == null || items.isEmpty()) {
            return "—";
        }

        OrderItem first = items.get(0);

        String firstItem = first.getProduct().getName()
                + (first.getQuantity() > 1
                        ? " x" + first.getQuantity()
                        : "");

        return items.size() > 1
                ? firstItem + ", +" + (items.size() - 1) + " more"
                : firstItem;
    }

    // ---------------------------------------------------------------------
    // Date and percentage utilities
    // ---------------------------------------------------------------------

    private Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        try {
            return dateTime.atZone(zone).toInstant();
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private static BigDecimal pct1(long part, long whole) {
        if (whole <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
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