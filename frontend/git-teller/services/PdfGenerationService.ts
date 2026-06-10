import { GitAnalysis } from "@/models/GitAnalysis";
import { apiPostBlob, apiGet, apiGetBlob } from "./apiClient";
import { getTokens } from "./secureStore";

const SERVICE_PATH = "public/pdf";

export async function generatePdf(gitAnalysis: GitAnalysis): Promise<Blob> {
  return apiPostBlob(`${SERVICE_PATH}/generate`, gitAnalysis);
}