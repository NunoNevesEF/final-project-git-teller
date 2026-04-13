import CustomTextInput from '@/components/textInput';
import { View, Button, StyleSheet, Text, TouchableOpacity } from 'react-native';

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
        <View style={styles.container}>
            <Text style={styles.title}>Analyze Repository</Text>

            <View style={styles.optionGroup}>
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
                <View style={styles.inputSection}>
                    <CustomTextInput
                        value={text}
                        onChangeText={onTextChange}
                        placeholder="Enter repository URL"
                    />
                </View>
            ) : (
                <View style={styles.inputSection}>
                    <View style={styles.optionGroup}>
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
            style={[styles.button, active && styles.buttonActive]}
            onPress={onPress}
        >
            <Text style={[styles.buttonText, active && styles.buttonTextActive]}>
                {label}
            </Text>
        </TouchableOpacity>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 20,
        backgroundColor: '#f5f5f5',
    },
    title: {
        fontSize: 28,
        fontWeight: '700',
        marginBottom: 30,
        color: '#333',
    },
    optionGroup: {
        flexDirection: 'row',
        gap: 12,
        marginBottom: 20,
        width: '100%',
        justifyContent: 'center',
    },
    button: {
        paddingVertical: 8,
        paddingHorizontal: 16,
        borderRadius: 6,
        backgroundColor: '#e8e8e8',
        alignItems: 'center',
    },
    buttonActive: {
        backgroundColor: '#007AFF',
    },
    buttonText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
    },
    buttonTextActive: {
        color: '#fff',
    },
    inputSection: {
        width: '100%',
        marginBottom: 20,
        gap: 12,
        maxWidth: 300,
        alignSelf: 'center',
    },
});
