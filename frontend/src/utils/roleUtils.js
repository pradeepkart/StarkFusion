import { ROLES } from "./constants";
export const isAdmin = (user) => user?.role === ROLES.ADMIN;
export const isStudent = (user) => user?.role === ROLES.STUDENT;
