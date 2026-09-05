import { useState } from "react";
export default function useSkills(initial = []) {
  return useState(initial);
}
