import { useState } from "react";
export default function useApplications(initial = []) {
  return useState(initial);
}
