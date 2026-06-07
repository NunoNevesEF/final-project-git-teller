import { GitAnalysis } from "@/models/GitAnalysis";
import { apiGet, apiPost } from "./apiClient";

const SERVICE_PATH = "public/gitCommunication";

type PromptComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX';
type AnalysisMode = 'DIFF' | 'META';

export interface ByShasRequest {
  repoURI: string;
  commitShas: string[];
  promptComplexity?: PromptComplexity;
  analysisMode?: AnalysisMode;
  requestedAnalyses?: string[];
}

export interface ByDateRangeRequest {
  repoURI: string;
  fromDate: string;
  toDate: string;
  promptComplexity?: PromptComplexity;
  analysisMode?: AnalysisMode;
  requestedAnalyses?: string[];
}

export async function analyzeRepo(
    repoURI: string,
    flag: boolean = false,
    byShas?: ByShasRequest,
    byDateRange?: ByDateRangeRequest,
): Promise<GitAnalysis> {
  if (flag) {
    return apiPost(`${SERVICE_PATH}/commitAnalysis`, {
      repoURI,
      flag,
      byShas: byShas ?? null,
      byDateRange: byDateRange ?? null,
    });
  }

  return apiGet(
      `${SERVICE_PATH}/gitAnalysis?repoURI=${encodeURIComponent(repoURI)}`,
  );
}
