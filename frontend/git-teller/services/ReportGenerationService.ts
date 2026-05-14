import { apiPostBlob, apiGet, apiGetBlob } from "./apiClient";

const SERVICE_PATH = "report";

export async function createReport(bytes: string[]): Promise<Blob> {
  const token = localStorage.getItem("accessToken");
  return apiPostBlob(`${SERVICE_PATH}/create`, bytes, token);
}

export async function getUserReports() {
  const token = localStorage.getItem("accessToken");
  return apiGet(`${SERVICE_PATH}/user-reports`, token);
}

export async function downloadReport(id: number) {
  const token = localStorage.getItem("accessToken");
  return apiGetBlob(`${SERVICE_PATH}/user-reports/${id}/download`, token);
}
