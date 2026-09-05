import api from "./api";
export const getStudents = () => api.get("/admin/students");
export const getStudentById = (id) => api.get(`/admin/students/${id}`);
export const getStudentProfile = () => api.get("/user/profile");
export const getAdminStudentSkills = (id) => api.get(`/admin/students/${id}/skills`);
