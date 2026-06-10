import {Pressable, Text, View} from "react-native";
import {
    dailyMode,
    FREQUENCY_MODE_VALUES,
    FrequencyMode, monthlyMode,
    weeklyMode, yearlyMode
} from "@/models/scheduledReport/CreateScheduledReportDTO";
import MonthlyModeInput from "@/components/reports/scheduledReports/Modal/FrequencyMode/MonthlyModeInput";
import WeeklyModeInput from "@/components/reports/scheduledReports/Modal/FrequencyMode/WeeklyModeInput";
import YearlyModeInput from "@/components/reports/scheduledReports/Modal/FrequencyMode/YearlyModeInput";
import {useCommonStyles} from "@/constants/useCommonStyles";

type FrequencyCategory =
    | 'DAILY'
    | 'WEEKLY'
    | 'MONTHLY'
    | 'YEARLY';

interface Props {
    value: FrequencyMode;
    onChange: (value: FrequencyMode) => void;
}

export default function FrequencyModeInput({value, onChange}: Props) {
    const styles = useCommonStyles();

    const modeToCategoryConverter = (mode: FrequencyMode) : FrequencyCategory => {
        switch(mode.type){
            case 'DAILY': return 'DAILY';

            case 'WEEKLY':
            case 'DAYS_OF_WEEK':
                return 'WEEKLY';

            case 'MONTHLY_DAY_OF_MONTH':
            case "MONTHLY_DAY_OF_N_WEEK":
            case "MONTHLY_LAST_DAY":
                return 'MONTHLY';

            case 'YEARLY': return 'YEARLY';
        }
    }

    const categoryToModeConverter = (category: FrequencyCategory): FrequencyMode => {
        switch(category){
            case 'DAILY': return dailyMode();
            case 'WEEKLY': return weeklyMode('MONDAY');
            case 'MONTHLY': return monthlyMode(1);
            case 'YEARLY': return yearlyMode(1, 1)
        }
    }

    const category: FrequencyCategory = modeToCategoryConverter(value)
    const setCategory = (category: FrequencyCategory) => onChange(categoryToModeConverter(category));

    return (
        <View>
            <View style={styles.optionGroup}>
                {(['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'] as const).map(
                    (option) => (
                        <Pressable
                            key={option}
                            onPress={() => setCategory(option)}
                            style={[
                                styles.optionButton,
                                category === option &&
                                styles.optionButtonActive,
                            ]}
                        >
                            <Text>{option}</Text>
                        </Pressable>
                    )
                )}
            </View>

            {(
                value.type === 'WEEKLY' ||
                value.type === 'DAYS_OF_WEEK'
            ) && (
                <WeeklyModeInput
                    value={value}
                    onChange={onChange}
                />
            )}

            {(
                value.type === 'MONTHLY_DAY_OF_MONTH' ||
                value.type ==='MONTHLY_DAY_OF_N_WEEK' ||
                value.type ==='MONTHLY_LAST_DAY'
            ) && (
                <MonthlyModeInput
                    value={value}
                    onChange={onChange}
                />
            )}

            {value.type === 'YEARLY' && (
                <YearlyModeInput
                    value={value}
                    onChange={onChange}
                />
            )}
        </View>
    );
}

