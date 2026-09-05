import { useMemo, useState } from "react";
import { AuthContext } from "./AuthContextValue";

export { AuthContext };
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem("starkfusion-user");
    return savedUser ? JSON.parse(savedUser) : null;
  });

  function signIn(authResponse) {
    localStorage.setItem("starkfusion-access-token", authResponse.accessToken);
    localStorage.setItem("starkfusion-user", JSON.stringify(authResponse.user));
    setUser(authResponse.user);
  }

  function signOut() {
    localStorage.removeItem("starkfusion-access-token");
    localStorage.removeItem("starkfusion-user");
    setUser(null);
  }

  const value = useMemo(
    () => ({ user, signIn, signOut, isAuthenticated: Boolean(user) }),
    [user],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
