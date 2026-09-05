import api from "./api";
export const getStudents = () => api.get("/students");
export const getStudentById = (studentId) => api.get(`/students/${studentId}`);
export const createStudent = (student) => api.post("/students", student);
export const updateStudent = (studentId, student) =>
  api.put(`/students/${studentId}`, student);
export const deleteStudent = (studentId) =>
  api.delete(`/students/${studentId}`);
