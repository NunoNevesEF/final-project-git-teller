export type WeekOrdinal = "FIRST" | "SECOND" | "THIRD" | "FOURTH" | "LAST";

export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

export type FrequencyMode =
  | { type: "DAILY" }
  | { type: "WEEKLY"; dayOfWeek: DayOfWeek }
  | { type: "DAYS_OF_WEEK"; daysOfWeek: DayOfWeek[] }
  | { type: "MONTHLY_DAY_OF_MONTH"; dayOfMonth: number }
  | {
      type: "MONTHLY_DAY_OF_N_WEEK";
      dayOfWeek: DayOfWeek;
      weekOrdinal: WeekOrdinal;
    }
  | { type: "MONTHLY_LAST_DAY" }
  | { type: "YEARLY"; dayOfMonth: number; month: number };

export type FrequencyModeType = FrequencyMode["type"];

export const FREQUENCY_MODE_VALUES: FrequencyModeType[] = [
  "DAILY",
  "WEEKLY",
  "DAYS_OF_WEEK",
  "MONTHLY_DAY_OF_MONTH",
  "MONTHLY_DAY_OF_N_WEEK",
  "MONTHLY_LAST_DAY",
  "YEARLY",
];

export const dailyMode = (): FrequencyMode => ({ type: "DAILY" });

export const weeklyMode = (dayOfWeek: DayOfWeek): FrequencyMode => ({
  type: "WEEKLY",
  dayOfWeek,
});

export const daysOfWeekMode = (daysOfWeek: DayOfWeek[]): FrequencyMode => ({
  type: "DAYS_OF_WEEK",
  daysOfWeek,
});

export const monthlyMode = (dayOfMonth: number): FrequencyMode => ({
  type: "MONTHLY_DAY_OF_MONTH",
  dayOfMonth,
});

export const monthlyDayOfNWeekMode = (
  dayOfWeek: DayOfWeek,
  weekOrdinal: WeekOrdinal,
): FrequencyMode => ({ type: "MONTHLY_DAY_OF_N_WEEK", dayOfWeek, weekOrdinal });

export const monthlyLastDayMode = (): FrequencyMode => ({
  type: "MONTHLY_LAST_DAY",
});

export const yearlyMode = (
  dayOfMonth: number,
  month: number,
): FrequencyMode => ({ type: "YEARLY", dayOfMonth, month });

export interface CreateOneTimeScheduledReportDTO {
  type: "ONE_TIME";
  repoUri: string;
  dataStart: string | null;
  runAt: string | null;
  llmConfig?: any;
}

export interface CreatePeriodicScheduledReportDTO {
  type: "PERIODIC";
  repoUri: string;
  timeZone: string;
  time: string;
  freqMode: FrequencyMode;
  llmConfig?: any;
  gitAccountId: number | null;
}

export type CreateScheduledReportDTO =
  | CreateOneTimeScheduledReportDTO
  | CreatePeriodicScheduledReportDTO;
