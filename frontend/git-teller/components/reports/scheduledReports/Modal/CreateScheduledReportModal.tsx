import { useEffect, useState } from "react";
import { listGitAccounts } from "@/services/AccountService";
import { OAuthLinkedAccountListItemDTO } from "@/models/account/OAuthLinkedAccountListItemDTO";
import { AccountDropdownMenu } from "@/components/gitRepos/AccountsDropdownMenu";
import {CreateScheduledReportDTO, dailyMode, FrequencyMode} from "@/models/scheduledReport/CreateScheduledReportDTO";
import {createScheduledReport} from "@/services/ScheduledReportService";
import {Modal, Pressable, ScrollView, TextInput, TouchableOpacity, View, Text} from "react-native";
import { useTheme } from '@/constants/themeProvider';
import { useCommonStyles } from '@/constants/useCommonStyles';
import FrequencyModeInput from "@/components/reports/scheduledReports/Modal/FrequencyMode/FrequencyModeInput";
import TooltipLabel from '@/components/utils/TooltipLabel';
import { DateTimeDayPicker } from '@/components/utils/DatePicker';
import { ANALYSIS_TYPES } from '@/constants/analysisTypes';

type LlmComplexity = 'SIMPLE' | 'MEDIUM' | 'COMPLEX';
type LlmMode = 'DIFF' | 'META';

interface ScheduledReportModalProps {
    visible: boolean;
    onClose: () => void;
    onCreated: () => void;
}

