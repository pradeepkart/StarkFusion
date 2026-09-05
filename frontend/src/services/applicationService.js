import api from "./api";
export const getApplications = () => api.get("/admin/applications");
export const getStudentApplications = () => api.get("/user/applications");
export const createApplication = ({ jobId }) => api.post("/user/applications", { jobId });
export const updateApplicationStatus = (id, status) => api.put(`/admin/applications/${id}/status`, { status });
