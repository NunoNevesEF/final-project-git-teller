import {apiGetAuthenticated, apiGetBlob, apiPostAuthenticated, apiPostBlob} from "@/services/apiClient";
import {CreateUserReportDTO, UserReportDTO} from "@/models/UserReportDTO";
import {GitAnalysis} from "@/models/GitAnalysis";
import {getTokens} from "@/services/secureStore";

const SERVICE_PATH = "private/report"

export async function createReport(dto: CreateUserReportDTO): Promise<number>{
    return apiPostAuthenticated(`${SERVICE_PATH}/create`, dto);
}

export async function getUserReports(): Promise<UserReportDTO[]>{
    return apiGetAuthenticated(`${SERVICE_PATH}/user-reports`)
}

export async function getReportAnalysis(reportId: number): Promise<GitAnalysis>{
    return apiGetAuthenticated(`${SERVICE_PATH}/user-reports/${reportId}/analysis`)
}

export async function downloadPdf(reportId: number): Promise<Blob>{
    const { accessToken } = await getTokens()

    return apiGetBlob(`${SERVICE_PATH}/user-reports/${reportId}/download`, accessToken)
}