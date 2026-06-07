import { GitAnalysis } from "@/models/GitAnalysis";
import { apiGet, apiPost } from "./apiClient";

const SERVICE_PATH = "public/gitCommunication";

type PromptComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX';
type AnalysisMode = 'DIFF' | 'META';

interface ByShasRequest {
  repoURI: string;
  commitShas: string[];
  promptComplexity?: PromptComplexity;
  analysisMode?: AnalysisMode;
  requestedAnalyses?: string[];
}

interface ByDateRangeRequest {
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


/*import { GitAnalysis } from "@/models/GitAnalysis";
import { apiGet } from "./apiClient";

const SERVICE_PATH = "public/gitCommunication";

export async function analyzeRepo(
    repoURI: string,
    flag: boolean = false,
    byShas?: {
      repoURI: string;
      commitShas: string[];
      promptComplexity: "SIMPLE" | "MEDIUM" | "COMPLEX";
      analysisMode: "DIFF" | "META"
    } | undefined,
    byDateRange?: {
      repoURI: string;
      fromDate: string;
      toDate: string;
      promptComplexity: "SIMPLE" | "MEDIUM" | "COMPLEX";
      analysisMode: "DIFF" | "META"
    } | undefined,
): Promise<GitAnalysis> {

  if(flag && byShas && byDateRange === "" ){
    return apiGet(
        `${SERVICE_PATH}/commitAnalysis?repoURI=${encodeURIComponent(repoURI)}&flag=${flag}&byShas=${flag}`,
    );
  }

  if(flag && byDateRange && byShas === "" ){
    return apiGet(
        `${SERVICE_PATH}/commitAnalysis?repoURI=${encodeURIComponent(repoURI)}&flag=${flag}&byDateRange=${flag}`,
    );
  }


  return apiGet(
      `${SERVICE_PATH}/gitAnalysis?repoURI=${encodeURIComponent(repoURI)}`,
  );




}*/
