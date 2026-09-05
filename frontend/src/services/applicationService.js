import api from "./api";
export const getApplications = () => api.get("/applications");
export const getStudentApplications = (studentId) =>
  api.get(`/students/${studentId}/applications`);
export const createApplication = (application) =>
  api.post("/applications", application);
export const updateApplicationStatus = (applicationId, status) =>
  api.patch(`/applications/${applicationId}/status`, { status });
