import { apiPostBlob, apiGet, apiGetBlob } from "./apiClient";

const PUBLIC_SERVICE_PATH = "public/report";
const PRIVATE_SERVICE_PATH = "private/report"

export async function createReportPDF(bytes: string[]): Promise<Blob> {
  const token = localStorage.getItem("accessToken");
  return apiPostBlob(`${PUBLIC_SERVICE_PATH}/create`, bytes, token);
}

export async function getUserReports() {
  const token = localStorage.getItem("accessToken");
  return apiGet(`${PUBLIC_SERVICE_PATH}/user-reports`, token);
}

export async function downloadReport(id: number) {
  const token = localStorage.getItem("accessToken");
  return apiGetBlob(`${PUBLIC_SERVICE_PATH}/user-reports/${id}/download`, token);
}
