import  { useEffect, useRef, useState } from "react";
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
  AlertCircle,
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


const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

// Login page save panna token (localStorage / sessionStorage)
function authHeaders() {
  try {
    const auth = JSON.parse(
      localStorage.getItem("auth") || sessionStorage.getItem("auth") || "null",
    );
    return auth?.token ? { Authorization: `Bearer ${auth.token}` } : {};
  } catch {
    return {};
  }
}

// success -> JSON, error -> Error(status) throw (401 na logout)
async function checkResponse(res) {
  if (res.ok) return res.json();
  const body = await res.json().catch(() => ({}));
  const err = new Error(
    body.message ||
      (res.status === 401
        ? "Session expired. Please sign in again."
        : res.status === 403
          ? "You do not have access to this data."
          : "Something went wrong. Please try again."),
  );
  err.status = res.status;
  throw err;
}

// undefined / empty params skip pannum
function toQuery(params) {
  const qs = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== "") qs.append(k, String(v));
  });
  const q = qs.toString();
  return q ? `?${q}` : "";
}

async function fetchDashboard(params, signal) {
  const res = await fetch(
    `${API_BASE}/api/analytics/dashboard${toQuery(params)}`,
    { signal, headers: authHeaders() },
  );
  return checkResponse(res);
}

async function fetchProducts(params, signal) {
  const res = await fetch(
    `${API_BASE}/api/analytics/products${toQuery(params)}`,
    { signal, headers: authHeaders() },
  );
  return checkResponse(res);
}

async function fetchFilterOptions(signal) {
  const res = await fetch(`${API_BASE}/api/analytics/filters`, {
    signal,
    headers: authHeaders(),
  });
  return checkResponse(res);
}

// ---- UI-only config (no business data) ----
const KPI_META = {
  grossRevenue: { icon: DollarSign, color: "#176B5B" },
  netProfit: { icon: TrendingUp, color: "#22A06B" },
  salesCount: { icon: ShoppingBag, color: "#2A9D8F" },
  totalOrders: { icon: Package, color: "#176B5B" },
  activeProducts: { icon: Package, color: "#4A90A4" },
  totalCustomers: { icon: Users, color: "#2A9D8F" },
  commission: { icon: Percent, color: "#718096" },
  deliverySuccessRate: { icon: Truck, color: "#22A06B" },
  returnRate: { icon: RotateCcw, color: "#F4B942" },
  avgOrderValue: { icon: Award, color: "#176B5B" },
  pendingSettlement: { icon: DollarSign, color: "#4A90A4" },
  cancelledOrders: { icon: TrendingDown, color: "#D9534F" },
};

const CATEGORY_COLORS = [
  "#176B5B",
  "#2A9D8F",
  "#4A90A4",
  "#F4B942",
  "#8E7DBE",
  "#D9534F",
];

const STATUS_COLORS = {
  Delivered: "#22A06B",
  "In-Transit": "#4A90A4",
  Pending: "#F4B942",
  Returned: "#D9534F",
  Cancelled: "#A0AEC0",
};

const PAGE_SIZE = 10;
const EXPORT_LIMIT = 5000;

const nf = new Intl.NumberFormat("en-IN", { maximumFractionDigits: 0 });
const inr = (n) => `₹${nf.format(Number(n) || 0)}`;

function formatKpi(kpi) {
  const v = Number(kpi.value) || 0;
  if (kpi.format === "CURRENCY") return inr(v);
  if (kpi.format === "PERCENT") return `${v.toFixed(1)}%`;
  return nf.format(v);
}

const escapeHtml = (s) =>
  String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");

const csvCell = (s) => `"${String(s).replace(/"/g, '""')}"`;

