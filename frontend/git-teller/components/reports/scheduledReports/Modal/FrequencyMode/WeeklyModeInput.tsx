import {daysOfWeekMode, FrequencyMode, weeklyMode} from "@/models/scheduledReport/CreateScheduledReportDTO";
import {DayOfWeekInput, DaysOfWeekInput} from "@/components/reports/scheduledReports/Modal/FrequencyMode/DaysOfWeekInput";
import OptionButton from "@/components/OptionButton";
import {commonStyles} from "@/constants/commonStyles";
import {useCommonStyles} from "@/constants/useCommonStyles";
import {View, Text} from "react-native";

interface Props {
    value: WeeklyFrequencyMode;
    onChange: (value: FrequencyMode) => void;
}

type WeeklyFrequencyMode =
    | Extract<FrequencyMode, { type: 'WEEKLY' }>
    | Extract<FrequencyMode, { type: 'DAYS_OF_WEEK' }>;

export default function WeeklyModeInput({value, onChange}: Props) {
    const multiDay = value.type === 'DAYS_OF_WEEK';

    const styles = useCommonStyles();
    const commonStyles = useCommonStyles();

    return (
        <>
            <View style={styles.optionGroup}>
                <OptionButton
                    active={!multiDay}
                    label="Single day"
                    onPress={ () => onChange(weeklyMode( value.type === 'WEEKLY' ? value.dayOfWeek : 'MONDAY' )) }
                    commonStyles={commonStyles}
                />

                <OptionButton
                    active={multiDay}
                    label="Multiple days"
                    onPress={() => onChange(daysOfWeekMode( value.type === 'DAYS_OF_WEEK' ? value.daysOfWeek : [] ))}
                    commonStyles={commonStyles}
                />
            </View>

            {value.type === 'WEEKLY' && (
                <DayOfWeekInput
                    value={value.dayOfWeek}
                    onChange={(dayOfWeek) =>
                        onChange({
                            ...value,
                            dayOfWeek,
                        })
                    }
                />
            )}

            {value.type === 'DAYS_OF_WEEK' && (
                <DaysOfWeekInput
                    value={value.daysOfWeek}
                    onChange={(daysOfWeek) =>
                        onChange({
                            ...value,
                            daysOfWeek,
                        })
                    }
                />
            )}
        </>
    );
}