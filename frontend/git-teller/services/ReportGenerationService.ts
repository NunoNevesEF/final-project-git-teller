import { apiPostBlob } from "./apiClient";

const SERVICE_PATH = "report";

export async function createReport(bytes: string): Promise<Blob> {
  return apiPostBlob(`${SERVICE_PATH}/create`, bytes);
}
