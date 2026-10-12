import { useState } from "react";
import Sidebar from "./components/Sidebar";
import BIDashboard from "./pages/BIDashboard";
import CommandCentre from "./pages/CommandCentre";
import VendorLoginPage from "./pages/VendorLoginPage";
import { clearAuth, loadAuth, saveAuth } from "./api";

export default function App() {
  const [auth, setAuth] = useState(() => loadAuth());
  const [activePage, setActivePage] = useState("bi-dashboard");

  const handleLogin = (authData) => {
    saveAuth(authData);
    setAuth(authData);
    setActivePage("bi-dashboard");
  };

  const handleLogout = () => {
    clearAuth();
    setAuth(null);
  };

  if (!auth) {
    return <VendorLoginPage onLogin={handleLogin} />;
  }

  return (
    <div
      style={{
        display: "flex",
        width: "100%",
        minHeight: "100vh",
        background: "var(--background)",
      }}
    >
      <Sidebar
        activePage={activePage}
        setActivePage={setActivePage}
        auth={auth}
        onLogout={handleLogout}
      />

      <main
        style={{
          marginLeft: "220px" ,
          width:
            "calc(100% - 220px)" ,
          minHeight: "100vh",
          boxSizing: "border-box",
        }}
      >
        {activePage === "bi-dashboard" ? (
          <BIDashboard
            token={auth.token}
            userRole={auth.role}
            vendorId={auth.vendorId}
            vendorName={auth.vendorName}
          />
        ) : activePage === "command-centre" ? (
          <CommandCentre
            token={auth.token}
            userRole={auth.role}
            vendorId={auth.vendorId}
            vendorName={auth.vendorName}
          />
        ) : (
          <div
            style={{ padding: "40px", textAlign: "center", color: "#718096" }}
          >
            <h2>{activePage.toUpperCase()} Module</h2>
            <p style={{ marginTop: "8px" }}>
              Module integrated with Vendor CRM backend.
            </p>
          </div>
        )}
      </main>
    </div>
  );
}
