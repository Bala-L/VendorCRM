import  { useState } from "react";
import Sidebar from "./components/Sidebar";
import BIDashboard from "./pages/BIDashboard";
import CommandCentre from "./pages/CommandCentre";
import VendorLoginPage from "./pages/VendorLoginPage";

export default function App() {
  const [auth, setAuth] = useState(null);
  const [activePage, setActivePage] = useState("bi-dashboard");

  const handleLogin = (authData) => {
    setAuth(authData);
    setActivePage("bi-dashboard");
  };

  const handleLogout = () => {
    setAuth(null);
  };

  if (!auth) {
    return <VendorLoginPage onLogin={handleLogin} />;
  }

  return (
    <div
      style={{
        display: "flex",
        width: "100vw",
        minHeight: "100vh",
        overflowX: "hidden",
      }}
    >
      <Sidebar
        activePage={activePage}
        setActivePage={setActivePage}
        auth={auth}
        onLogout={handleLogout}
      />

      <main style={{ flex: 1, minWidth: 0, overflowY: "auto" }}>
        {activePage === "bi-dashboard" ? (
          <BIDashboard
            userRole={auth.role}
            vendorId={auth.vendorId}
            vendorName={auth.vendorName}
          />
        ) : activePage === "command-centre" ? (
          <CommandCentre
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
