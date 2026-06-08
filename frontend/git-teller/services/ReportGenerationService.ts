import { GitAnalysis } from "@/models/GitAnalysis";
import { apiPostBlob, apiGet, apiGetBlob } from "./apiClient";
import { getTokens } from "./secureStore";

const SERVICE_PATH = "report";

export async function createReport(gitAnaylsis: GitAnalysis): Promise<Blob> {
  const { accessToken } = await getTokens();

  return apiPostBlob(`${SERVICE_PATH}/create`, gitAnaylsis, accessToken);
}

export async function getUserReports() {
  const token = localStorage.getItem("accessToken");
  return apiGet(`${SERVICE_PATH}/user-reports`, token);
}

export async function downloadReport(id: number) {
  const token = localStorage.getItem("accessToken");
  return apiGetBlob(`${SERVICE_PATH}/user-reports/${id}/download`, token);
}
