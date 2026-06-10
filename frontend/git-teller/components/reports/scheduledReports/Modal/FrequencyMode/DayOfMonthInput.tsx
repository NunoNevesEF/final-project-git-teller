import { TextInput } from 'react-native';
import {useCommonStyles} from "@/constants/useCommonStyles";

export function DayOfMonthInput({value, onChange}: {
    value: number;
    onChange: (value: number) => void;
}) {
    const styles = useCommonStyles();

    return (
        <TextInput
            value={String(value)}
            keyboardType="number-pad"
            placeholder="Day of month"
            onChangeText={(text) => {
                const parsed = Number(text);

                if (
                    !Number.isNaN(parsed) &&
                    parsed >= 1 &&
                    parsed <= 31
                ) {
                    onChange(parsed);
                }
            }}
            style={styles.searchInput}
        />
    );
}