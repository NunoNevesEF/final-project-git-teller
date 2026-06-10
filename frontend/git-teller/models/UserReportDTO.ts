import {GitAnalysis} from "@/models/GitAnalysis";

export interface CreateUserReportDTO{
    gitAnalysis: GitAnalysis,
    repoUri: string
}

export interface UserReportDTO{
    id: number,
    createdAt: string,
    repoUri: string,
}