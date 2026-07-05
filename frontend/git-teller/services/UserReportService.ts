import authApiClient from "@/services/authApiClient";
import {CreateUserReportDTO, UserReportDTO} from "@/models/UserReportDTO";
import {GitAnalysis} from "@/models/GitAnalysis";

const SERVICE_PATH = "/api/private/report"

export async function createReport(dto: CreateUserReportDTO): Promise<number>{
    return (await authApiClient.post<number>(`${SERVICE_PATH}/create`, dto)).data;
}

export async function getUserReports(): Promise<UserReportDTO[]>{
    const userReports = (await authApiClient.get<UserReportDTO[]>(`${SERVICE_PATH}/user-reports`)).data;
    return userReports.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
}

export async function getReportAnalysis(reportId: number): Promise<GitAnalysis>{
    return (await authApiClient.get<GitAnalysis>(`${SERVICE_PATH}/user-reports/${reportId}/analysis`)).data;
}

export async function downloadPdf(reportId: number): Promise<Blob>{
    return (await authApiClient.get<Blob>(`${SERVICE_PATH}/user-reports/${reportId}/download`, { responseType: 'blob' })).data;
}
