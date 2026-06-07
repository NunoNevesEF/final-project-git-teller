import CustomTextInput from '@/components/textInput';
import { useCommonStyles } from '@/constants/useCommonStyles';
import { View, Button, Text, TouchableOpacity, ScrollView } from 'react-native';

export type LlmFilterType = 'shas' | 'dateRange' | 'overview';
export type PromptComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX';
export type AnalysisMode = 'DIFF' | 'META';
export type AnalysisType =
    | 'DEFAULT' | 'SECURITY' | 'QUALITY' | 'TESTS' | 'PERFORMANCE'
    | 'COMMENTS' | 'IMPACT' | 'DEVELOPER_EXPERIENCE' | 'ARCHITECTURE'
    | 'RISK' | 'DEPENDENCIES' | 'BREAKING_CHANGES' | 'DOCUMENTATION';

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
    const commonStyles = useCommonStyles();

    const ANALYSIS_TYPES: { value: AnalysisType; label: string }[] = [
        { value: 'DEFAULT',              label: 'Default' },
        { value: 'SECURITY',             label: 'Security' },
        { value: 'QUALITY',              label: 'Quality' },
        { value: 'TESTS',                label: 'Tests' },
        { value: 'PERFORMANCE',          label: 'Performance' },
        { value: 'COMMENTS',             label: 'Comments' },
        { value: 'IMPACT',               label: 'Impact' },
        { value: 'DEVELOPER_EXPERIENCE', label: 'Dev Experience' },
        { value: 'ARCHITECTURE',         label: 'Architecture' },
        { value: 'RISK',                 label: 'Risk' },
        { value: 'DEPENDENCIES',         label: 'Dependencies' },
        { value: 'BREAKING_CHANGES',     label: 'Breaking Changes' },
        { value: 'DOCUMENTATION',        label: 'Documentation' },
    ];

    const maxAnalyses = promptComplexity === 'SIMPLE' ? 2 : promptComplexity === 'MEDIUM' ? 3 : 4;

    return (
        <ScrollView contentContainerStyle={[commonStyles.screen, commonStyles.centered]}>
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

            <View style={commonStyles.toggleSection}>
                <Text style={commonStyles.toggleLabel}>LLM Analysis</Text>
                <View style={commonStyles.optionGroup}>
                    <OptionButton label="On" active={llmAnalysisEnabled} onPress={() => onLlmAnalysisEnabledChange(true)} commonStyles={commonStyles} />
                    <OptionButton label="Off" active={!llmAnalysisEnabled} onPress={() => onLlmAnalysisEnabledChange(false)} commonStyles={commonStyles} />
                </View>
            </View>

            {llmAnalysisEnabled && (
                <View style={commonStyles.inputSection}>

                    <Text style={commonStyles.toggleLabel}>Filter by</Text>
                    <View style={commonStyles.optionGroup}>
                        <OptionButton label="Overview" active={llmFilterType === 'overview'} onPress={() => onLlmFilterTypeChange('overview')} commonStyles={commonStyles} />
                        <OptionButton label="Date Range" active={llmFilterType === 'dateRange'} onPress={() => onLlmFilterTypeChange('dateRange')} commonStyles={commonStyles} />
                        <OptionButton label="Commit SHAs" active={llmFilterType === 'shas'} onPress={() => onLlmFilterTypeChange('shas')} commonStyles={commonStyles} />
                    </View>

                    {llmFilterType === 'shas' && (
                        <CustomTextInput
                            value={commitShas}
                            onChangeText={onCommitShasChange}
                            placeholder="Commit SHAs (comma separated)"
                        />
                    )}

                    {llmFilterType === 'dateRange' && (
                        <>
                            <CustomTextInput value={fromDate} onChangeText={onFromDateChange} placeholder="From (YYYY-MM-DD)" />
                            <CustomTextInput value={toDate} onChangeText={onToDateChange} placeholder="To (YYYY-MM-DD)" />
                        </>
                    )}

                    {llmFilterType !== 'overview' && (
                        <>
                            <Text style={commonStyles.toggleLabel}>Prompt Complexity</Text>
                            <View style={commonStyles.optionGroup}>
                                <OptionButton label="Simple" active={promptComplexity === 'SIMPLE'} onPress={() => onPromptComplexityChange('SIMPLE')} commonStyles={commonStyles} />
                                <OptionButton label="Medium" active={promptComplexity === 'MEDIUM'} onPress={() => onPromptComplexityChange('MEDIUM')} commonStyles={commonStyles} />
                                <OptionButton label="Complex" active={promptComplexity === 'COMPLEX'} onPress={() => onPromptComplexityChange('COMPLEX')} commonStyles={commonStyles} />
                            </View>

                            <Text style={commonStyles.toggleLabel}>Analysis Mode</Text>
                            <View style={commonStyles.optionGroup}>
                                <OptionButton label="Diff" active={analysisMode === 'DIFF'} onPress={() => onAnalysisModeChange('DIFF')} commonStyles={commonStyles} />
                                <OptionButton label="Meta" active={analysisMode === 'META'} onPress={() => onAnalysisModeChange('META')} commonStyles={commonStyles} />
                            </View>

                            {analysisMode === 'META' && (
                                <>
                                    <Text style={commonStyles.toggleLabel}>
                                        Requested Analyses ({requestedAnalyses.length}/{maxAnalyses})
                                    </Text>
                                    <View style={[commonStyles.optionGroup, { flexWrap: 'wrap', justifyContent: 'flex-start' }]}>
                                        {ANALYSIS_TYPES.map((type) => {
                                            const isSelected = requestedAnalyses.includes(type.value);
                                            const isDisabled = !isSelected && requestedAnalyses.length >= maxAnalyses;
                                            return (
                                                <TouchableOpacity
                                                    key={type.value}
                                                    style={[
                                                        commonStyles.optionButton,
                                                        isSelected && commonStyles.optionButtonActive,
                                                        isDisabled && { opacity: 0.35 },
                                                    ]}
                                                    onPress={() => {
                                                        if (isDisabled) return;
                                                        if (isSelected) {
                                                            onRequestedAnalysesChange(requestedAnalyses.filter(a => a !== type.value));
                                                        } else {
                                                            onRequestedAnalysesChange([...requestedAnalyses, type.value]);
                                                        }
                                                    }}
                                                >
                                                    <Text style={[commonStyles.optionButtonText, isSelected && commonStyles.optionButtonTextActive]}>
                                                        {type.label}
                                                    </Text>
                                                </TouchableOpacity>
                                            );
                                        })}
                                    </View>
                                </>
                            )}
                        </>
                    )}

                </View>
            )}

            <Button title="Analyze" onPress={onSubmit} />
        </ScrollView>
    );
}

interface OptionButtonProps {
    label: string;
    active: boolean;
    onPress: () => void;
    commonStyles: any;
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

