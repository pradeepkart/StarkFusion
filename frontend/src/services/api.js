import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem("starkfusion-access-token");

  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) =>
    Promise.reject(
      error.response?.data ?? {
        message: "Unable to reach the StarkFusion API.",
      },
    ),
);

export default api;
export { api };
