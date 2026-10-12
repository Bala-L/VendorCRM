import  { useState, useEffect, useRef } from "react";
import {
  Radio,
  RefreshCw,
  Search,
  Clock,
  Truck,
  Package,
  RotateCcw,
  DollarSign,
  CheckCircle2,
  ChevronRight,
  ShieldAlert,
  Flame,
  Activity,
  Award,
  Boxes,
  Lock,
} from "lucide-react";
import "./CommandCentre.css";

const ALL_OPERATIONAL_ORDERS = [
  {
    id: "ORD-89432",
    vendor: "Chandran Retailers Ltd",
    customer: "Rajesh ",
    items: "Royal Canin (15kg)",
    amount: "₹3,499",
    courier: "Delhivery",
    status: "New",
    sla: "1h 45m remaining",
  },
  {
    id: "ORD-89428",
    vendor: "BlueCross Animal Pharma",
    customer: "Ram",
    items: "Bravecto Flea Tablet x2",
    amount: "₹2,100",
    courier: "BlueDart",
    status: "Processing",
    sla: "42m remaining",
  },
  {
    id: "ORD-89419",
    vendor: "PetVitals Co.",
    customer: "Anand",
    items: "Himalaya Coat Shampoo",
    amount: "₹450",
    courier: "Shadowfax",
    status: "Ready to Dispatch",
    sla: "Overdue 12m",
  },
  {
    id: "ORD-89402",
    vendor: "Zenve Companion Supplies",
    customer: "Vikram ",
    items: "Memory Foam Pet Bed (L)",
    amount: "₹2,800",
    courier: "Delhivery",
    status: "In-Transit",
    sla: "Delivery Today",
  },
  {
    id: "ORD-89381",
    vendor: "Chandran Retailers Ltd",
    customer: "Soodan",
    items: "Pedigree Dentastix (Pack of 7)",
    amount: "₹380",
    courier: "ExpressBees",
    status: "Delivered",
    sla: "Completed",
  },
  {
    id: "ORD-89365",
    vendor: "Chandran Retailers Ltd",
    customer: "Kavitha R.",
    items: "Whiskas Ocean Fish (7kg)",
    amount: "₹1,850",
    courier: "Delhivery",
    status: "Processing",
    sla: "25m remaining",
  },
];

const ALL_INVENTORY_HEALTH = [
  {
    sku: "SKU-RC-15KG",
    vendor: "Chandran Retailers Ltd",
    name: "Royal Canin Maxi Adult Dog (15kg)",
    currentStock: 0,
    reorderLevel: 25,
    status: "Out of Stock",
  },
  {
    sku: "SKU-BV-FLEA",
    vendor: "BlueCross Animal Pharma",
    name: "Bravecto Chewable Tick Tablet",
    currentStock: 4,
    reorderLevel: 20,
    status: "Critical Low",
  },
  {
    sku: "SKU-WH-7KG",
    vendor: "Chandran Retailers Ltd",
    name: "Whiskas Ocean Fish Cat Food (7kg)",
    currentStock: 12,
    reorderLevel: 30,
    status: "Low Stock",
  },
  {
    sku: "SKU-PD-BED",
    vendor: "Zenve Companion Supplies",
    name: "Orthopedic Memory Foam Pet Bed",
    currentStock: 48,
    reorderLevel: 15,
    status: "Healthy",
  },
  {
    sku: "SKU-HM-SHP",
    vendor: "PetVitals Co.",
    name: "Himalaya Erina Plus Coat Cleanser",
    currentStock: 64,
    reorderLevel: 20,
    status: "Healthy",
  },
];

const ALL_RETURNS = [
  {
    id: "RET-1092",
    vendor: "BlueCross Animal Pharma",
    orderId: "ORD-89410",
    item: "Bravecto Tick Tablet",
    reason: "Damaged Package Seal",
    status: "Dispute Review",
    actionRequired: true,
  },
  {
    id: "RET-1088",
    vendor: "Zenve Companion Supplies",
    orderId: "ORD-89354",
    item: "Cat Bed Memory Foam",
    reason: "Wrong Size Selected",
    status: "In-Transit to Warehouse",
    actionRequired: false,
  },
  {
    id: "RET-1081",
    vendor: "PetVitals Co.",
    orderId: "ORD-89211",
    item: "Himalaya Coat Shampoo",
    reason: "Leakage on Delivery",
    status: "Refund Pending Approval",
    actionRequired: true,
  },
  {
    id: "RET-1075",
    vendor: "Chandran Retailers Ltd",
    orderId: "ORD-89190",
    item: "Royal Canin Puppy Food",
    reason: "Expired Bag Dispatched",
    status: "Replacement Dispatched",
    actionRequired: false,
  },
];

