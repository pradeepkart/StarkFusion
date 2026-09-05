import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "./AuthContextValue";
function readSession() {
  try {
    const user = JSON.parse(localStorage.getItem("starkfusion-user"));
    const token = localStorage.getItem("starkfusion-access-token");
    return token && token !== "undefined" && ["ROLE_ADMIN", "ROLE_USER"].includes(user?.role) ? user : null;
  } catch { return null; }
}
export function AuthProvider({ children }) {
  const [user, setUser] = useState(readSession);
  const signOut = useCallback(() => {
    localStorage.removeItem("starkfusion-user");
    localStorage.removeItem("starkfusion-access-token");
    setUser(null);
  }, []);
  const signIn = useCallback((response) => {
    if (!response.token || !["ROLE_ADMIN", "ROLE_USER"].includes(response.role)) {
      throw new Error("The server returned an invalid login response.");
    }
    const account = { name: response.name, email: response.email, role: response.role };
    localStorage.setItem("starkfusion-access-token", response.token);
    localStorage.setItem("starkfusion-user", JSON.stringify(account));
    setUser(account);
  }, []);
  useEffect(() => {
    window.addEventListener("starkfusion:unauthorized", signOut);
    return () => window.removeEventListener("starkfusion:unauthorized", signOut);
  }, [signOut]);
  const value = useMemo(() => ({ user, signIn, signOut, isAuthenticated: Boolean(user) }), [user, signIn, signOut]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
