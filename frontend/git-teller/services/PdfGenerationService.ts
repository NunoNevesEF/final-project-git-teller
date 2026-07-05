import { GitAnalysis } from "@/models/GitAnalysis";
import apiClient from "@/services/authApiClient";

const SERVICE_PATH = "api/public/pdf";

export async function generatePdf(gitAnalysis: GitAnalysis): Promise<Blob> {
  return (
    await apiClient.post<Blob>(`${SERVICE_PATH}/generate`, gitAnalysis, {
      responseType: "blob",
    })
  ).data;
}
