import {FrequencyMode} from "@/models/scheduledReport/CreateScheduledReportDTO";
import {MonthInput} from "@/components/reports/scheduledReports/Modal/FrequencyMode/MonthInput";
import {DayOfMonthInput} from "@/components/reports/scheduledReports/Modal/FrequencyMode/DayOfMonthInput";

type YearlyFrequencyMode = Extract<FrequencyMode, { type: 'YEARLY' }>;

interface Props {
    value: YearlyFrequencyMode;
    onChange: (value: FrequencyMode) => void;
}

export default function YearlyEditor({value, onChange}: Props) {
    return (
        <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            <MonthInput
                value={value.month}
                onChange={(month) =>
                    onChange({
                        ...value,
                        month,
                    })
                }
            />

            <DayOfMonthInput
                value={value.dayOfMonth}
                onChange={(dayOfMonth) =>
                    onChange({
                        ...value,
                        dayOfMonth,
                    })
                }
            />
        </div>
    );
}