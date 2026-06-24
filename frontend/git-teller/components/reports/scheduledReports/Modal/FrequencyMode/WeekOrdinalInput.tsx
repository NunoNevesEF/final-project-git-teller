import { Pressable, Text, View } from 'react-native';
import { WeekOrdinal } from '@/models/scheduledReport/CreateScheduledReportDTO';
import OptionButton from "@/components/utils/OptionButton";
import {commonStyles} from "@/constants/commonStyles";
import {useCommonStyles} from "@/constants/useCommonStyles";

const ORDINALS: WeekOrdinal[] = [
    'FIRST',
    'SECOND',
    'THIRD',
    'FOURTH',
    'LAST',
];

export function WeekOrdinalInput({value, onChange}: {
    value: WeekOrdinal;
    onChange: (value: WeekOrdinal) => void;
}) {
    const styles = useCommonStyles();
    const commonStyles = useCommonStyles();

    return (
        <View style={styles.optionGroup}>
            {ORDINALS.map((ordinal) => (
                <OptionButton
                    key={ordinal}
                    label={ordinal}
                    active={value === ordinal}
                    onPress={() => onChange(ordinal)}
                    commonStyles={commonStyles}
                />
            ))}
        </View>
    );
}