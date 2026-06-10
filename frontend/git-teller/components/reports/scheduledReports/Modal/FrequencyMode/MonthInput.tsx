import OptionButton from "@/components/OptionButton";
import {commonStyles} from "@/constants/commonStyles";
import {View} from "react-native";
import {useCommonStyles} from "@/constants/useCommonStyles";

const MONTHS = [
    { value: 1, label: 'Jan' },
    { value: 2, label: 'Feb' },
    { value: 3, label: 'Mar' },
    { value: 4, label: 'Apr' },
    { value: 5, label: 'May' },
    { value: 6, label: 'Jun' },
    { value: 7, label: 'Jul' },
    { value: 8, label: 'Aug' },
    { value: 9, label: 'Sep' },
    { value: 10, label: 'Oct' },
    { value: 11, label: 'Nov' },
    { value: 12, label: 'Dec' },
];

export function MonthInput({value, onChange}: {
    value: number;
    onChange: (value: number) => void;
}) {
    const styles = useCommonStyles();
    const commonStyles = useCommonStyles();

    return (
        <View style={styles.pickerGrid}>
            {MONTHS.map((month) => (
                <OptionButton
                    key={month.value}
                    label={month.label}
                    active={value === month.value}
                    onPress={() => onChange(month.value)}
                    commonStyles={commonStyles}
                />
            ))}
        </View>
    );
}