function downloadBlob(content, type, filename) {
  const blob = new Blob([content], { type });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export default function BIDashboard({
  userRole = "VENDOR",
  vendorName = "",
  onLogout,
}) {
  const isVendor = userRole === "VENDOR";

  const [dateRange, setDateRange] = useState("30d");
  const [selectedVendor, setSelectedVendor] = useState("All"); // vendor code (admin only)
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [selectedStatus, setSelectedStatus] = useState("All");
  const [searchQuery, setSearchQuery] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [sort, setSort] = useState({ by: "revenue", dir: "desc" });

  const [filterOptions, setFilterOptions] = useState({
    categories: [],
    vendors: [],
  });
  const [dashResult, setDashResult] = useState({ key: null, data: null });
  const [productsResult, setProductsResult] = useState({
    key: null,
    data: null,
  });
  const [error, setError] = useState("");
  const [exporting, setExporting] = useState(false);

  // Vendor scope is enforced by the server; admin picks vendor, vendor => nothing sent
  const vendorParam = isVendor ? undefined : selectedVendor;

  // Loading = "result is not for the current filters yet" (no setState in effect)
  const dashKey = `${dateRange}|${vendorParam}`;
  const tableKey = JSON.stringify([
    dateRange,
    vendorParam,
    selectedCategory,
    selectedStatus,
    debouncedSearch,
    currentPage,
    sort.by,
    sort.dir,
  ]);

  const dashboard = dashResult.data;
  const productsPage = productsResult.data ?? {
    content: [],
    page: 1,
    totalPages: 1,
    totalElements: 0,
  };
  const dashLoading = dashResult.key !== dashKey;
  const tableLoading = productsResult.key !== tableKey;

  const onLogoutRef = useRef(onLogout);
  useEffect(() => {
    onLogoutRef.current = onLogout;
  }, [onLogout]);

  const handleError = (err) => {
    if (err.name === "AbortError") return;
    if (err.status === 401) {
      onLogoutRef.current?.();
      return;
    }
    setError(err.message || "Failed to load analytics.");
  };

  // Debounce search box
  useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchQuery.trim()), 400);
    return () => clearTimeout(t);
  }, [searchQuery]);

  // Filter dropdown options (categories, vendors)
  useEffect(() => {
    const ctrl = new AbortController();
    fetchFilterOptions(ctrl.signal).then(setFilterOptions).catch(handleError);
    return () => ctrl.abort();
  }, []);

  // KPIs + charts
  useEffect(() => {
    const ctrl = new AbortController();
    fetchDashboard({ range: dateRange, vendorId: vendorParam }, ctrl.signal)
      .then((data) => {
        setDashResult({ key: dashKey, data });
        setError("");
      })
      .catch((err) => {
        handleError(err);
        if (err.name !== "AbortError") {
          setDashResult((prev) => ({ ...prev, key: dashKey }));
        }
      });
    return () => ctrl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dashKey]);

  // Products table
  useEffect(() => {
    const ctrl = new AbortController();
    fetchProducts(
      {
        range: dateRange,
        vendorId: vendorParam,
        category: selectedCategory,
        stockStatus: selectedStatus,
        search: debouncedSearch,
        page: currentPage,
        size: PAGE_SIZE,
        sortBy: sort.by,
        sortDir: sort.dir,
      },
      ctrl.signal,
    )
      .then((data) => setProductsResult({ key: tableKey, data }))
      .catch((err) => {
        handleError(err);
        if (err.name !== "AbortError") {
          setProductsResult((prev) => ({ ...prev, key: tableKey }));
        }
      });
    return () => ctrl.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tableKey]);

  // Filter handlers always reset to page 1
  const onFilter = (setter) => (e) => {
    setter(e.target.value);
    setCurrentPage(1);
  };

  const handleSort = (key) => {
    setSort((prev) =>
      prev.by === key
        ? { by: key, dir: prev.dir === "asc" ? "desc" : "asc" }
        : { by: key, dir: key === "name" ? "asc" : "desc" },
    );
    setCurrentPage(1);
  };

  // ---- Exports (fetch ALL rows for current filters, not just current page) ----
  const fetchAllForExport = async () => {
    const data = await fetchProducts({
      range: dateRange,
      vendorId: vendorParam,
      category: selectedCategory,
      stockStatus: selectedStatus,
      search: debouncedSearch,
      page: 1,
      size: EXPORT_LIMIT,
      sortBy: sort.by,
      sortDir: sort.dir,
    });
    return data.content;
  };

  const exportName = (ext, prefix) =>
    `${isVendor ? "Vendor" : "Admin"}_${prefix}_${new Date().toISOString().slice(0, 10)}.${ext}`;

  const handleExportCSV = async () => {
    try {
      setExporting(true);
      const rows = await fetchAllForExport();
      const header =
        "Product ID,Product Name,Category,Vendor,Units Sold,Revenue (INR),Stock,Status\n";
      const body = rows
        .map((p) =>
          [
            csvCell(p.id),
            csvCell(p.name),
            csvCell(p.category),
            csvCell(p.vendor),
            p.sold,
            p.revenue,
            p.stock,
            csvCell(p.status),
          ].join(","),
        )
        .join("\n");
      downloadBlob(
        "\uFEFF" + header + body,
        "text/csv;charset=utf-8;",
        exportName("csv", "Analytics"),
      );
    } catch (err) {
      handleError(err);
    } finally {
      setExporting(false);
    }
  };

  const handleExportExcel = async () => {
    try {
      setExporting(true);
      const rows = await fetchAllForExport();
      const html = `
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
            ${rows
              .map(
                (p) => `<tr>
                <td>${escapeHtml(p.id)}</td><td>${escapeHtml(p.name)}</td><td>${escapeHtml(p.category)}</td><td>${escapeHtml(p.vendor)}</td><td>${p.sold}</td><td>${p.revenue}</td><td>${p.stock}</td><td>${escapeHtml(p.status)}</td>
              </tr>`,
              )
              .join("")}
          </tbody>
        </table>
      </body></html>`;
      downloadBlob(html, "application/vnd.ms-excel", exportName("xls", "Report"));
    } catch (err) {
      handleError(err);
    } finally {
      setExporting(false);
    }
  };

  // ---- Derived chart data ----
  const kpis = dashboard?.kpis ?? [];
  const trendData = dashboard?.trend ?? [];
  const categoryData = (dashboard?.categoryShare ?? []).map((c, i) => ({
    ...c,
    color: CATEGORY_COLORS[i % CATEGORY_COLORS.length],
  }));
  const statusData = (dashboard?.orderStatus ?? []).map((s) => ({
    ...s,
    color: STATUS_COLORS[s.status] || "#A0AEC0",
  }));

  const totalPages = productsPage.totalPages || 1;
  const pageNumbers = [];
  for (
    let i = Math.max(1, currentPage - 2);
    i <= Math.min(totalPages, currentPage + 2);
    i++
  ) {
    pageNumbers.push(i);
  }

  const yTick = (v) => (v >= 1000 ? `₹${Math.round(v / 1000)}k` : `₹${v}`);
  const emptyChart = (
    <div
      style={{
        height: 200,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "#718096",
        fontSize: 13,
      }}
    >
      No data for this period
    </div>
  );

  return (
    <div className="bi-dashboard-container">
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
              onChange={onFilter(setDateRange)}
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
              disabled={exporting}
            >
              <Download size={13} />
              <span>CSV</span>
            </button>
            <button
              type="button"
              className="btn-export"
              onClick={handleExportExcel}
              disabled={exporting}
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
          role="alert"
          className="no-print"
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            margin: "0 0 12px",
            padding: "10px 14px",
            borderRadius: 8,
            background: "#fef2f2",
            border: "1px solid #fecaca",
            color: "#b91c1c",
            fontSize: 13,
          }}
        >
          <AlertCircle size={15} />
          <span>{error}</span>
        </div>
      )}

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
              onChange={onFilter(setSelectedVendor)}
              className="bi-mini-select"
            >
              <option value="All">All Authorized Vendors (Marketplace)</option>
              {filterOptions.vendors.map((v) => (
                <option key={v.vendorId} value={v.vendorId}>
                  {v.name}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="filter-item">
          <label>Category:</label>
          <select
            value={selectedCategory}
            onChange={onFilter(setSelectedCategory)}
            className="bi-mini-select"
          >
            <option value="All">All Categories</option>
            {filterOptions.categories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-item">
          <label>Stock Status:</label>
          <select
            value={selectedStatus}
            onChange={onFilter(setSelectedStatus)}
            className="bi-mini-select"
          >
            <option value="All">All Status</option>
            <option value="In Stock">In Stock</option>
            <option value="Low Stock">Low Stock</option>
            <option value="Out of Stock">Out of Stock</option>
          </select>
        </div>

        <div className="filter-search-box">
          <Search size={14} className="search-icon" />
          <input
            type="text"
            placeholder="Search items or SKU..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(1);
            }}
            className="search-input"
          />
        </div>
      </div>

      <section
        className="kpi-grid"
        style={{ opacity: dashLoading ? 0.6 : 1, transition: "opacity .2s" }}
      >
        {kpis.map((kpi) => {
          const meta = KPI_META[kpi.key] || { icon: Package, color: "#176B5B" };
          const Icon = meta.icon;
          const hasChange = kpi.changePercent !== null && kpi.changePercent !== undefined;
          const isGood = hasChange
            ? kpi.changePercent >= 0 === kpi.positiveIsGood
            : true;
          return (
            <div key={kpi.key} className="kpi-card">
              <div className="kpi-top">
                <span className="kpi-title">{kpi.title}</span>
                <div
                  className="kpi-icon-pill"
                  style={{
                    backgroundColor: `${meta.color}15`,
                    color: meta.color,
                  }}
                >
                  <Icon size={16} />
                </div>
              </div>
              <div className="kpi-bottom">
                <h3 className="kpi-value">{formatKpi(kpi)}</h3>
                {hasChange ? (
                  <span
                    className={`kpi-trend ${isGood ? "trend-up" : "trend-down"}`}
                  >
                    {kpi.changePercent >= 0 ? (
                      <TrendingUp size={12} />
                    ) : (
                      <TrendingDown size={12} />
                    )}
                    {kpi.changePercent > 0 ? "+" : ""}
                    {kpi.changePercent.toFixed(1)}%
                  </span>
                ) : (
                  kpi.note && (
                    <span className="kpi-trend" style={{ color: "#718096" }}>
                      {kpi.note}
                    </span>
                  )
                )}
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
            <span className="chart-badge">Live Aggregations</span>
          </div>
          <div className="chart-wrapper">
            {trendData.length === 0 ? (
              emptyChart
            ) : (
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
                    dataKey="label"
                    stroke="#718096"
                    fontSize={12}
                    tickLine={false}
                    minTickGap={20}
                  />
                  <YAxis
                    stroke="#718096"
                    fontSize={12}
                    tickLine={false}
                    tickFormatter={yTick}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#ffffff",
                      borderRadius: "8px",
                      border: "1px solid #E5EAE8",
                    }}
                    formatter={(value) => [inr(value), ""]}
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
            )}
          </div>
        </div>

        <div className="chart-card chart-small">
          <div className="chart-header">
            <div>
              <h3>Sales by Category</h3>
              <p>Revenue contribution percentage</p>
            </div>
          </div>
          <div className="chart-wrapper donut-center-wrap">
            {categoryData.length === 0 ? (
              emptyChart
            ) : (
              <>
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
                      nameKey="name"
                    >
                      {categoryData.map((entry) => (
                        <Cell key={entry.name} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value) => [`${value}%`, "Share"]} />
                  </PieChart>
                </ResponsiveContainer>
                <div className="donut-legend">
                  {categoryData.map((cat) => (
                    <div key={cat.name} className="legend-row">
                      <div
                        className="legend-indicator"
                        style={{ backgroundColor: cat.color }}
                      ></div>
                      <span className="legend-name">{cat.name}</span>
                      <span className="legend-value">{cat.value}%</span>
                    </div>
                  ))}
                </div>
              </>
            )}
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
            {statusData.every((s) => s.count === 0) ? (
              emptyChart
            ) : (
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
                  <YAxis
                    stroke="#718096"
                    fontSize={12}
                    tickLine={false}
                    allowDecimals={false}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#ffffff",
                      borderRadius: "8px",
                      border: "1px solid #E5EAE8",
                    }}
                  />
                  <Bar dataKey="count" name="Total Orders" radius={[6, 6, 0, 0]}>
                    {statusData.map((entry) => (
                      <Cell key={entry.status} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
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
          <span className="records-count">
            Showing {productsPage.content.length} of{" "}
            {productsPage.totalElements} Products
          </span>
        </div>

        <div
          className="table-responsive"
          style={{ opacity: tableLoading ? 0.6 : 1, transition: "opacity .2s" }}
        >
          <table className="bi-table">
            <thead>
              <tr>
                <th
                  onClick={() => handleSort("name")}
                  style={{ cursor: "pointer" }}
                >
                  Product Identifier <ArrowUpDown size={12} />
                </th>
                <th>Category</th>
                <th>Vendor Store</th>
                <th
                  onClick={() => handleSort("sold")}
                  style={{ cursor: "pointer" }}
                >
                  Units Sold <ArrowUpDown size={12} />
                </th>
                <th
                  onClick={() => handleSort("revenue")}
                  style={{ cursor: "pointer" }}
                >
                  Total Revenue <ArrowUpDown size={12} />
                </th>
                <th
                  onClick={() => handleSort("stock")}
                  style={{ cursor: "pointer" }}
                >
                  Inventory Stock <ArrowUpDown size={12} />
                </th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {productsPage.content.length === 0 && !tableLoading && (
                <tr>
                  <td
                    colSpan={7}
                    style={{ textAlign: "center", padding: 24, color: "#718096" }}
                  >
                    No products match the selected filters.
                  </td>
                </tr>
              )}
              {productsPage.content.map((p) => (
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
                  <td className="font-semibold">{nf.format(p.sold)}</td>
                  <td className="font-bold text-primary">{inr(p.revenue)}</td>
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
              ))}
            </tbody>
          </table>
        </div>

        <div className="table-pagination-bar no-print">
          <span className="pagination-info">
            Page {productsPage.page} of {totalPages}
          </span>
          <div className="pagination-buttons">
            <button
              type="button"
              className="btn-page"
              disabled={currentPage <= 1}
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            >
              Previous
            </button>
            {pageNumbers.map((n) => (
              <button
                key={n}
                type="button"
                className={`btn-page ${n === currentPage ? "active" : ""}`}
                onClick={() => setCurrentPage(n)}
              >
                {n}
              </button>
            ))}
            <button
              type="button"
              className="btn-page"
              disabled={currentPage >= totalPages}
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            >
              Next
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
