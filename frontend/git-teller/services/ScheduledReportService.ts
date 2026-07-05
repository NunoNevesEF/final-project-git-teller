import { CreateScheduledReportDTO } from "@/models/scheduledReport/CreateScheduledReportDTO";
import apiClient from "@/services/authApiClient";
import { GetScheduledReportDTO } from "@/models/scheduledReport/GetScheduledReportDTO";
import {ScheduledReportJobListItemDTO} from "@/models/scheduledReport/ScheduledReportJobListItemDTO";

const SERVICE_PATH = "/api/private/schedule";

export async function createScheduledReport(dto: CreateScheduledReportDTO) {
  return (await apiClient.post(`${SERVICE_PATH}/create`, dto)).data;
}

export async function getUserScheduledReports(): Promise<
  GetScheduledReportDTO[]
> {
  return (await apiClient.get<GetScheduledReportDTO[]>(`${SERVICE_PATH}/get`))
    .data;
}

export async function getUserQueuedJobs(): Promise<ScheduledReportJobListItemDTO[]>{
    return (await apiClient.get<ScheduledReportJobListItemDTO[]>(`${SERVICE_PATH}/get-queued-jobs`)).data;
}

export async function pauseScheduledReport(scheduledReportId: number): Promise<any>{
    return (await apiClient.post(`${SERVICE_PATH}/pause-schedule/${scheduledReportId}`))
}

export async function resumeScheduledReport(scheduledReportId: number): Promise<any>{
    return (await apiClient.post(`${SERVICE_PATH}/resume-schedule/${scheduledReportId}`))
}

export async function deleteScheduledReport(scheduledReportId: number): Promise<any>{
    return (await apiClient.post(`${SERVICE_PATH}/delete-schedule/${scheduledReportId}`))
}