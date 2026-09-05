import { useState } from "react";
export default function useStudents(initial = []) {
  return useState(initial);
}
