import CustomTextInput from '@/components/utils/textInput';
import OptionButton from '@/components/utils/OptionButton';
import LlmAnalysisSettings, {
    LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType,
} from '@/components/analysis/LlmAnalysisSettings';
import { useCommonStyles } from '@/constants/useCommonStyles';
import { useState } from 'react';
import { View, Button, Text, Pressable, ScrollView, Platform } from 'react-native';
import { useAuth } from '@/store/AuthProvider';
import { DateRangePicker } from '../utils/DatePicker';
import QRCode from "react-native-qrcode-svg";

export type { LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType };

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
    llmAnalysisEnabled: boolean;
    onLlmAnalysisEnabledChange: (enabled: boolean) => void;
    llmFilterType: LlmFilterType;
    onLlmFilterTypeChange: (type: LlmFilterType) => void;
    commitShas: string;
    onCommitShasChange: (shas: string) => void;
    fromDate: string;
    onFromDateChange: (date: string) => void;
    toDate: string;
    onToDateChange: (date: string) => void;
    promptComplexity: PromptComplexity;
    onPromptComplexityChange: (c: PromptComplexity) => void;
    analysisMode: AnalysisMode;
    onAnalysisModeChange: (m: AnalysisMode) => void;
    requestedAnalyses: AnalysisType[];
    onRequestedAnalysesChange: (analyses: AnalysisType[]) => void;
    onSubmit: () => void;
}

export default function RepositorySearchForm({
                                                 searchType, onSearchTypeChange,
                                                 platform, onPlatformChange,
                                                 text, onTextChange,
                                                 projectName, onProjectNameChange,
                                                 username, onUsernameChange,
                                                 llmAnalysisEnabled, onLlmAnalysisEnabledChange,
                                                 llmFilterType, onLlmFilterTypeChange,
                                                 commitShas, onCommitShasChange,
                                                 fromDate, onFromDateChange,
                                                 toDate, onToDateChange,
                                                 promptComplexity, onPromptComplexityChange,
                                                 analysisMode, onAnalysisModeChange,
                                                 requestedAnalyses, onRequestedAnalysesChange,
                                                 onSubmit,
                                             }: RepositorySearchFormProps) {
    // ATUALIZAR PARA FAZER DEPLOY DO UMA NOVA BUILD MOBILE
    const ANDROID_DOWNLOAD_URL =
        "https://expo.dev/accounts/zetalfa13/projects/git-teller/builds/f13afa30-32ba-4911-9453-29fb3d28a7b8";

    const commonStyles = useCommonStyles();
    const [settingsModalVisible, setSettingsModalVisible] = useState(false);
    const { isAuthenticated } = useAuth();


    return (
        <ScrollView contentContainerStyle={[commonStyles.screen, commonStyles.centered]}>
            {Platform.OS === "web" && (
                <View
                    style={{
                        position: "absolute",
                        top: 20,
                        right: 20,
                        alignItems: "center",
                        zIndex: 1000,
                    }}
                >
                    <Text style={{ marginBottom: 8, fontSize: 12 }}>
                        Android App
                    </Text>

                    <QRCode
                        value={ANDROID_DOWNLOAD_URL}
                        size={100}
                    />

                    <Text style={{ marginTop: 8, fontSize: 11 }}>
                        Scan to download
                    </Text>
                </View>
            )}
            <Text style={commonStyles.formTitle}>Analyze Repository</Text>

            <View style={commonStyles.optionGroup}>
                <OptionButton label="By URL" active={searchType === 'url'} onPress={() => onSearchTypeChange('url')} commonStyles={commonStyles} />
                <OptionButton label="By Project Name" active={searchType === 'project'} onPress={() => onSearchTypeChange('project')} commonStyles={commonStyles} />
            </View>

            {searchType === 'url' ? (
                <View style={commonStyles.inputSection}>
                    <CustomTextInput value={text} onChangeText={onTextChange} placeholder="Enter repository URL" />
                </View>
            ) : (
                <View style={commonStyles.inputSection}>
                    <View style={commonStyles.optionGroup}>
                        <OptionButton label="GitHub" active={platform === 'github'} onPress={() => onPlatformChange('github')} commonStyles={commonStyles} />
                        <OptionButton label="GitLab" active={platform === 'gitlab'} onPress={() => onPlatformChange('gitlab')} commonStyles={commonStyles} />
                    </View>
                    <CustomTextInput value={username} onChangeText={onUsernameChange} placeholder="Username" />
                    <CustomTextInput value={projectName} onChangeText={onProjectNameChange} placeholder="Project Name" />
                </View>
            )}

            {(isAuthenticated) && 
            <View style={commonStyles.toggleSection}>
                <Text style={commonStyles.toggleLabel}>Generic Filters</Text>
                <View style={commonStyles.optionGroup}>
                    <DateRangePicker
                        fromDate={fromDate}
                        toDate={toDate}
                        onConfirm={(from, to) => {
                            onFromDateChange(from);
                            onToDateChange(to);
                        }}
                    />
                </View>
                <Text style={commonStyles.toggleLabel}>LLM Analysis</Text>
                <View style={commonStyles.optionGroup}>
                    <OptionButton
                        label="On"
                        active={llmAnalysisEnabled}
                        onPress={() => {
                            onLlmAnalysisEnabledChange(true);
                            setSettingsModalVisible(true);
                        }}
                        commonStyles={commonStyles}
                    />
                    <OptionButton label="Off" active={!llmAnalysisEnabled} onPress={() => onLlmAnalysisEnabledChange(false)} commonStyles={commonStyles} />
                </View>

                {llmAnalysisEnabled && (
                    <Pressable onPress={() => setSettingsModalVisible(true)}>
                        <Text style={[commonStyles.toggleDescription, { textDecorationLine: 'underline' }]}>
                            Edit LLM Settings
                        </Text>
                    </Pressable>
                )}
            </View>}

            <LlmAnalysisSettings
                visible={settingsModalVisible}
                onClose={() => setSettingsModalVisible(false)}
                onConfirm={() => setSettingsModalVisible(false)}
                llmFilterType={llmFilterType}
                onLlmFilterTypeChange={onLlmFilterTypeChange}
                commitShas={commitShas}
                onCommitShasChange={onCommitShasChange}
                fromDate={fromDate}
                onFromDateChange={onFromDateChange}
                toDate={toDate}
                onToDateChange={onToDateChange}
                promptComplexity={promptComplexity}
                onPromptComplexityChange={onPromptComplexityChange}
                analysisMode={analysisMode}
                onAnalysisModeChange={onAnalysisModeChange}
                requestedAnalyses={requestedAnalyses}
                onRequestedAnalysesChange={onRequestedAnalysesChange}
            />

            <Button title="Analyze" onPress={onSubmit} />
        </ScrollView>
    );
}