const DELIVERY_MONITORING = [
  {
    carrier: "Delhivery Express",
    activeShipments: 42,
    onTimeRate: "98.4%",
    avgTransit: "1.8 Days",
    status: "Optimal",
  },
  {
    carrier: "BlueDart Aviation",
    activeShipments: 28,
    onTimeRate: "99.1%",
    avgTransit: "1.2 Days",
    status: "Optimal",
  },
  {
    carrier: "Shadowfax Local",
    activeShipments: 16,
    onTimeRate: "91.2%",
    avgTransit: "2.4 Days",
    status: "Delayed Alert",
  },
];

const INITIAL_ACTIVITY_FEED = [
  {
    id: 1,
    text: "New Order #ORD-89432 placed by Rajesh Kumar for Royal Canin",
    time: "Just now",
    icon: Package,
    color: "#176B5B",
  },
  {
    id: 2,
    text: "Shipment #TRK-58291 marked Out for Delivery via BlueDart",
    time: "3 mins ago",
    icon: Truck,
    color: "#4A90A4",
  },
  {
    id: 3,
    text: "Settlement payout batch ₹28,400 initiated to bank account",
    time: "12 mins ago",
    icon: DollarSign,
    color: "#22A06B",
  },
  {
    id: 4,
    text: "Return request submitted for Order #ORD-89218 (Damaged package)",
    time: "25 mins ago",
    icon: RotateCcw,
    color: "#D9534F",
  },
  {
    id: 5,
    text: "Whiskas Cat Food (7kg) stock updated (+50 units added)",
    time: "42 mins ago",
    icon: Boxes,
    color: "#2A9D8F",
  },
];

