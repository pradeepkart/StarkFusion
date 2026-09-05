import api from "./api";
export const getSkillGaps = (studentId, jobId) =>
  api.get("/skill-gaps", { params: { studentId, jobId } });
