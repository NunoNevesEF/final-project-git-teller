import {FrequencyMode} from "@/models/scheduledReport/CreateScheduledReportDTO";
import {DayOfWeekInput} from "@/components/reports/scheduledReports/Modal/FrequencyMode/DaysOfWeekInput";
import {WeekOrdinalInput} from "@/components/reports/scheduledReports/Modal/FrequencyMode/WeekOrdinalInput";
import {DayOfMonthInput} from "@/components/reports/scheduledReports/Modal/FrequencyMode/DayOfMonthInput";
import OptionButton from "@/components/OptionButton";
import {commonStyles} from "@/constants/commonStyles";
import {View, Text} from "react-native";
import {useCommonStyles} from "@/constants/useCommonStyles";

type MonthlyFrequencyMode =
    | Extract<FrequencyMode, { type: 'MONTHLY_DAY_OF_MONTH' }>
    | Extract<FrequencyMode, { type: 'MONTHLY_DAY_OF_N_WEEK' }>
    | Extract<FrequencyMode, { type: 'MONTHLY_LAST_DAY' }>;

interface Props {
    value: MonthlyFrequencyMode;
    onChange: (value: FrequencyMode) => void;
}

export default function MonthlyModeInput({value, onChange}: Props) {
    const styles = useCommonStyles();
    const commonStyles = useCommonStyles();

    const monthlyType =
        value.type === 'MONTHLY_DAY_OF_MONTH' ? 'DAY_OF_MONTH' :
            value.type === 'MONTHLY_DAY_OF_N_WEEK' ? 'N_WEEK' : 'LAST_DAY';

    return (
        <>
            <View style={styles.optionGroup}>
                <OptionButton
                    active={monthlyType === 'DAY_OF_MONTH'}
                    onPress={() =>
                        onChange({
                            type: 'MONTHLY_DAY_OF_MONTH',
                            dayOfMonth: 1,
                        })
                    }
                    label="Day of month"
                    commonStyles={commonStyles}
                />

                <OptionButton
                    active={monthlyType === 'N_WEEK'}
                    onPress={() =>
                        onChange({
                            type: 'MONTHLY_DAY_OF_N_WEEK',
                            dayOfWeek: 'MONDAY',
                            weekOrdinal: 'FIRST',
                        })
                    }
                    label="Nth weekday"
                    commonStyles={commonStyles}
                />

                <OptionButton
                    active={monthlyType === 'LAST_DAY'}
                    onPress={() =>
                        onChange({
                            type: 'MONTHLY_LAST_DAY',
                        })
                    }
                    label="Last day"
                    commonStyles={commonStyles}
                />
            </View>

            {value.type === 'MONTHLY_DAY_OF_MONTH' && (
                <DayOfMonthInput
                    value={value.dayOfMonth}
                    onChange={(dayOfMonth) =>
                        onChange({
                            ...value,
                            dayOfMonth,
                        })
                    }
                />
            )}

            {value.type === 'MONTHLY_DAY_OF_N_WEEK' && (
                <>
                    <WeekOrdinalInput
                        value={value.weekOrdinal}
                        onChange={(weekOrdinal) =>
                            onChange({
                                ...value,
                                weekOrdinal,
                            })
                        }
                    />

                    <DayOfWeekInput
                        value={value.dayOfWeek}
                        onChange={(dayOfWeek) =>
                            onChange({
                                ...value,
                                dayOfWeek,
                            })
                        }
                    />
                </>
            )}
        </>
    );
}