import { GitAnalysis } from "@/models/GitAnalysis";
import { GitAnalysisInput } from "@/models/GitAnalysisInput";
import { create } from "zustand";

interface AnalysisState {
  result: GitAnalysis | null;
  input: GitAnalysisInput | null;
  setResult: (result: GitAnalysis) => void;
  setInput: (input: GitAnalysisInput) => void;
  clearResult: () => void;
}

export const useAnalysisStore = create<AnalysisState>((set) => ({
  result: null,
  input: null,

  setInput: (input: GitAnalysisInput) => set({ input }),
  setResult: (result: GitAnalysis) => set({ result }),

  clearResult: () => set({ result: null, input: null }),
}));
