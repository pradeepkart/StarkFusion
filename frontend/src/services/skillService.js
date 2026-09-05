import api from "./api";
export const getSkills = () => api.get("/skills");
export const createSkill = (skill) => api.post("/admin/skills", skill);
export const updateSkill = (id, skill) => api.put(`/admin/skills/${id}`, skill);
export const deleteSkill = (id) => api.delete(`/admin/skills/${id}`);
export const getStudentSkills = () => api.get("/user/skills");
export const addStudentSkill = (skill) => api.post("/user/skills", skill);
export const removeStudentSkill = (skillId) => api.delete(`/user/skills/${skillId}`);
