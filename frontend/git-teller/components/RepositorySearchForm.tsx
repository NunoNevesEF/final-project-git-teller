import CustomTextInput from '@/components/textInput';
import { commonStyles } from '@/constants/commonStyles';
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
    return (
        <View style={[commonStyles.screen, commonStyles.centered]}>
            <Text style={commonStyles.formTitle}>Analyze Repository</Text>

            <View style={commonStyles.optionGroup}>
                <OptionButton
                    label="By URL"
                    active={searchType === 'url'}
                    onPress={() => onSearchTypeChange('url')}
                />
                <OptionButton
                    label="By Project Name"
                    active={searchType === 'project'}
                    onPress={() => onSearchTypeChange('project')}
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
                        />
                        <OptionButton
                            label="GitLab"
                            active={platform === 'gitlab'}
                            onPress={() => onPlatformChange('gitlab')}
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
}

function OptionButton({ label, active, onPress }: OptionButtonProps) {
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
