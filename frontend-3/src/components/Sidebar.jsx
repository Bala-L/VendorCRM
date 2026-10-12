import React from "react";
import { BarChart3, Terminal, LogOut } from "lucide-react";
import logo from "../assets/zenve_logo.png";
import "./Sidebar.css";

const NAV_ITEMS = [
  {
    key: "bi-dashboard",
    label: "BI Dashboard",
    icon: BarChart3,
    badge: "Analytics",
  },
  {
    key: "command-centre",
    label: "Command Centre",
    icon: Terminal,
    badge: "Live Ops",
  },
];

export default function Sidebar({ activePage, setActivePage, auth, onLogout }) {
  const isAdmin = auth?.role === "ADMIN";

  return (
    <aside className="vendor-sidebar">
      <div className="sidebar-brand">
        <div className="brand-logo-wrap">
          <img src={logo} alt="Vendor CRM Logo" className="brand-logo-img" />
        </div>
        <div className="brand-text">
          <h2>Vendor CRM</h2>
          <span>Enterprise Portal</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <span className="nav-group-title">Main Modules</span>
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          const isActive = activePage === item.key;
          return (
            <button
              key={item.key}
              type="button"
              className={`nav-item-btn ${isActive ? "active" : ""}`}
              onClick={() => setActivePage(item.key)}
              aria-current={isActive ? "page" : undefined}
            >
              <span className="nav-item-left">
                <Icon size={18} className="nav-icon" />
                <span>{item.label}</span>
              </span>
              {item.badge && (
                <span className={`nav-badge ${isActive ? "badge-active" : ""}`}>
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer-profile">
        <div className="profile-details-row">
          <div className="vendor-avatar">{isAdmin ? "AD" : "VR"}</div>
          <div className="vendor-info">
            <h4>{auth?.vendorName || "Vendor Store"}</h4>
            <span>{isAdmin ? "Role: Admin" : "Role: Authorized Vendor"}</span>
          </div>
        </div>

        {onLogout && (
          <button
            type="button"
            className="sidebar-logout-btn"
            onClick={onLogout}
            title="Sign out of Vendor CRM"
          >
            <LogOut size={14} />
            <span>Logout</span>
          </button>
        )}
      </div>
    </aside>
  );
}
