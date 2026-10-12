import { useState, useMemo, useEffect } from "react";
import {
  TrendingUp,
  TrendingDown,
  Calendar,
  Download,
  DollarSign,
  ShoppingBag,
  Package,
  Users,
  Percent,
  RotateCcw,
  Truck,
  Award,
  Search,
  ArrowUpDown,
  FileSpreadsheet,
  FileText,
  Building2,
  Lock,
} from "lucide-react";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from "recharts";
import "./BIDashboard.css";
import { apiFetch, buildQuery } from "../api";

const REVENUE_DATA = [
  { month: "Jan", revenue: 42000, profit: 12500, orders: 320 },
  { month: "Feb", revenue: 53000, profit: 16200, orders: 410 },
  { month: "Mar", revenue: 61000, profit: 19800, orders: 480 },
  { month: "Apr", revenue: 58000, profit: 17400, orders: 450 },
  { month: "May", revenue: 74000, profit: 24100, orders: 590 },
  { month: "Jun", revenue: 89000, profit: 29500, orders: 720 },
  { month: "Jul", revenue: 95000, profit: 32000, orders: 780 },
];

const CATEGORY_DATA = [
  { name: "Pet Food", value: 42, color: "#176B5B" },
  { name: "Medicines", value: 28, color: "#2A9D8F" },
  { name: "Accessories", value: 18, color: "#4A90A4" },
  { name: "Grooming", value: 12, color: "#F4B942" },
];

const ORDER_STATUS_DATA = [
  { status: "Delivered", count: 620, color: "#22A06B" },
  { status: "In-Transit", count: 95, color: "#4A90A4" },
  { status: "Pending", count: 48, color: "#F4B942" },
  { status: "Returned", count: 24, color: "#D9534F" },
];

const ALL_PRODUCTS = [
  {
    id: "PRD-01",
    name: "Royal Canin Maxi Adult Dog Food (15kg)",
    category: "Pet Food",
    vendor: "Chandran Retailers Ltd",
    sold: 482,
    revenue: 168700,
    stock: 45,
    status: "In Stock",
  },
  {
    id: "PRD-02",
    name: "Bravecto Chewable Tick & Flea Tablet",
    category: "Medicines",
    vendor: "BlueCross Animal Pharma",
    sold: 395,
    revenue: 98750,
    stock: 12,
    status: "Low Stock",
  },
  {
    id: "PRD-03",
    name: "Whiskas Ocean Fish Adult Cat Food (7kg)",
    category: "Pet Food",
    vendor: "Chandran Retailers Ltd",
    sold: 340,
    revenue: 74800,
    stock: 82,
    status: "In Stock",
  },
  {
    id: "PRD-04",
    name: "Pet Care Orthopedic Memory Foam Bed",
    category: "Accessories",
    vendor: "Zenve Companion Supplies",
    sold: 210,
    revenue: 52500,
    stock: 0,
    status: "Out of Stock",
  },
  {
    id: "PRD-05",
    name: "Himalaya Erina Plus Coat Cleanser Shampoo",
    category: "Grooming",
    vendor: "PetVitals Co.",
    sold: 188,
    revenue: 37600,
    stock: 64,
    status: "In Stock",
  },
  {
    id: "PRD-06",
    name: "Pedigree Dentastix Daily Dental Chews",
    category: "Pet Food",
    vendor: "Chandran Retailers Ltd",
    sold: 165,
    revenue: 24750,
    stock: 30,
    status: "In Stock",
  },
];

const RANGE_SCALE = { today: 0.04, "7d": 0.25, "30d": 1, "90d": 2.8, ytd: 5.5 };

const TREND_SHAPES = {
  today: {
    labels: ["9 AM", "12 PM", "3 PM", "6 PM", "9 PM"],
    weights: [0.12, 0.22, 0.24, 0.26, 0.16],
  },
  "7d": {
    labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
    weights: [0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.13],
  },
  "30d": {
    labels: ["Week 1", "Week 2", "Week 3", "Week 4"],
    weights: [0.22, 0.25, 0.26, 0.27],
  },
};

