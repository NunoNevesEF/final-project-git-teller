import { GitAnalysis } from "@/models/GitAnalysis";
import { create } from "zustand";

interface AnalysisState {
  result: GitAnalysis | null;
  setResult: (result: GitAnalysis) => void;
  clearResult: () => void;
}

export const useAnalysisStore = create<AnalysisState>((set) => ({
  result: null,

  setResult: (result: GitAnalysis) => set({ result }),

  clearResult: () => set({ result: null }),
}));
