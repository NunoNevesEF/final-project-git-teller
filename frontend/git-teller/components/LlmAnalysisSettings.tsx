import { Modal, Pressable, ScrollView, Text, TouchableOpacity, View } from 'react-native';
import CustomTextInput from '@/components/textInput';
import OptionButton from '@/components/OptionButton';
import { useCommonStyles } from '@/constants/useCommonStyles';

export type LlmFilterType = 'shas' | 'dateRange' | 'overview';
export type PromptComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX';
export type AnalysisMode = 'DIFF' | 'META';
export type AnalysisType =
    | 'DEFAULT' | 'SECURITY' | 'QUALITY' | 'TESTS' | 'PERFORMANCE'
    | 'COMMENTS' | 'IMPACT' | 'DEVELOPER_EXPERIENCE' | 'ARCHITECTURE'
    | 'RISK' | 'DEPENDENCIES' | 'BREAKING_CHANGES' | 'DOCUMENTATION';

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

interface LlmAnalysisSettingsProps {
    visible: boolean;
    onClose: () => void;
    onConfirm: () => void;
    confirmLabel?: string;
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
}

export default function LlmAnalysisSettings({
                                                     visible, onClose, onConfirm, confirmLabel = 'Done',
                                                     llmFilterType, onLlmFilterTypeChange,
                                                     commitShas, onCommitShasChange,
                                                     fromDate, onFromDateChange,
                                                     toDate, onToDateChange,
                                                     promptComplexity, onPromptComplexityChange,
                                                     analysisMode, onAnalysisModeChange,
                                                     requestedAnalyses, onRequestedAnalysesChange,
                                                 }: LlmAnalysisSettingsProps) {
    const commonStyles = useCommonStyles();
    const maxAnalyses = promptComplexity === 'SIMPLE' ? 2 : promptComplexity === 'MEDIUM' ? 3 : 4;

    return (
        <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
            <Pressable
                style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.35)', justifyContent: 'center', alignItems: 'center' }}
                onPress={onClose}
            >
                <Pressable
                    onPress={() => {}}
                    style={{ width: '90%', maxWidth: 360, maxHeight: '85%', backgroundColor: '#fff', borderRadius: 12, padding: 16 }}
                >
                    <ScrollView showsVerticalScrollIndicator={false}>
                        <Text style={[commonStyles.formTitle, { marginBottom: 12 }]}>LLM Analysis Settings</Text>

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

                                {analysisMode === 'DIFF' && (
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

                        <Pressable style={[commonStyles.primaryButton, { marginTop: 16 }]} onPress={onConfirm}>
                            <Text style={commonStyles.primaryButtonText}>{confirmLabel}</Text>
                        </Pressable>
                    </ScrollView>
                </Pressable>
            </Pressable>
        </Modal>
    );
}