export default function CommandCentre({
  userRole = "VENDOR",
  vendorName = "Chandran Retailers Ltd",
}) {
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [countdown, setCountdown] = useState(30);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [dateFilter, setDateFilter] = useState("today");
  const [activeTab, setActiveTab] = useState("all");

  const isVendor = userRole === "VENDOR";
  const tableRef = useRef(null);

  const LIVE_METRICS = [
    {
      title: "Today's Gross Sales",
      value: isVendor ? "₹38,240" : "₹68,450",
      subtext: "vs yesterday ₹29,100",
      icon: DollarSign,
      color: "#176B5B",
    },
    {
      title: "Live Orders Today",
      value: isVendor ? "48" : "142",
      subtext: "6 in last hour",
      icon: Package,
      color: "#2A9D8F",
    },
    {
      title: "Pending Dispatch",
      value: isVendor ? "9" : "29",
      subtext: "2 urgent SLA (<2h)",
      icon: Clock,
      color: "#F4B942",
    },
    {
      title: "Shipments In-Transit",
      value: isVendor ? "24" : "86",
      subtext: "98.2% on-time delivery",
      icon: Truck,
      color: "#4A90A4",
    },
    {
      title: "Returns Awaiting Action",
      value: isVendor ? "1" : "7",
      subtext: "Disputes pending",
      icon: RotateCcw,
      color: "#D9534F",
    },
    {
      title: "Low Stock Alert",
      value: isVendor ? "2 SKUs" : "9 SKUs",
      subtext: "1 out of stock",
      icon: Flame,
      color: "#D9534F",
    },
    {
      title: "Unsettled Payout",
      value: isVendor ? "₹28,400" : "₹42,300",
      subtext: "Next payout: Tomorrow",
      icon: DollarSign,
      color: "#176B5B",
    },
    {
      title: "Dispatch SLA Health",
      value: isVendor ? "98.1%" : "96.8%",
      subtext: "Target: 95.0%",
      icon: CheckCircle2,
      color: "#22A06B",
    },
  ];

  const ACTION_REQUIRED_ITEMS = isVendor
    ? [
        {
          id: "ACT-01",
          type: "CRITICAL",
          title: "2 Orders Pending Dispatch > 2 Hours",
          desc: "Delivery SLA may breach if not handed over to Delhivery by 06:30 PM",
          time: "18 mins ago",
          targetTab: "pending",
          buttonText: "Review My Orders",
        },
        {
          id: "ACT-02",
          type: "WARNING",
          title: "Stockout Alert: Royal Canin Maxi (15kg)",
          desc: "Inventory reached 0 units while 6 open orders are in queue",
          time: "34 mins ago",
          targetSection: "inventory-section",
          buttonText: "Update Stock",
        },
      ]
    : [
        {
          id: "ACT-01",
          type: "CRITICAL",
          title: "8 Orders Pending Dispatch > 2 Hours (Multi-Vendor)",
          desc: "Delivery SLA may breach if not handed over to couriers by 06:30 PM",
          time: "18 mins ago",
          targetTab: "pending",
          buttonText: "Review Orders",
        },
        {
          id: "ACT-02",
          type: "WARNING",
          title: "Stockout Alert across 3 Vendors",
          desc: "Multiple high-velocity items reached 0 units",
          time: "34 mins ago",
          targetSection: "inventory-section",
          buttonText: "Review Stockouts",
        },
        {
          id: "ACT-03",
          type: "ATTENTION",
          title: "Return Dispute: Order #ORD-89410",
          desc: "Customer reported damaged seal on BlueCross tablet",
          time: "1 hour ago",
          targetSection: "returns-section",
          buttonText: "Inspect Dispute",
        },
      ];

  const ORDER_PIPELINE = [
    {
      stage: "New Orders",
      count: isVendor ? 8 : 24,
      status: "Awaiting Confirmation",
      color: "#4A90A4",
    },
    {
      stage: "Packed / Ready",
      count: isVendor ? 12 : 38,
      status: "Awaiting Courier Pickup",
      color: "#F4B942",
    },
    {
      stage: "In-Transit",
      count: isVendor ? 24 : 86,
      status: "On-Route Delivery",
      color: "#2A9D8F",
    },
    {
      stage: "Delivered Today",
      count: isVendor ? 18 : 68,
      status: "Completed Orders",
      color: "#22A06B",
    },
  ];

  const filteredOrders = ALL_OPERATIONAL_ORDERS.filter((order) => {
    const matchesVendor = isVendor ? order.vendor === vendorName : true;
    const matchesSearch =
      order.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.customer.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.courier.toLowerCase().includes(searchQuery.toLowerCase());

    if (activeTab === "pending")
      return (
        matchesVendor &&
        matchesSearch &&
        (order.status === "New" ||
          order.status === "Processing" ||
          order.status === "Ready to Dispatch")
      );
    if (activeTab === "transit")
      return matchesVendor && matchesSearch && order.status === "In-Transit";
    return matchesVendor && matchesSearch;
  });

  const filteredInventory = ALL_INVENTORY_HEALTH.filter((inv) =>
    isVendor ? inv.vendor === vendorName : true,
  );

  const filteredReturns = ALL_RETURNS.filter((ret) =>
    isVendor ? ret.vendor === vendorName : true,
  );

  useEffect(() => {
    if (!autoRefresh) return;
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          triggerRefresh();
          return 30;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [autoRefresh]);

  const triggerRefresh = () => {
    setIsRefreshing(true);
    setTimeout(() => {
      setIsRefreshing(false);
      setCountdown(30);
    }, 600);
  };

  const handleActionClick = (item) => {
    if (item.targetTab) {
      setActiveTab(item.targetTab);
      tableRef.current?.scrollIntoView({ behavior: "smooth" });
    } else if (item.targetSection) {
      const section = document.getElementById(item.targetSection);
      section?.scrollIntoView({ behavior: "smooth" });
    }
  };

  return (
    <div className="command-centre-container">
      <header className="command-header">
        <div className="command-title-wrap">
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "8px",
              marginBottom: "6px",
            }}
          >
            <div className="live-status-indicator">
              <span className="live-pulse"></span>
              <Radio size={16} className="text-emerald" />
              <span className="live-text">OPERATIONS COMMAND CENTRE</span>
            </div>

            <span
              style={{
                fontSize: "11px",
                fontWeight: "700",
                padding: "2px 8px",
                borderRadius: "9999px",
                backgroundColor: isVendor ? "var(--primary-light)" : "#e0e7ff",
                color: isVendor ? "var(--primary)" : "#4338ca",
                display: "inline-flex",
                alignItems: "center",
                gap: "4px",
              }}
            >
              {isVendor ? (
                <>
                  <Lock size={11} /> Authorized Scope: {vendorName}
                </>
              ) : (
                "Super Admin: Global Marketplace View"
              )}
            </span>
          </div>

          <h1>Central Operations Dashboard</h1>
          <p>
            {isVendor
              ? `Real-time operational monitoring, delivery SLAs & live triage scoped strictly to ${vendorName}.`
              : "Marketplace-wide operations monitoring, logistics SLA adherence & cross-vendor fulfillment tracking."}
          </p>
        </div>

        <div className="command-controls">
          <div className="refresh-control-box">
            <button
              type="button"
              className={`auto-refresh-toggle ${autoRefresh ? "active" : ""}`}
              onClick={() => setAutoRefresh(!autoRefresh)}
              title="Toggle automatic data refresh"
            >
              <span className="refresh-slider" />
            </button>
            <div className="refresh-meta">
              <span className="refresh-title">
                Auto Refresh: {autoRefresh ? "ON" : "OFF"}
              </span>
              <span className="refresh-sub">
                {autoRefresh ? `Next in ${countdown}s` : "Manual"}
              </span>
            </div>
            <button
              type="button"
              className={`manual-refresh-btn ${isRefreshing ? "spin" : ""}`}
              onClick={triggerRefresh}
              title="Force Refresh Data"
            >
              <RefreshCw size={14} />
            </button>
          </div>

          <select
            value={dateFilter}
            onChange={(e) => setDateFilter(e.target.value)}
            className="command-select"
          >
            <option value="today">Today (Live)</option>
            <option value="yesterday">Yesterday</option>
            <option value="24h">Last 24 Hours</option>
            <option value="week">This Week</option>
          </select>
        </div>
      </header>

      <div className="global-command-search">
        <Search size={16} className="search-lead-icon" />
        <input
          type="text"
          placeholder={
            isVendor
              ? `Search active orders or items in ${vendorName}...`
              : "Global search: Enter Order ID, Customer, Courier AWB, SKU across all vendors..."
          }
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="global-search-input"
        />
      </div>

      <section className="alerts-tray-section">
        <div className="alerts-tray-header">
          <div className="alerts-tray-title">
            <ShieldAlert size={18} className="text-danger" />
            <h3>Action Required Items</h3>
            <span className="alert-count-pill">
              {ACTION_REQUIRED_ITEMS.length} Urgent
            </span>
          </div>
          <span className="alerts-subtitle">
            {isVendor
              ? "Issues requiring your immediate store intervention"
              : "Platform-wide critical operations requiring review"}
          </span>
        </div>

        <div className="alerts-grid">
          {ACTION_REQUIRED_ITEMS.map((item) => (
            <div key={item.id} className="alert-box-card">
              <div className="alert-box-top">
                <span className={`alert-type-badge ${item.type.toLowerCase()}`}>
                  {item.type}
                </span>
                <span className="alert-time">{item.time}</span>
              </div>
              <h4 className="alert-box-title">{item.title}</h4>
              <p className="alert-box-desc">{item.desc}</p>
              <button
                type="button"
                className="alert-action-btn"
                onClick={() => handleActionClick(item)}
              >
                <span>{item.buttonText}</span>
                <ChevronRight size={14} />
              </button>
            </div>
          ))}
        </div>
      </section>

      <section className="operational-kpis-grid">
        {LIVE_METRICS.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div key={idx} className="ops-kpi-card">
              <div className="ops-kpi-top">
                <span className="ops-kpi-title">{kpi.title}</span>
                <div
                  className="ops-kpi-icon"
                  style={{
                    backgroundColor: `${kpi.color}15`,
                    color: kpi.color,
                  }}
                >
                  <Icon size={16} />
                </div>
              </div>
              <div className="ops-kpi-main">
                <h3 className="ops-kpi-val">{kpi.value}</h3>
                <span className="ops-kpi-sub">{kpi.subtext}</span>
              </div>
            </div>
          );
        })}
      </section>

      <section className="monitoring-section">
        <div className="section-head-simple">
          <h3>Order Monitoring: Active Lifecycle Pipeline</h3>
          <span>
            {isVendor
              ? `Live progress of open orders for ${vendorName}`
              : "Marketplace-wide order progress across fulfillment stages"}
          </span>
        </div>

        <div className="pipeline-stages-row">
          {ORDER_PIPELINE.map((pipe, idx) => (
            <div key={idx} className="pipeline-stage-card">
              <div
                className="stage-accent-bar"
                style={{ backgroundColor: pipe.color }}
              />
              <div className="stage-content">
                <span className="stage-name">{pipe.stage}</span>
                <h2 className="stage-count">{pipe.count}</h2>
                <span
                  className="stage-badge"
                  style={{
                    color: pipe.color,
                    backgroundColor: `${pipe.color}18`,
                  }}
                >
                  {pipe.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </section>

      <div className="two-col-grid">
        <section className="monitoring-card">
          <div className="card-head-flex">
            <div>
              <h3>Delivery Monitoring (Carrier Performance)</h3>
              <p>Tracking logistics SLA adherence and delivery exceptions</p>
            </div>
            <Truck size={18} className="text-secondary" />
          </div>

          <div className="delivery-carriers-list">
            {DELIVERY_MONITORING.map((del, i) => (
              <div key={i} className="carrier-row">
                <div className="carrier-info">
                  <strong>{del.carrier}</strong>
                  <span>
                    {del.activeShipments} Active Shipments • Avg{" "}
                    {del.avgTransit}
                  </span>
                </div>
                <div className="carrier-metrics">
                  <span className="carrier-rate">SLA: {del.onTimeRate}</span>
                  <span
                    className={`carrier-status-tag ${del.status === "Optimal" ? "tag-green" : "tag-amber"}`}
                  >
                    {del.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="monitoring-card">
          <div className="card-head-flex">
            <div>
              <h3>
                {isVendor
                  ? `${vendorName} Settlement Health`
                  : "Marketplace Settlement & Performance"}
              </h3>
              <p>Fulfillment score, dispatch speed & pending payouts</p>
            </div>
            <Award size={18} className="text-primary" />
          </div>

          <div className="vendor-score-grid">
            <div className="score-box">
              <span className="score-label">Fulfillment Rate</span>
              <h3 className="score-val text-success">
                {isVendor ? "98.8%" : "98.4%"}
              </h3>
              <span className="score-hint">Top tier ranking</span>
            </div>
            <div className="score-box">
              <span className="score-label">Avg Dispatch Time</span>
              <h3 className="score-val text-primary">
                {isVendor ? "1.9 Hours" : "2.1 Hours"}
              </h3>
              <span className="score-hint">Within SLA benchmark</span>
            </div>
            <div className="score-box">
              <span className="score-label">Available Payout</span>
              <h3 className="score-val text-primary">
                {isVendor ? "₹28,400" : "₹42,300"}
              </h3>
              <span className="score-hint">Scheduled: Tomorrow 11 AM</span>
            </div>
            <div className="score-box">
              <span className="score-label">Commission Deducted</span>
              <h3 className="score-val text-muted">
                {isVendor ? "₹2,272" : "₹3,410"}
              </h3>
              <span className="score-hint">8% Platform fee rate</span>
            </div>
          </div>
        </section>
      </div>

      <div className="two-col-grid" style={{ marginTop: "20px" }}>
        <section id="inventory-section" className="monitoring-card">
          <div className="card-head-flex">
            <div>
              <h3>Inventory Health & Stockout Radar</h3>
              <p>
                {isVendor
                  ? `Your store catalog SKUs needing restocking`
                  : "Platform SKUs requiring urgent restocking"}
              </p>
            </div>
            <Boxes size={18} className="text-danger" />
          </div>

          <div className="inventory-list-table">
            {filteredInventory.map((inv, i) => (
              <div key={i} className="inv-row">
                <div className="inv-title">
                  <strong>{inv.name}</strong>
                  <span>
                    SKU: {inv.sku} {!isVendor && `• ${inv.vendor}`}
                  </span>
                </div>
                <div className="inv-stock">
                  <span>Stock: {inv.currentStock} units</span>
                  <span
                    className={`inv-tag ${inv.status === "Out of Stock" ? "inv-red" : inv.status === "Critical Low" ? "inv-amber" : "inv-green"}`}
                  >
                    {inv.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section id="returns-section" className="monitoring-card">
          <div className="card-head-flex">
            <div>
              <h3>Returns & Refund Monitoring</h3>
              <p>
                {isVendor
                  ? `Returns filed against ${vendorName}`
                  : "Cross-vendor reverse logistics and disputes"}
              </p>
            </div>
            <RotateCcw size={18} className="text-danger" />
          </div>

          <div className="returns-list-table">
            {filteredReturns.length === 0 ? (
              <p
                style={{ fontSize: "12px", color: "#718096", padding: "10px" }}
              >
                No pending returns for your store.
              </p>
            ) : (
              filteredReturns.map((ret, i) => (
                <div key={i} className="return-row">
                  <div className="return-details">
                    <strong>
                      {ret.orderId} • {ret.item}
                    </strong>
                    <span>Reason: {ret.reason}</span>
                  </div>
                  <div className="return-status-wrap">
                    <span
                      className={`return-badge ${ret.actionRequired ? "ret-amber" : "ret-blue"}`}
                    >
                      {ret.status}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </section>
      </div>

      <div
        className="command-split-layout"
        ref={tableRef}
        style={{ marginTop: "24px" }}
      >
        <section className="command-table-card">
          <div className="table-header-tabs">
            <div>
              <h3>
                {isVendor
                  ? `${vendorName} Active Orders`
                  : "Marketplace Active Orders Triage"}
              </h3>
              <p>
                Live order processing, fulfillment deadlines & shipment tracking
              </p>
            </div>

            <div className="tab-pills-row">
              <button
                type="button"
                className={`tab-pill ${activeTab === "all" ? "active" : ""}`}
                onClick={() => setActiveTab("all")}
              >
                All Orders ({filteredOrders.length})
              </button>
              <button
                type="button"
                className={`tab-pill ${activeTab === "pending" ? "active" : ""}`}
                onClick={() => setActiveTab("pending")}
              >
                Pending Dispatch
              </button>
              <button
                type="button"
                className={`tab-pill ${activeTab === "transit" ? "active" : ""}`}
                onClick={() => setActiveTab("transit")}
              >
                In-Transit
              </button>
            </div>
          </div>

          <div className="table-responsive">
            <table className="command-table">
              <thead>
                <tr>
                  <th>Order Reference</th>
                  <th>Customer</th>
                  <th>Items</th>
                  <th>Value</th>
                  <th>Courier / SLA Status</th>
                  <th>Current State</th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.map((ord) => (
                  <tr key={ord.id}>
                    <td>
                      <span className="order-id-code">{ord.id}</span>
                    </td>
                    <td>
                      <span className="customer-name">{ord.customer}</span>
                    </td>
                    <td>
                      <span className="order-items-snippet">{ord.items}</span>
                    </td>
                    <td className="font-bold text-primary">{ord.amount}</td>
                    <td>
                      <div className="courier-sla-cell">
                        <span className="courier-name">{ord.courier}</span>
                        <span
                          className={`sla-badge ${ord.sla.includes("Overdue") ? "sla-breach" : "sla-ok"}`}
                        >
                          {ord.sla}
                        </span>
                      </div>
                    </td>
                    <td>
                      <span
                        className={`state-badge state-${ord.status.toLowerCase().replace(/\s+/g, "-")}`}
                      >
                        {ord.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <aside className="activity-feed-card">
          <div className="feed-header">
            <div className="feed-title-wrap">
              <Activity size={18} className="text-primary" />
              <h3>Live Operations Feed</h3>
            </div>
            <span className="live-stream-tag">STREAM</span>
          </div>

          <div className="feed-items-list">
            {INITIAL_ACTIVITY_FEED.map((feed) => {
              const Icon = feed.icon;
              return (
                <div key={feed.id} className="feed-item-row">
                  <div
                    className="feed-icon-circle"
                    style={{
                      backgroundColor: `${feed.color}15`,
                      color: feed.color,
                    }}
                  >
                    <Icon size={14} />
                  </div>
                  <div className="feed-body">
                    <p className="feed-text">{feed.text}</p>
                    <span className="feed-time">{feed.time}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </aside>
      </div>
    </div>
  );
}
