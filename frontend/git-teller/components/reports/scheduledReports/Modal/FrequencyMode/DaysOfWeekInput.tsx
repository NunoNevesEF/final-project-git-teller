import {DayOfWeek} from "@/models/scheduledReport/CreateScheduledReportDTO";
import OptionButton from "@/components/utils/OptionButton";
import {commonStyles} from "@/constants/commonStyles";
import {View} from "react-native";
import {useCommonStyles} from "@/constants/useCommonStyles";

const DAYS: DayOfWeek[] = [
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY',
];

export function DayOfWeekInput({value, onChange}: {
    value: DayOfWeek;
    onChange: (value: DayOfWeek) => void;
}) {
    const styles = useCommonStyles();
    const commonStyles = useCommonStyles();

    return (
        <View style={styles.pickerGrid}>
            {DAYS.map((day) => (
                <OptionButton
                    key={day}
                    label={day.slice(0, 3)}
                    active={value === day}
                    onPress={() => onChange(day)}
                    commonStyles={commonStyles}
                />
            ))}
        </View>
    );
}

export function DaysOfWeekInput({value, onChange}: {
    value: DayOfWeek[];
    onChange: (value: DayOfWeek[]) => void;
}) {
    const styles = useCommonStyles();
    const commonStyles = useCommonStyles();

    const toggle = (day: DayOfWeek) => {
        if (value.includes(day)) {
            onChange(value.filter((d) => d !== day));
        } else {
            onChange([...value, day]);
        }
    };

    return (
        <View style={styles.pickerGrid}>
            {DAYS.map((day) => (
                <OptionButton
                    key={day}
                    label={day.slice(0, 3)}
                    active={value.includes(day)}
                    onPress={() => toggle(day)}
                    commonStyles={commonStyles}
                />
            ))}
        </View>
    );
}