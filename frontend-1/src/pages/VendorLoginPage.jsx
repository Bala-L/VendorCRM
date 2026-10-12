import { useState } from "react";
import {
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
import logo from "../assets/zenve_logo.png";
import { loginUser } from "../api";

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
      const authResponse = await loginUser({
        email: email.trim(),
        password,
        role,
      });

      onLogin(authResponse);
    } catch (err) {
      setError(err.message || "Unable to sign in. Please check backend status.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-viewport">
      <div className="login-panel-card">
        <div className="login-brand-block">
          <div className="login-logo-container">
            <img src={logo} alt="Vendor CRM Logo" className="login-logo-img" />
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
          <div className="login-error-banner">
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
                  alert(
                    "Password reset link will be sent by Backend SMTP Service.",
                  )
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
                ? "Authenticating JWT..."
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
