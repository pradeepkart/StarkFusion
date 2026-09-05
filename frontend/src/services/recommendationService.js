import api from "./api";
export const getRecommendations = (jobId) => api.get(`/user/jobs/${jobId}/recommendations`);
