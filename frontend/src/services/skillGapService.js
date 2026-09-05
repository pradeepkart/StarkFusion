import api from "./api";
export const getSkillGaps = (jobId) => api.get(`/user/jobs/${jobId}/skill-gap`);
