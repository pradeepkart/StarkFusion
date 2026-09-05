import { useState } from "react";
export default function useSkillGap(initial = []) {
  return useState(initial);
}
