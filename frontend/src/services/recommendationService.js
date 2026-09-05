import api from "./api";
export const getRecommendations = (studentId) =>
  api.get(`/students/${studentId}/recommendations`);
