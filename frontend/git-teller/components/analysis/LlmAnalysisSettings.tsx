import { Modal, Pressable, ScrollView, Text, TouchableOpacity, View } from 'react-native';
import CustomTextInput from '@/components/utils/textInput';
import OptionButton from '@/components/utils/OptionButton';
import TooltipLabel from '@/components/utils/TooltipLabel';
import { useCommonStyles } from '@/constants/useCommonStyles';
import { ANALYSIS_TYPES, AnalysisType } from '@/constants/analysisTypes';

export type LlmFilterType = 'shas' | 'dateRange' | 'overview';
export type PromptComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX';
export type AnalysisMode = 'DIFF' | 'META';
export type { AnalysisType };

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

                        <TooltipLabel
                            label="Filter by"
                            tooltip="Select which commits the LLM will analyse. Overview uses the full repo summary; Date Range and Commit SHAs target specific commits."
                            style={commonStyles.toggleLabel}
                        />
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
                                <TooltipLabel
                                    label="Prompt Complexity"
                                    tooltip="Simple: short summary (8 lines). Medium: structured with impact and risks. Complex: deep evidence-based review."
                                    style={commonStyles.toggleLabel}
                                />
                                <View style={commonStyles.optionGroup}>
                                    <OptionButton label="Simple" active={promptComplexity === 'SIMPLE'} onPress={() => onPromptComplexityChange('SIMPLE')} commonStyles={commonStyles} />
                                    <OptionButton label="Medium" active={promptComplexity === 'MEDIUM'} onPress={() => onPromptComplexityChange('MEDIUM')} commonStyles={commonStyles} />
                                    <OptionButton label="Complex" active={promptComplexity === 'COMPLEX'} onPress={() => onPromptComplexityChange('COMPLEX')} commonStyles={commonStyles} />
                                </View>

                                <TooltipLabel
                                    label="Analysis Mode"
                                    tooltip="Diff: sends actual code changes, more accurate but slower. Meta: sends only file names and stats, faster but less detailed."
                                    style={commonStyles.toggleLabel}
                                />
                                <View style={commonStyles.optionGroup}>
                                    <OptionButton label="Diff" active={analysisMode === 'DIFF'} onPress={() => onAnalysisModeChange('DIFF')} commonStyles={commonStyles} />
                                    <OptionButton label="Meta" active={analysisMode === 'META'} onPress={() => onAnalysisModeChange('META')} commonStyles={commonStyles} />
                                </View>

                                {analysisMode === 'DIFF' && (
                                    <>
                                        <TooltipLabel
                                            label="Requested Analysis"
                                            tooltip={ANALYSIS_TYPES.map(t => (
                                                <Text key={t.value} style={{ fontSize: 11, color: '#777', fontStyle: 'italic', marginBottom: 2 }}>
                                                    <Text style={{ fontWeight: 'bold' }}>{t.label}:</Text>{' '}{t.description}
                                                </Text>
                                            ))}
                                            style={commonStyles.toggleLabel}
                                        />
                                        <View style={[commonStyles.optionGroup, { flexWrap: 'wrap', justifyContent: 'flex-start' }]}>
                                            {ANALYSIS_TYPES.map((type) => {
                                                const isSelected = requestedAnalyses.includes(type.value);
                                                return (
                                                    <TouchableOpacity
                                                        key={type.value}
                                                        style={[
                                                            commonStyles.optionButton,
                                                            isSelected && commonStyles.optionButtonActive,
                                                        ]}
                                                        onPress={() => onRequestedAnalysesChange([type.value])}
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
