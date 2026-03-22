import { GitAnalysis } from "@/models/GitAnalysis";
import { apiGet } from "./apiClient";

const SERVICE_PATH = "gitCommunication";

export async function analyzeRepo(repoURI: string): Promise<GitAnalysis> {
  return apiGet(
    `${SERVICE_PATH}/gitAnalysis?repoURI=${encodeURIComponent(repoURI)}`,
  );
}
