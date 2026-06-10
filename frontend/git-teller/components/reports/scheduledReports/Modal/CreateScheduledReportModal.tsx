import {useState} from "react";
import {CreateScheduledReportDTO, dailyMode, FrequencyMode} from "@/models/scheduledReport/CreateScheduledReportDTO";

import {createScheduledReport} from "@/services/ScheduledReportService";
import {Modal, Pressable, ScrollView, TextInput, View, Text} from "react-native";
import { useTheme } from '@/constants/themeProvider';
import { useCommonStyles } from '@/constants/useCommonStyles';
import FrequencyModeInput from "@/components/reports/scheduledReports/Modal/FrequencyMode/FrequencyModeInput";

interface ScheduledReportModalProps {
    visible: boolean;
    onClose: () => void;
    onCreated: () => void;
}

export default function ScheduledReportModal({visible, onClose, onCreated,}: ScheduledReportModalProps) {
    const styles = useCommonStyles();
    const { colors } = useTheme();

    const [scheduleType, setScheduleType] =
        useState<'ONE_TIME' | 'PERIODIC'>('ONE_TIME');

    const [repoUri, setRepoUri] = useState('');

    const [dataStart, setDataStart] = useState('');
    const [runAt, setRunAt] = useState('');

    const [timeZone, setTimeZone] = useState(
        Intl.DateTimeFormat().resolvedOptions().timeZone
    );

    const [time, setTime] = useState('09:00');

    const [freqMode, setFreqMode] = useState<FrequencyMode>(dailyMode());

    const handleCreate = async () => {
        try {
            let dto: CreateScheduledReportDTO;

            if (scheduleType === 'ONE_TIME') {
                dto = {
                    type: 'ONE_TIME',
                    repoUri,
                    dataStart: toInstant(dataStart),
                    runAt: toInstant(runAt),
                };
            } else {
                dto = {
                    type: 'PERIODIC',
                    repoUri,
                    timeZone: timeZone,
                    time,
                    freqMode: freqMode,
                };
            }

            await createScheduledReport(dto);

            onCreated();
            onClose()
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

        // Basic validation
        if (
            !year || !month || !day ||
            hour === undefined || minute === undefined
        ) {
            return null;
        }

        const date = new Date(year, month - 1, day, hour, minute, 0);

        return date.toISOString();
    }

    return (
        <Modal
            visible={visible}
            transparent
            animationType="fade"
            onRequestClose={onClose}
        >
            <Pressable
                onPress={onClose}
                style={[
                    styles.root,
                    styles.centered,
                    { backgroundColor: 'rgba(0,0,0,0.4)' },
                ]}
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

                        <Text style={styles.formTitle}>
                            Create Scheduled Report
                        </Text>

                        <View style={styles.optionGroup}>
                            <Pressable
                                onPress={() => setScheduleType('ONE_TIME')}
                                style={[
                                    styles.optionButton,
                                    scheduleType === 'ONE_TIME' &&
                                    styles.optionButtonActive,
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.optionButtonText,
                                        scheduleType === 'ONE_TIME' &&
                                        styles.optionButtonTextActive,
                                    ]}
                                >
                                    One-time
                                </Text>
                            </Pressable>

                            <Pressable
                                onPress={() => setScheduleType('PERIODIC')}
                                style={[
                                    styles.optionButton,
                                    scheduleType === 'PERIODIC' &&
                                    styles.optionButtonActive,
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.optionButtonText,
                                        scheduleType === 'PERIODIC' &&
                                        styles.optionButtonTextActive,
                                    ]}
                                >
                                    Periodic
                                </Text>
                            </Pressable>
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
                                    <TextInput
                                        placeholder="Data start"
                                        placeholderTextColor={colors.icon}
                                        value={dataStart}
                                        onChangeText={setDataStart}
                                        style={styles.searchInput}
                                    />

                                    <TextInput
                                        placeholder="Run at"
                                        placeholderTextColor={colors.icon}
                                        value={runAt}
                                        onChangeText={setRunAt}
                                        style={styles.searchInput}
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

                                    <FrequencyModeInput
                                        value={freqMode}
                                        onChange={setFreqMode}
                                    />
                                </>
                            )}
                        </View>

                        <Pressable
                            onPress={handleCreate}
                            style={styles.primaryButton}
                        >
                            <Text style={styles.primaryButtonText}>
                                Create
                            </Text>
                        </Pressable>

                        <Pressable
                            onPress={onClose}
                            style={[
                                styles.topLeftActionButton,
                                { marginTop: 8 },
                            ]}
                        >
                            <Text style={styles.topLeftActionButtonText}>
                                Cancel
                            </Text>
                        </Pressable>

                    </ScrollView>
                </Pressable>
            </Pressable>
        </Modal>
    );
}