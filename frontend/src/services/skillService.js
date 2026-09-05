import api from "./api";
export const getSkills = () => api.get("/skills");
export const getStudentSkills = (studentId) =>
  api.get(`/students/${studentId}/skills`);
export const addStudentSkill = (studentId, skill) =>
  api.post(`/students/${studentId}/skills`, skill);
export const removeStudentSkill = (studentId, skillId) =>
  api.delete(`/students/${studentId}/skills/${skillId}`);
