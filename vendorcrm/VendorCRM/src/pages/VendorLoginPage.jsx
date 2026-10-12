import  { useState } from "react";
import {
  Store,
  Lock,
  Mail,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  AlertCircle,
  Building2,
  UserCheck,
} from "lucide-react";
import "./VendorLoginPage.css";

// .env file la: VITE_API_URL=http://localhost:8080  (CRA use panna REACT_APP_API_URL)
const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

export default function VendorLoginPage({ onLogin }) {
  const [role, setRole] = useState("VENDOR");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRoleSwitch = (selectedRole) => {
    setRole(selectedRole);
    setError("");
    setEmail("");
    setPassword("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!email.trim() || !password.trim()) {
      setError("Please enter both Email/Username and Password.");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim(), password}),
      });

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        throw new Error(
          data.message ||
            (res.status === 401
              ? "Invalid email or password."
              : "Login failed. Please try again."),
        );
      }

      // data = { token, role, vendorId, vendorName, email }
      const storage = rememberMe ? localStorage : sessionStorage;
      storage.setItem("auth", JSON.stringify(data));

      onLogin(data);
   } catch (err) {
  console.error("LOGIN ERROR:", err);
  setError(err.message || "Login failed");
} finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-viewport">
      <div className="login-panel-card">
        <div className="login-brand-block">
          <div className="login-logo-badge">
            <Store size={26} />
          </div>
          <h2>Vendor CRM</h2>
          <p>Enterprise Operations & Analytics Portal</p>
        </div>

        <div className="login-role-selector">
          <button
            type="button"
            className={`role-tab-btn ${role === "VENDOR" ? "active" : ""}`}
            onClick={() => handleRoleSwitch("VENDOR")}
          >
            <Building2 size={14} />
            <span>Vendor Access</span>
          </button>
          <button
            type="button"
            className={`role-tab-btn ${role === "ADMIN" ? "active" : ""}`}
            onClick={() => handleRoleSwitch("ADMIN")}
          >
            <UserCheck size={14} />
            <span>Admin / Ops</span>
          </button>
        </div>

        {error && (
          <div className="login-error-banner" role="alert">
            <AlertCircle size={15} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form-body">
          <div className="login-field-group">
            <label htmlFor="login-email">
              {role === "VENDOR"
                ? "Authorized Email Address"
                : "Corporate Admin Email"}
            </label>
            <div className="login-input-wrap">
              <Mail size={16} className="login-field-icon" />
              <input
                id="login-email"
                type="text"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="username"
                required
              />
            </div>
          </div>

          <div className="login-field-group">
            <div className="login-label-row">
              <label htmlFor="login-password">Password</label>
              <button
                type="button"
                className="forgot-pass-btn"
                onClick={() =>
                  alert("Please contact your administrator to reset your password.")
                }
              >
                Forgot password?
              </button>
            </div>
            <div className="login-input-wrap">
              <Lock size={16} className="login-field-icon" />
              <input
                id="login-password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="eye-toggle-btn"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          <div className="login-aux-row">
            <label className="remember-checkbox-label">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              <span>Keep session active</span>
            </label>
            <span className="role-indicator-badge">
              Role: <strong>{role}</strong>
            </span>
          </div>

          <button
            type="submit"
            className="login-submit-button"
            disabled={loading}
          >
            <span>
              {loading
                ? "Signing in..."
                : `Sign In as ${role === "VENDOR" ? "Vendor" : "Admin"}`}
            </span>
            {!loading && <ArrowRight size={16} />}
          </button>
        </form>

        <div className="login-trust-footer">
          <ShieldCheck size={14} className="text-success" />
          <span>JWT Role-Authorized • Multi-Vendor Scoped Session</span>
        </div>
      </div>
    </div>
  );
}