export default function ScheduledReportModal({visible, onClose, onCreated}: ScheduledReportModalProps) {
    const styles = useCommonStyles();
    const { colors } = useTheme();

    const [scheduleType, setScheduleType] = useState<'ONE_TIME' | 'PERIODIC'>('ONE_TIME');
    const [repoUri, setRepoUri] = useState('');
    const [dataStart, setDataStart] = useState('');
    const [runAt, setRunAt] = useState('');
    const [timeZone, setTimeZone] = useState(Intl.DateTimeFormat().resolvedOptions().timeZone);
    const [time, setTime] = useState('09:00');
    const [freqMode, setFreqMode] = useState<FrequencyMode>(dailyMode());

    const [accountList, setAccountList] = useState<OAuthLinkedAccountListItemDTO[]>([]);
    const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);

    const [enableLlm, setEnableLlm] = useState(false);
    const [llmStrategy, setLlmStrategy] = useState<'overview' | 'commits'>('commits');
    const [llmComplexity, setLlmComplexity] = useState<LlmComplexity>('MEDIUM');
    const [llmMode, setLlmMode] = useState<LlmMode>('DIFF');
    const [llmAnalyses, setLlmAnalyses] = useState<string[]>(['DEFAULT']);

    useEffect(() => {
        if (!visible) return;

        const loadAccounts = async () => {
            try {
                const accounts = await listGitAccounts();
                setAccountList(accounts);

                if (accounts.length > 0) {
                    setSelectedAccountId(accounts[0].id);
                }
            } catch (err) {
                console.error("Failed to load git accounts", err);
            }
        };

        loadAccounts();
    }, [visible]);

    const handleCreate = async () => {
        try {
            if (!isValidRepositoryUrl(repoUri)) {
                console.log("Invalid repository URL");
                return;
            }

            const byDetailedSettings = {
                promptComplexity: llmComplexity,
                analysisMode: llmMode,
                requestedAnalyses: llmAnalyses,
            };
            
            const llmConfig = enableLlm ? {
                flag: llmStrategy === 'overview',
                byShas: null,
                byDetailedSettings: byDetailedSettings
            } : null;

            let dto: CreateScheduledReportDTO;

            if (scheduleType === 'ONE_TIME') {
                dto = {
                    type: 'ONE_TIME',
                    repoUri,
                    dataStart: toInstant(dataStart),
                    runAt: toInstant(runAt),
                    llmConfig,
                    gitAccountId: selectedAccountId
                };
            } else {
                dto = {
                    type: 'PERIODIC',
                    repoUri,
                    timeZone,
                    time,
                    freqMode,
                    llmConfig,
                    gitAccountId: selectedAccountId
                };
            }

            await createScheduledReport(dto);
            onCreated();
            onClose();
        } catch (err) {
            console.error(err);
        }
    };

    const toInstant = (value: string) => {
        if (!value) return null;
        const [datePart, timePart] = value.split("/");
        if (!datePart || !timePart) return null;
        const [year, month, day] = datePart.split("-").map(Number);
        const [hour, minute] = timePart.split(":").map(Number);
        if (!year || !month || !day || hour === undefined || minute === undefined) return null;
        return new Date(year, month - 1, day, hour, minute, 0).toISOString();
    };

    const isValidRepositoryUrl = (repoUrl: string) => {
        try {
            const url = new URL(repoUrl);

            const parts = url.pathname
                .split("/")
                .filter(Boolean);

            return parts.length >= 2;
        } catch {
            return false;
        }
    };

    return (
        <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
            <Pressable
                onPress={onClose}
                style={[styles.root, styles.centered, { backgroundColor: 'rgba(0,0,0,0.4)' }]}
            >
                <Pressable
                    onPress={() => {}}
                    style={{
                        width: '90%',
                        maxWidth: 420,
                        backgroundColor: colors.background,
                        borderRadius: 12,
                        padding: 16,
                        maxHeight: '85%',
                    }}
                >
                    <ScrollView showsVerticalScrollIndicator={false}>

                        <Text style={styles.formTitle}>Create Scheduled Report</Text>

                        <View style={styles.optionGroup}>
                            <Pressable
                                onPress={() => setScheduleType('ONE_TIME')}
                                style={[styles.optionButton, scheduleType === 'ONE_TIME' && styles.optionButtonActive]}
                            >
                                <Text style={[styles.optionButtonText, scheduleType === 'ONE_TIME' && styles.optionButtonTextActive]}>
                                    One-time
                                </Text>
                            </Pressable>
                            <Pressable
                                onPress={() => setScheduleType('PERIODIC')}
                                style={[styles.optionButton, scheduleType === 'PERIODIC' && styles.optionButtonActive]}
                            >
                                <Text style={[styles.optionButtonText, scheduleType === 'PERIODIC' && styles.optionButtonTextActive]}>
                                    Periodic
                                </Text>
                            </Pressable>
                        </View>

                        <View style={{ flexDirection: "row", alignItems: "center", marginBottom: 12 }}>
                            <Text style={{ marginRight: 10 }}>ACCOUNT</Text>

                            <AccountDropdownMenu
                                accounts={accountList}
                                selectedAccountId={selectedAccountId}
                                onSelect={setSelectedAccountId}
                            />
                        </View>

                        <View style={styles.inputSection}>
                            <TextInput
                                placeholder="Repository URL"
                                placeholderTextColor={colors.icon}
                                value={repoUri}
                                onChangeText={setRepoUri}
                                style={styles.searchInput}
                            />

                            {scheduleType === 'ONE_TIME' && (
                                <>
                                    <DateTimeDayPicker
                                        placeholder="Data start"
                                        value={dataStart}
                                        onChange={setDataStart}
                                    />
                                    <DateTimeDayPicker
                                        placeholder="Run at"
                                        value={runAt}
                                        onChange={setRunAt}
                                    />
                                </>
                            )}

                            {scheduleType === 'PERIODIC' && (
                                <>
                                    <TextInput
                                        placeholder="Timezone"
                                        placeholderTextColor={colors.icon}
                                        value={timeZone}
                                        onChangeText={setTimeZone}
                                        style={styles.searchInput}
                                    />
                                    <TextInput
                                        placeholder="Time (HH:mm)"
                                        placeholderTextColor={colors.icon}
                                        value={time}
                                        onChangeText={setTime}
                                        style={styles.searchInput}
                                    />
                                    <FrequencyModeInput value={freqMode} onChange={setFreqMode} />
                                </>
                            )}
                        </View>


                        <Pressable
                            onPress={() => setEnableLlm(!enableLlm)}
                            style={[styles.optionButton, enableLlm && styles.optionButtonActive, { marginTop: 12, alignSelf: 'center' }]}
                        >
                            <Text style={[styles.optionButtonText, enableLlm && styles.optionButtonTextActive]}>
                                {enableLlm ? 'LLM Analysis: ON' : 'LLM Analysis: OFF'}
                            </Text>
                        </Pressable>

                        {enableLlm && (
                            <>
                                <TooltipLabel
                                    label="Analysis Strategy"
                                    tooltip="By Commits: analyses only the commits from this report's period. Overview: analyses the full repository summary contributors, activity, and most changed files."
                                    style={[styles.toggleLabel, { marginTop: 12 }]}
                                />
                                <View style={styles.optionGroup}>
                                    <Pressable
                                        onPress={() => setLlmStrategy('commits')}
                                        style={[styles.optionButton, llmStrategy === 'commits' && styles.optionButtonActive]}
                                    >
                                        <Text style={[styles.optionButtonText, llmStrategy === 'commits' && styles.optionButtonTextActive]}>By Commits</Text>
                                    </Pressable>
                                    <Pressable
                                        onPress={() => setLlmStrategy('overview')}
                                        style={[styles.optionButton, llmStrategy === 'overview' && styles.optionButtonActive]}
                                    >
                                        <Text style={[styles.optionButtonText, llmStrategy === 'overview' && styles.optionButtonTextActive]}>Overview</Text>
                                    </Pressable>
                                </View>

                                {llmStrategy === 'commits' && (
                                <>
                                <TooltipLabel
                                    label="Complexity"
                                    tooltip="Simple: short summary (8 lines). Medium: structured with impact and risks. Complex: deep evidence-based review."
                                    style={[styles.toggleLabel, { marginTop: 12 }]}
                                />
                                <View style={styles.optionGroup}>
                                    {(['SIMPLE', 'MEDIUM', 'COMPLEX'] as LlmComplexity[]).map(c => (
                                        <Pressable
                                            key={c}
                                            onPress={() => {
                                                setLlmComplexity(c);
                                                const max = c === 'SIMPLE' ? 2 : c === 'MEDIUM' ? 3 : 4;
                                                setLlmAnalyses(prev => prev.slice(0, max));
                                            }}
                                            style={[styles.optionButton, llmComplexity === c && styles.optionButtonActive]}
                                        >
                                            <Text style={[styles.optionButtonText, llmComplexity === c && styles.optionButtonTextActive]}>
                                                {c.charAt(0) + c.slice(1).toLowerCase()}
                                            </Text>
                                        </Pressable>
                                    ))}
                                </View>

                                <TooltipLabel
                                    label="Mode"
                                    tooltip="Diff: sends actual code changes, more accurate but slower. Meta: sends only file names and stats, faster but less detailed."
                                    style={styles.toggleLabel}
                                />
                                <View style={styles.optionGroup}>
                                    {(['DIFF', 'META'] as LlmMode[]).map(m => (
                                        <Pressable
                                            key={m}
                                            onPress={() => setLlmMode(m)}
                                            style={[styles.optionButton, llmMode === m && styles.optionButtonActive]}
                                        >
                                            <Text style={[styles.optionButtonText, llmMode === m && styles.optionButtonTextActive]}>
                                                {m.charAt(0) + m.slice(1).toLowerCase()}
                                            </Text>
                                        </Pressable>
                                    ))}
                                </View>

                                {llmMode === 'DIFF' && (
                                    <>
                                        <TooltipLabel
                                            label="Requested Analysis"
                                            tooltip={ANALYSIS_TYPES.map(t => (
                                                <Text key={t.value} style={{ fontSize: 11, color: '#777', fontStyle: 'italic', marginBottom: 2 }}>
                                                    <Text style={{ fontWeight: 'bold' }}>{t.label}:</Text>{' '}{t.description}
                                                </Text>
                                            ))}
                                            style={styles.toggleLabel}
                                        />
                                        <View style={[styles.optionGroup, { flexWrap: 'wrap', justifyContent: 'flex-start' }]}>
                                            {ANALYSIS_TYPES.map(type => {
                                                const isSelected = llmAnalyses.includes(type.value);
                                                return (
                                                    <TouchableOpacity
                                                        key={type.value}
                                                        style={[
                                                            styles.optionButton,
                                                            isSelected && styles.optionButtonActive,
                                                        ]}
                                                        onPress={() => setLlmAnalyses([type.value])}
                                                    >
                                                        <Text style={[styles.optionButtonText, isSelected && styles.optionButtonTextActive]}>
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
                            </>
                        )}

                        <Pressable onPress={handleCreate} style={[styles.primaryButton, { marginTop: 16, alignSelf: 'center' }]}>
                            <Text style={styles.primaryButtonText}>Create</Text>
                        </Pressable>

                        <Pressable onPress={onClose} style={[styles.topLeftActionButton, { marginTop: 8, alignSelf: 'center' }]}>
                            <Text style={styles.topLeftActionButtonText}>Cancel</Text>
                        </Pressable>

                    </ScrollView>
                </Pressable>
            </Pressable>
        </Modal>
    );
}
