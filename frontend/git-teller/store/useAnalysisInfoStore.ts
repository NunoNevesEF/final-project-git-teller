import { GitAnalysis } from "@/models/GitAnalysis";
import { create } from "zustand";

interface AnalysisInfoState {
    result: GitAnalysis | null;
    reportId: number | null;
    projectName: string;

    setResult: (result: GitAnalysis) => void;
    setReportId: (id: number | null) => void;
    setProjectName: (name: string) => void;

    clearResult: () => void;
}

export const useAnalysisInfoStore = create<AnalysisInfoState>((set) => ({
    result: null,
    reportId: null,
    projectName: '',

    setResult: (result) => set({ result }),

    setReportId: (reportId) => set({ reportId }),

    setProjectName: (projectName) => set({ projectName }),

    clearResult: () =>
        set({
            result: null,
            reportId: null,
            projectName: '',
        }),
}));
