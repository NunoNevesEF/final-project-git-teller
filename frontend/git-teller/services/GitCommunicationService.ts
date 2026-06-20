import { GitAnalysis } from "@/models/GitAnalysis";
import { apiGet, apiPost } from "./apiClient";
import {CreateScheduledReportDTO} from "@/models/scheduledReport/CreateScheduledReportDTO";
import authApiClient from "@/services/authApiClient";
import {GetScheduledReportDTO} from "@/models/scheduledReport/GetScheduledReportDTO";

const PUBLIC_SERVICE_PATH = "public/gitCommunication";
const PRIVATE_SERVICE_PATH = "private/gitCommunication";

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
    return apiPost(`${PUBLIC_SERVICE_PATH}/commitAnalysis`, {
      repoURI,
      flag,
      byShas: byShas ?? null,
      byDateRange: byDateRange ?? null,
    });
  }

  return apiGet(
      `${PUBLIC_SERVICE_PATH}/gitAnalysis?repoURI=${encodeURIComponent(repoURI)}`,
  );
}

export async function analyseRepoWithToken(
    repoURI: string,
    flag: boolean = false,
    gitAccountId: number,
    byShas?: ByShasRequest,
    byDateRange?: ByDateRangeRequest,
): Promise<GitAnalysis> {
    if (flag) {
        return(
            await authApiClient.post<GitAnalysis>(`api/${PRIVATE_SERVICE_PATH}/commitAnalysis?gitAccountId=${gitAccountId}`, {
                repoURI,
                flag,
                byShas: byShas ?? null,
                byDateRange: byDateRange ?? null,
            })
        ).data;
    }

    const params = new URLSearchParams({
        repoURI: repoURI,
        gitAccountId: String(gitAccountId),
    })

    return (
        await authApiClient.get<GitAnalysis>(`api/${PRIVATE_SERVICE_PATH}/gitAnalysis?${params.toString()}`)
    ).data;
}
