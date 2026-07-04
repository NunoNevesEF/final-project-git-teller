import { GitAnalysis } from "@/models/GitAnalysis";
import authApiClient from "@/services/authApiClient";
import { DateInterval } from "@/models/DateInterval";

const PUBLIC_SERVICE_PATH = "api/public/gitCommunication";
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
  // PUBLIC (sem auth)
  if (!request.gitAccountId) {
    console.log("inside request");
    return (
      await authApiClient.post<GitAnalysis>(
        `${PUBLIC_SERVICE_PATH}/gitAnalysis`,
        request,
      )
    ).data;
  }

  // PRIVATE (com auth header via authApiClient interceptor)
  return (
    await authApiClient.post<GitAnalysis>(
      `${PRIVATE_SERVICE_PATH}/gitAnalysis`,
      request,
    )
  ).data;
}
