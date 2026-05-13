import CustomTextInput from '@/components/textInput';
import { useCommonStyles } from '@/constants/useCommonStyles';
import { View, Button, Text, TouchableOpacity } from 'react-native';

interface RepositorySearchFormProps {
    searchType: 'url' | 'project';
    onSearchTypeChange: (type: 'url' | 'project') => void;
    platform: 'github' | 'gitlab';
    onPlatformChange: (platform: 'github' | 'gitlab') => void;
    text: string;
    onTextChange: (text: string) => void;
    projectName: string;
    onProjectNameChange: (name: string) => void;
    username: string;
    onUsernameChange: (username: string) => void;
    onSubmit: () => void;
}

export default function RepositorySearchForm({
                                                 searchType,
                                                 onSearchTypeChange,
                                                 platform,
                                                 onPlatformChange,
                                                 text,
                                                 onTextChange,
                                                 projectName,
                                                 onProjectNameChange,
                                                 username,
                                                 onUsernameChange,
                                                 onSubmit,
                                             }: RepositorySearchFormProps) {
    const commonStyles = useCommonStyles();
    return (
        <View style={[commonStyles.screen, commonStyles.centered]}>
            <Text style={commonStyles.formTitle}>Analyze Repository</Text>

            <View style={commonStyles.optionGroup}>
                <OptionButton
                    label="By URL"
                    active={searchType === 'url'}
                    onPress={() => onSearchTypeChange('url')}
                    commonStyles={commonStyles}
                />
                <OptionButton
                    label="By Project Name"
                    active={searchType === 'project'}
                    onPress={() => onSearchTypeChange('project')}
                    commonStyles={commonStyles}
                />
            </View>

            {searchType === 'url' ? (
                <View style={commonStyles.inputSection}>
                    <CustomTextInput
                        value={text}
                        onChangeText={onTextChange}
                        placeholder="Enter repository URL"
                    />
                </View>
            ) : (
                <View style={commonStyles.inputSection}>
                    <View style={commonStyles.optionGroup}>
                        <OptionButton
                            label="GitHub"
                            active={platform === 'github'}
                            onPress={() => onPlatformChange('github')}
                            commonStyles={commonStyles}
                        />
                        <OptionButton
                            label="GitLab"
                            active={platform === 'gitlab'}
                            onPress={() => onPlatformChange('gitlab')}
                            commonStyles={commonStyles}
                        />
                    </View>

                    <CustomTextInput
                        value={username}
                        onChangeText={onUsernameChange}
                        placeholder="Username"
                    />
                    <CustomTextInput
                        value={projectName}
                        onChangeText={onProjectNameChange}
                        placeholder="Project Name"
                    />
                </View>
            )}

            <Button title="Analyze" onPress={onSubmit} />
        </View>
    );
}

interface OptionButtonProps {
    label: string;
    active: boolean;
    onPress: () => void;
    commonStyles: any
}

function OptionButton({ label, active, onPress, commonStyles }: OptionButtonProps) {
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
