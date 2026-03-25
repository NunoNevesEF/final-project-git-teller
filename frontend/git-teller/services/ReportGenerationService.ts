import { GitAnalysis } from "@/models/GitAnalysis";
import { apiPostBlob } from "./apiClient";

const SERVICE_PATH = "report";

export async function createReport(gitAnalysis: GitAnalysis): Promise<Blob> {
  return apiPostBlob(`${SERVICE_PATH}/create`, gitAnalysis);
}
