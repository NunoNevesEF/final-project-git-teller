import { Text, TouchableOpacity } from 'react-native';

export interface OptionButtonProps {
    label: string;
    active: boolean;
    onPress: () => void;
    commonStyles: any;
}

export default function OptionButton({ label, active, onPress, commonStyles }: OptionButtonProps) {
    return (
        <TouchableOpacity
            style={[commonStyles.optionButton, active && commonStyles.optionButtonActive]}
            onPress={onPress}
        >
            <Text style={[commonStyles.optionButtonText, active && commonStyles.optionButtonTextActive]}>
                {label}
            </Text>
        </TouchableOpacity>
    );
}