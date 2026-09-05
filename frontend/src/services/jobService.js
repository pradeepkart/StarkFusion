import api from "./api";
export const getJobs = () => api.get("/jobs");
export const getJobById = (jobId) => api.get(`/jobs/${jobId}`);
export const createJob = (job) => api.post("/jobs", job);
export const updateJob = (jobId, job) => api.put(`/jobs/${jobId}`, job);
export const deleteJob = (jobId) => api.delete(`/jobs/${jobId}`);
