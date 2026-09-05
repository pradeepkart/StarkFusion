import api from "./api";
export const getJobs = () => api.get("/jobs");
export const getJobById = (id) => api.get(`/jobs/${id}`);
export const createJob = (job) => api.post("/admin/jobs", job);
export const updateJob = (id, job) => api.put(`/admin/jobs/${id}`, job);
export const deleteJob = (id) => api.delete(`/admin/jobs/${id}`);
export const getJobSkills = (id) => api.get(`/jobs/${id}/skills`);
export const addJobSkill = (id, skill) => api.post(`/admin/jobs/${id}/skills`, skill);
export const removeJobSkill = (id, skillId) => api.delete(`/admin/jobs/${id}/skills/${skillId}`);
