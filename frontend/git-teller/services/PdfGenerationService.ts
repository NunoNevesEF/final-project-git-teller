import { GitAnalysis } from "@/models/GitAnalysis";
import authApiClient from "@/services/authApiClient";

const SERVICE_PATH = "api/public/pdf";

export async function generatePdf(gitAnalysis: GitAnalysis): Promise<Blob> {
  return (
    await authApiClient.post<Blob>(`${SERVICE_PATH}/generate`, gitAnalysis, {
      responseType: "blob",
    })
  ).data;
}
