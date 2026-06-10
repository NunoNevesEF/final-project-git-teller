import {CreateScheduledReportDTO} from "@/models/scheduledReport/CreateScheduledReportDTO";
import authApiClient from "@/services/authApiClient";
import {GetScheduledReportDTO} from "@/models/scheduledReport/GetScheduledReportDTO";

const SERVICE_PATH = '/api/private/schedule'

export async function createScheduledReport(dto: CreateScheduledReportDTO){
    return (await authApiClient.post(`${SERVICE_PATH}/create`, dto)).data;
}

export async function getUserScheduledReports(): Promise<GetScheduledReportDTO[]>{
    return (await authApiClient.get<GetScheduledReportDTO[]>(`${SERVICE_PATH}/get`)).data;
}
