export interface IGetScheduledReportDTO {
    id: number;
    repoUri: string;
    nextRunAt?: string;
    lastRunAt?: string;
    dataFrom: string;
    isCancelled: boolean;
    cancellationReason: string | null;
}

export interface OneTimeScheduledReportDTO extends IGetScheduledReportDTO {
    type: 'ONE_TIME';
}

export interface PeriodicScheduledReportDTO extends IGetScheduledReportDTO {
    type: 'PERIODIC';
    active: boolean;
    timeZone: string;
}

export type GetScheduledReportDTO =
    | OneTimeScheduledReportDTO
    | PeriodicScheduledReportDTO
