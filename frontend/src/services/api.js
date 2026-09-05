import axios from "axios";
const api = axios.create({
  baseURL: (import.meta.env.VITE_API_URL || "/api").replace(/\/$/, ""),
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});
api.interceptors.request.use((config) => {
  if (!config.url?.startsWith("/auth/")) {
    const token = localStorage.getItem("starkfusion-access-token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isCancel(error)) return Promise.reject(error);
    const status = error.response?.status ?? 0;
    const sentToken = error.config?.headers?.Authorization;
    if (status === 401 && sentToken === `Bearer ${localStorage.getItem("starkfusion-access-token")}` && !error.config?.url?.startsWith("/auth/")) {
      window.dispatchEvent(new Event("starkfusion:unauthorized"));
    }
    return Promise.reject({
      status,
      message: error.response?.data?.message || (status ? "The request could not be completed." : "Cannot reach the server. Check that the backend is running."),
      errors: error.response?.data?.errors ?? {},
    });
  },
);
export default api;
export { api };
