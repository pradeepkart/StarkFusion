import { useState } from "react";
export default function useJobs(initial = []) {
  return useState(initial);
}
