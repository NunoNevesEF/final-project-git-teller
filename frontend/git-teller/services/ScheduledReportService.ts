import {CreateScheduledReportDTO} from "@/models/scheduledReport/CreateScheduledReportDTO";
import authApiClient from "@/services/authApiClient";
import {GetScheduledReportDTO} from "@/models/scheduledReport/GetScheduledReportDTO";
import {ScheduledReportJobListItemDTO} from "@/models/scheduledReport/ScheduledReportJobListItemDTO";

const SERVICE_PATH = '/api/private/schedule'

export async function createScheduledReport(dto: CreateScheduledReportDTO){
    return (await authApiClient.post(`${SERVICE_PATH}/create`, dto)).data;
}

export async function getUserScheduledReports(): Promise<GetScheduledReportDTO[]>{
    return (await authApiClient.get<GetScheduledReportDTO[]>(`${SERVICE_PATH}/get`)).data;
}

export async function getUserQueuedJobs(): Promise<ScheduledReportJobListItemDTO[]>{
    return (await authApiClient.get<ScheduledReportJobListItemDTO[]>(`${SERVICE_PATH}/get-queued-jobs`)).data;
}

export async function pauseScheduledReport(scheduledReportId: Int): Promise<void>{
    return (await authApiClient.post(`${SERVICE_PATH}/pause-schedule/${scheduledReportId}`))
}

export async function resumeScheduledReport(scheduledReportId: Int): Promise<void>{
    return (await authApiClient.post(`${SERVICE_PATH}/resume-schedule/${scheduledReportId}`))
}

export async function deleteScheduledReport(scheduledReportId: Int): Promise<void>{
    return (await authApiClient.post(`${SERVICE_PATH}/delete-schedule/${scheduledReportId}`))
}