import {CreateScheduledReportDTO} from "@/models/scheduledReport/CreateScheduledReportDTO";
import {apiGetAuthenticated, apiPostAuthenticated} from "@/services/apiClient";
import {GetScheduledReportDTO} from "@/models/scheduledReport/GetScheduledReportDTO";

const SERVICE_PATH = 'private/schedule'

export async function createScheduledReport(dto: CreateScheduledReportDTO){
    return apiPostAuthenticated(`${SERVICE_PATH}/create`, dto);
}

export async function getUserScheduledReports(): Promise<GetScheduledReportDTO[]>{
    return apiGetAuthenticated(`${SERVICE_PATH}/get`)
}