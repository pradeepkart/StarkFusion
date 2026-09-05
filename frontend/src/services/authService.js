import api from "./api";

export async function login(credentials) {
  const { data } = await api.post("/auth/login", credentials);
  return data;
}

export function register(student) {
  return api.post("/auth/register", student);
}
export function logout() {
  localStorage.removeItem("starkfusion-access-token");
}