const KPI_ICON_MAP = {
  grossRevenue: DollarSign,
  netProfit: TrendingUp,
  totalSalesCount: ShoppingBag,
  totalOrders: Package,
  activeProducts: Package,
  totalCustomers: Users,
  commissionDeducted: Percent,
  deliverySuccessRate: Truck,
  returnRate: RotateCcw,
  avgOrderValue: Award,
  pendingSettlement: DollarSign,
  cancelledOrders: TrendingDown,
};

const KPI_COLORS = [
  "#176B5B",
  "#22A06B",
  "#2A9D8F",
  "#176B5B",
  "#4A90A4",
  "#2A9D8F",
  "#718096",
  "#22A06B",
  "#F4B942",
  "#176B5B",
  "#4A90A4",
  "#D9534F",
];

const CHART_COLORS = ["#176B5B", "#2A9D8F", "#4A90A4", "#F4B942", "#D9534F"];

const formatCurrency = (value) =>
  `₹${Number(value || 0).toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;

const formatKpiValue = (kpi) => {
  const value = Number(kpi.value || 0);
  if (kpi.format === "CURRENCY") return formatCurrency(value);
  if (kpi.format === "PERCENT") return `${value.toFixed(value % 1 ? 1 : 0)}%`;
  return value.toLocaleString("en-IN", { maximumFractionDigits: 0 });
};

const formatChange = (kpi) => {
  if (kpi.changePercent === null || kpi.changePercent === undefined) {
    return kpi.note || "";
  }
  const sign = kpi.changePercent > 0 ? "+" : "";
  return `${sign}${Number(kpi.changePercent).toFixed(1)}%`;
};

export default function BIDashboard({
  token,
  userRole = "VENDOR",
  vendorName = "Chandran Retailers Ltd",
}) {
  const [dateRange, setDateRange] = useState("30d");
  const [selectedVendor, setSelectedVendor] = useState(
    userRole === "VENDOR" ? vendorName : "All",
  );
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [selectedStatus, setSelectedStatus] = useState("All");
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [dashboardData, setDashboardData] = useState(null);
  const [productsData, setProductsData] = useState(null);
  const [filterOptions, setFilterOptions] = useState({ categories: [], vendors: [] });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const isVendor = userRole === "VENDOR";
  const scale = RANGE_SCALE[dateRange] ?? 1;

  const money = (n) => `₹${Math.round(n * scale).toLocaleString("en-IN")}`;
  const count = (n, min = 0) =>
    Math.max(min, Math.round(n * scale)).toLocaleString("en-IN");

  const KPI_METRICS = [
    {
      title: "Gross Revenue",
      value: money(isVendor ? 268250 : 472000),
      change: "+18.4%",
      isPositive: true,
      icon: DollarSign,
      color: "#176B5B",
    },
    {
      title: "Net Profit",
      value: money(isVendor ? 85840 : 151500),
      change: "+14.2%",
      isPositive: true,
      icon: TrendingUp,
      color: "#22A06B",
    },
    {
      title: "Total Sales Count",
      value: count(isVendor ? 987 : 3840),
      change: "+9.8%",
      isPositive: true,
      icon: ShoppingBag,
      color: "#2A9D8F",
    },
    {
      title: "Total Orders",
      value: count(isVendor ? 812 : 3024),
      change: "+12.1%",
      isPositive: true,
      icon: Package,
      color: "#176B5B",
    },
    {
      title: "Active Products",
      value: isVendor ? "3" : "248",
      change: "+4.0%",
      isPositive: true,
      icon: Package,
      color: "#4A90A4",
    },
    {
      title: "Total Customers",
      value: count(isVendor ? 540 : 1890),
      change: "+16.5%",
      isPositive: true,
      icon: Users,
      color: "#2A9D8F",
    },
    {
      title: "Commission Deducted",
      value: money(isVendor ? 21460 : 37760),
      change: "8% rate",
      isPositive: false,
      icon: Percent,
      color: "#718096",
    },
    {
      title: "Delivery Success Rate",
      value: "97.2%",
      change: "+1.2%",
      isPositive: true,
      icon: Truck,
      color: "#22A06B",
    },
    {
      title: "Return Rate",
      value: "2.4%",
      change: "-0.4%",
      isPositive: true,
      icon: RotateCcw,
      color: "#F4B942",
    },
    {
      title: "Avg Order Value",
      value: "₹1,140",
      change: "+5.6%",
      isPositive: true,
      icon: Award,
      color: "#176B5B",
    },
    {
      title: "Pending Settlement",
      value: isVendor ? "₹28,400" : "₹42,300",
      change: "Tomorrow",
      isPositive: true,
      icon: DollarSign,
      color: "#4A90A4",
    },
    {
      title: "Cancelled Orders",
      value: count(isVendor ? 11 : 38, 1),
      change: "-12.0%",
      isPositive: true,
      icon: TrendingDown,
      color: "#D9534F",
    },
  ];

  useEffect(() => {
    let ignore = false;

    const loadDashboard = async () => {
      if (!token) return;
      setError("");
      setLoading(true);

      try {
        const [dashboard, filters] = await Promise.all([
          apiFetch(`/api/analytics/dashboard${buildQuery({ range: dateRange })}`, {
            token,
          }),
          apiFetch("/api/analytics/filters", { token }),
        ]);

        if (!ignore) {
          setDashboardData(dashboard);
          setFilterOptions({
            categories: filters?.categories || [],
            vendors: filters?.vendors || [],
          });
        }
      } catch (err) {
        if (!ignore) setError(err.message);
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    loadDashboard();
    return () => {
      ignore = true;
    };
  }, [token, dateRange]);

  useEffect(() => {
    let ignore = false;

    const loadProducts = async () => {
      if (!token) return;
      setError("");

      try {
        const selectedVendorId =
          !isVendor && selectedVendor !== "All"
            ? filterOptions.vendors.find((vendor) => vendor.name === selectedVendor)?.vendorId
            : undefined;

        const products = await apiFetch(
          `/api/analytics/products${buildQuery({
            range: dateRange,
            vendorId: selectedVendorId,
            category: selectedCategory,
            stockStatus: selectedStatus,
            search: searchQuery,
            page: currentPage,
            size: 10,
            sortBy: "revenue",
            sortDir: "desc",
          })}`,
          { token },
        );

        if (!ignore) setProductsData(products);
      } catch (err) {
        if (!ignore) setError(err.message);
      }
    };

    loadProducts();
    return () => {
      ignore = true;
    };
  }, [
    token,
    dateRange,
    selectedVendor,
    selectedCategory,
    selectedStatus,
    searchQuery,
    currentPage,
    isVendor,
    filterOptions.vendors,
  ]);

  const backendProducts = (productsData?.content || []).map((p) => ({
    ...p,
    revenue: Number(p.revenue || 0),
  }));

  const filteredProducts = productsData
    ? backendProducts
    : ALL_PRODUCTS.map((p) => ({
        ...p,
        sold: Math.round(p.sold * scale),
        revenue: Math.round(p.revenue * scale),
      })).filter((p) => {
    const matchesVendor = isVendor
      ? p.vendor === vendorName
      : selectedVendor === "All" || p.vendor === selectedVendor;

    const matchesSearch =
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.vendor.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesCat =
      selectedCategory === "All" || p.category === selectedCategory;
    const matchesStatus =
      selectedStatus === "All" || p.status === selectedStatus;

    return matchesVendor && matchesSearch && matchesCat && matchesStatus;
  });

  const trendData = useMemo(() => {
    if (dashboardData?.trend?.length) {
      return dashboardData.trend.map((point) => ({
        month: point.label,
        revenue: Number(point.revenue || 0),
        profit: Number(point.profit || 0),
        orders: Number(point.orders || 0),
      }));
    }
    if (dateRange === "ytd") return REVENUE_DATA;
    if (dateRange === "90d") return REVENUE_DATA.slice(-3);
    const shape = TREND_SHAPES[dateRange] || TREND_SHAPES["30d"];
    const latest = REVENUE_DATA[REVENUE_DATA.length - 1];
    const factor = RANGE_SCALE[dateRange] ?? 1;
    return shape.labels.map((label, i) => ({
      month: label,
      revenue: Math.round(latest.revenue * factor * shape.weights[i]),
      profit: Math.round(latest.profit * factor * shape.weights[i]),
    }));
  }, [dateRange, dashboardData]);

  const categoryData =
    dashboardData?.categoryShare?.length
      ? dashboardData.categoryShare.map((cat, index) => ({
          name: cat.name,
          value: Number(cat.value || 0),
          color: CHART_COLORS[index % CHART_COLORS.length],
        }))
      : CATEGORY_DATA;

  const statusData =
    dashboardData?.orderStatus?.length
      ? dashboardData.orderStatus.map((s, index) => ({
          status: s.status,
          count: Number(s.count || 0),
          color: CHART_COLORS[index % CHART_COLORS.length],
        }))
      : ORDER_STATUS_DATA.map((s) => ({
          ...s,
          count: Math.max(1, Math.round(s.count * scale)),
        }));

  const backendKpis =
    dashboardData?.kpis?.map((kpi, index) => ({
      title: kpi.title,
      value: formatKpiValue(kpi),
      change: formatChange(kpi),
      isPositive:
        kpi.changePercent === null || kpi.changePercent === undefined
          ? kpi.positiveIsGood
          : kpi.positiveIsGood
            ? kpi.changePercent >= 0
            : kpi.changePercent <= 0,
      icon: KPI_ICON_MAP[kpi.key] || TrendingUp,
      color: KPI_COLORS[index % KPI_COLORS.length],
    })) || [];

  const displayedKpis = backendKpis.length ? backendKpis : KPI_METRICS;

  const handleExportCSV = () => {
    const headers = [
      "Product ID,Product Name,Category,Vendor,Units Sold,Revenue (INR),Stock,Status\n",
    ];
    const rows = filteredProducts
      .map(
        (p) =>
          `"${p.id}","${p.name}","${p.category}","${p.vendor}",${p.sold},${p.revenue},${p.stock},"${p.status}"`,
      )
      .join("\n");

    const blob = new Blob([headers + rows], {
      type: "text/csv;charset=utf-8;",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `${isVendor ? "Vendor" : "Admin"}_Analytics_${new Date().toISOString().slice(0, 10)}.csv`,
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleExportExcel = () => {
    const excelContent = `
      <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
      <head><meta charset="utf-8" /></head>
      <body>
        <table border="1">
          <thead>
            <tr style="background-color: #176B5B; color: #ffffff; font-weight: bold;">
              <th>Product ID</th><th>Product Name</th><th>Category</th><th>Vendor</th><th>Units Sold</th><th>Revenue (INR)</th><th>Stock</th><th>Status</th>
            </tr>
          </thead>
          <tbody>
            ${filteredProducts
              .map(
                (p) => `
              <tr>
                <td>${p.id}</td><td>${p.name}</td><td>${p.category}</td><td>${p.vendor}</td><td>${p.sold}</td><td>${p.revenue}</td><td>${p.stock}</td><td>${p.status}</td>
              </tr>
            `,
              )
              .join("")}
          </tbody>
        </table>
      </body></html>
    `;
    const blob = new Blob([excelContent], { type: "application/vnd.ms-excel" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      `${isVendor ? "Vendor" : "Admin"}_Report_${new Date().toISOString().slice(0, 10)}.xls`,
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const totalPages = productsData?.totalPages || 1;

  return (
    <div className="bi-dashboard-container">
      {/* Header */}
      <header className="bi-header">
        <div>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "8px",
              marginBottom: "4px",
            }}
          >
            <h1 className="bi-title">BI Analytics Dashboard</h1>
            <span
              style={{
                fontSize: "11px",
                fontWeight: "700",
                padding: "2px 8px",
                borderRadius: "9999px",
                backgroundColor: isVendor ? "var(--primary-light)" : "#e0e7ff",
                color: isVendor ? "var(--primary)" : "#4338ca",
              }}
            >
              {isVendor
                ? `Authorized Vendor: ${vendorName}`
                : "Admin: Global View"}
            </span>
          </div>
          <p className="bi-subtitle">
            {isVendor
              ? "Showing authorized store analytics and financial reports for your vendor account."
              : "Global commercial intelligence across all marketplace vendors and product categories."}
          </p>
        </div>

        <div className="bi-header-actions no-print">
          <div className="filter-select-wrap">
            <Calendar size={15} className="select-icon" />
            <select
              value={dateRange}
              onChange={(e) => setDateRange(e.target.value)}
              className="bi-select"
            >
              <option value="today">Today</option>
              <option value="7d">Last 7 Days</option>
              <option value="30d">Last 30 Days</option>
              <option value="90d">Last 3 Months</option>
              <option value="ytd">Year to Date (YTD)</option>
            </select>
          </div>

          <div className="export-btn-group">
            <button
              type="button"
              className="btn-export"
              onClick={handleExportCSV}
            >
              <Download size={13} />
              <span>CSV</span>
            </button>
            <button
              type="button"
              className="btn-export"
              onClick={handleExportExcel}
            >
              <FileSpreadsheet size={13} />
              <span>Excel</span>
            </button>
            <button
              type="button"
              className="btn-export btn-pdf"
              onClick={() => window.print()}
            >
              <FileText size={13} />
              <span>PDF Report</span>
            </button>
          </div>
        </div>
      </header>

      {error && (
        <div
          style={{
            marginBottom: "16px",
            padding: "10px 12px",
            borderRadius: "8px",
            background: "#fef2f2",
            color: "#b91c1c",
            border: "1px solid #fecaca",
            fontSize: "13px",
            fontWeight: 700,
          }}
        >
          {error}
        </div>
      )}

      <section className="kpi-grid">
        {displayedKpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div key={idx} className="kpi-card">
              <div className="kpi-top">
                <span className="kpi-title">{kpi.title}</span>
                <div
                  className="kpi-icon-pill"
                  style={{
                    backgroundColor: `${kpi.color}15`,
                    color: kpi.color,
                  }}
                >
                  <Icon size={16} />
                </div>
              </div>
              <div className="kpi-bottom">
                <h3 className="kpi-value">{kpi.value}</h3>
                <span
                  className={`kpi-trend ${kpi.isPositive ? "trend-up" : "trend-down"}`}
                >
                  {kpi.isPositive ? (
                    <TrendingUp size={12} />
                  ) : (
                    <TrendingDown size={12} />
                  )}
                  {kpi.change}
                </span>
              </div>
            </div>
          );
        })}
      </section>

      <section className="charts-grid-layout">
        <div className="chart-card chart-large">
          <div className="chart-header">
            <div>
              <h3>Revenue & Profit Performance</h3>
              <p>Historical comparison of gross intake vs net profit</p>
            </div>
            <span className="chart-badge">
              {loading ? "Syncing..." : "Live Aggregations"}
            </span>
          </div>
          <div className="chart-wrapper">
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart
                data={trendData}
                margin={{ top: 10, right: 20, left: 0, bottom: 0 }}
              >
                <defs>
                  <linearGradient id="colorRev" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#176B5B" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#176B5B" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="colorProf" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#2A9D8F" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#2A9D8F" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="#E5EAE8"
                  vertical={false}
                />
                <XAxis
                  dataKey="month"
                  stroke="#718096"
                  fontSize={12}
                  tickLine={false}
                />
                <YAxis
                  stroke="#718096"
                  fontSize={12}
                  tickLine={false}
                  tickFormatter={(val) => `₹${val / 1000}k`}
                />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#ffffff",
                    borderRadius: "8px",
                    border: "1px solid #E5EAE8",
                  }}
                  formatter={(value) => [formatCurrency(value), ""]}
                />
                <Legend iconType="circle" />
                <Area
                  type="monotone"
                  dataKey="revenue"
                  name="Gross Revenue"
                  stroke="#176B5B"
                  strokeWidth={2.5}
                  fillOpacity={1}
                  fill="url(#colorRev)"
                />
                <Area
                  type="monotone"
                  dataKey="profit"
                  name="Net Profit"
                  stroke="#2A9D8F"
                  strokeWidth={2}
                  fillOpacity={1}
                  fill="url(#colorProf)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="chart-card chart-small">
          <div className="chart-header">
            <div>
              <h3>Sales by Category</h3>
              <p>Volume contribution percentage</p>
            </div>
          </div>
          <div className="chart-wrapper donut-center-wrap">
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  innerRadius={65}
                  outerRadius={95}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {categoryData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => [`${value}%`, "Share"]} />
              </PieChart>
            </ResponsiveContainer>
            <div className="donut-legend">
              {categoryData.map((cat, idx) => (
                <div key={idx} className="legend-row">
                  <div
                    className="legend-indicator"
                    style={{ backgroundColor: cat.color }}
                  ></div>
                  <span className="legend-name">{cat.name}</span>
                  <span className="legend-value">{cat.value}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="chart-card chart-full">
          <div className="chart-header">
            <div>
              <h3>Fulfillment & Delivery Distribution</h3>
              <p>Current period orders grouped by completion lifecycle</p>
            </div>
          </div>
          <div className="chart-wrapper">
            <ResponsiveContainer width="100%" height={220}>
              <BarChart
                data={statusData}
                margin={{ top: 10, right: 20, left: 0, bottom: 0 }}
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="#E5EAE8"
                  vertical={false}
                />
                <XAxis
                  dataKey="status"
                  stroke="#718096"
                  fontSize={12}
                  tickLine={false}
                />
                <YAxis stroke="#718096" fontSize={12} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#ffffff",
                    borderRadius: "8px",
                    border: "1px solid #E5EAE8",
                  }}
                />
                <Bar dataKey="count" name="Total Orders" radius={[6, 6, 0, 0]}>
                  {statusData.map((entry, index) => (
                    <Cell key={`bar-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </section>

      <section className="analytics-table-card">
        <div className="table-header-flex">
          <div>
            <h3>
              {isVendor
                ? "My Store Products Analytics"
                : "Marketplace Products Analytics"}
            </h3>
            <p>
              {isVendor
                ? "Authorized performance view for your catalog only."
                : "Aggregated sales metrics across all marketplace sellers."}
            </p>
          </div>

          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "14px",
              flexWrap: "wrap",
            }}
          >
            <div className="filter-search-box" style={{ margin: 0 }}>
              <Search size={14} className="search-icon" />
              <input
                type="text"
                placeholder="Search products or SKU..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="search-input"
                style={{ width: "240px" }}
              />
            </div>

            <span className="records-count">
              Showing {filteredProducts.length} Products
            </span>
          </div>
        </div>

        <div className="bi-secondary-filters no-print">
          {isVendor ? (
            <div
              className="filter-item"
              style={{
                background: "var(--primary-light)",
                padding: "4px 10px",
                borderRadius: "6px",
              }}
            >
              <Lock size={13} style={{ color: "var(--primary)" }} />
              <span
                style={{
                  color: "var(--primary)",
                  fontSize: "11.5px",
                  fontWeight: "700",
                }}
              >
                Vendor Scope: {vendorName}
              </span>
            </div>
          ) : (
            <div className="filter-item">
              <Building2 size={14} className="text-primary" />
              <label>Vendor Filter:</label>
              <select
                value={selectedVendor}
                onChange={(e) => {
                  setSelectedVendor(e.target.value);
                  setCurrentPage(1);
                }}
                className="bi-mini-select"
              >
                <option value="All">
                  All Authorized Vendors (Marketplace)
                </option>
                {(filterOptions.vendors.length
                  ? filterOptions.vendors.map((vendor) => vendor.name)
                  : [
                      "Chandran Retailers Ltd",
                      "BlueCross Animal Pharma",
                      "Zenve Companion Supplies",
                      "PetVitals Co.",
                    ]
                ).map((vendor) => (
                  <option key={vendor} value={vendor}>
                    {vendor}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="filter-item">
            <label>Category:</label>
            <select
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setCurrentPage(1);
              }}
              className="bi-mini-select"
            >
              <option value="All">All Categories</option>
              {(filterOptions.categories.length
                ? filterOptions.categories
                : ["Pet Food", "Medicines", "Accessories", "Grooming"]
              ).map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-item">
            <label>Stock Status:</label>
            <select
              value={selectedStatus}
              onChange={(e) => {
                setSelectedStatus(e.target.value);
                setCurrentPage(1);
              }}
              className="bi-mini-select"
            >
              <option value="All">All Status</option>
              <option value="In Stock">In Stock</option>
              <option value="Low Stock">Low Stock</option>
              <option value="Out of Stock">Out of Stock</option>
            </select>
          </div>
        </div>

        <div className="table-responsive">
          <table className="bi-table">
            <thead>
              <tr>
                <th>
                  Product Identifier <ArrowUpDown size={12} />
                </th>
                <th>Category</th>
                <th>Vendor Store</th>
                <th>Units Sold</th>
                <th>Total Revenue</th>
                <th>Inventory Stock</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.length === 0 ? (
                <tr>
                  <td
                    colSpan="7"
                    style={{
                      textAlign: "center",
                      padding: "30px",
                      color: "#718096",
                    }}
                  >
                    No products found matching your search.
                  </td>
                </tr>
              ) : (
                filteredProducts.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <div className="product-title-cell">
                        <span className="product-name">{p.name}</span>
                        <span className="product-sku">{p.id}</span>
                      </div>
                    </td>
                    <td>
                      <span className="category-pill">{p.category}</span>
                    </td>
                    <td>
                      <span className="vendor-pill">{p.vendor}</span>
                    </td>
                    <td className="font-semibold">{p.sold.toLocaleString()}</td>
                    <td className="font-bold text-primary">
                      {formatCurrency(p.revenue)}
                    </td>
                    <td>{p.stock} units</td>
                    <td>
                      <span
                        className={`status-pill ${
                          p.status === "In Stock"
                            ? "status-green"
                            : p.status === "Low Stock"
                              ? "status-amber"
                              : "status-red"
                        }`}
                      >
                        {p.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="table-pagination-bar no-print">
          <span className="pagination-info">
            Page {productsData?.page || currentPage} of {totalPages}
          </span>
          <div className="pagination-buttons">
            <button
              type="button"
              disabled={currentPage <= 1}
              className="btn-page"
              onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
            >
              Previous
            </button>
            <button type="button" className="btn-page active">
              {productsData?.page || currentPage}
            </button>
            <button
              type="button"
              disabled={currentPage >= totalPages}
              className="btn-page"
              onClick={() =>
                setCurrentPage((page) => Math.min(totalPages, page + 1))
              }
            >
              Next
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
