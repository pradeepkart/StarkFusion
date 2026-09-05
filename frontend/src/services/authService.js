import api from "./api";
export async function login(credentials) {
  return (await api.post("/auth/login", { email: credentials.email, password: credentials.password })).data;
}
export async function register(account) {
  return (await api.post("/auth/register", { name: account.name, email: account.email, password: account.password })).data;
}
export function logout() {
  localStorage.removeItem("starkfusion-access-token");
  localStorage.removeItem("starkfusion-user");
}
