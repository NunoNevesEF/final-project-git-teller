export enum JobStatus {
    PENDING = "PENDING",
    RUNNING = "RUNNING",
    SUCCESS = "SUCCESS",
    FAILURE = "FAILURE",
}

export interface ScheduledReportJobListItemDTO {
    dataTo: string;
    dataFrom: string;
    scheduledFor: string;
    repoUri: string;
    status: JobStatus;
    retryCount: number;
}