import { GitAnalysis } from "@/models/GitAnalysis";
import { apiPost } from "./apiClient";
import authApiClient from "@/services/authApiClient";
import { DateInterval } from "@/models/DateInterval";

const PUBLIC_SERVICE_PATH = "public/gitCommunication";
const PRIVATE_SERVICE_PATH = "api/private/gitCommunication";

export type PromptComplexity = "SIMPLE" | "MEDIUM" | "COMPLEX";
export type AnalysisMode = "DIFF" | "META";

export interface ByShasRequest {
  commitShas: string[];
  promptComplexity?: PromptComplexity;
  analysisMode?: AnalysisMode;
  requestedAnalyses?: string[];
}

export interface ByDetailedSettingsRequest {
  promptComplexity?: PromptComplexity;
  analysisMode?: AnalysisMode;
  requestedAnalyses?: string[];
}

export interface GitAnalysisRequest {
  repoURI: string;
  llmRequest?: any | null;
  dateFilter?: DateInterval | null;
  gitAccountId?: number | null;
}

export async function analyzeRepo(
  request: GitAnalysisRequest,
): Promise<GitAnalysis> {
    console.log(request)
  // PUBLIC (sem auth)
  if (!request.gitAccountId) {
    return apiPost(`${PUBLIC_SERVICE_PATH}/gitAnalysis`, request);
  }

  // PRIVATE (com auth header via authApiClient interceptor)
  return (
    await authApiClient.post<GitAnalysis>(
      `${PRIVATE_SERVICE_PATH}/gitAnalysis`,
      request,
    )
  ).data;
}
