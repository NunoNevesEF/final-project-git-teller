import {useRouter} from "expo-router";
import {useTheme} from "@/constants/themeProvider";
import {useEffect, useState} from "react";
import {FlatList, Pressable, Text, View} from "react-native";
import {getUserScheduledReports} from "@/services/ScheduledReportService";
import {GetScheduledReportDTO} from "@/models/scheduledReport/GetScheduledReportDTO";

interface Props{
    refreshKey: number
}

export default function ScheduledReportsTab({refreshKey}: Props){
    const router = useRouter();

    const { colors } = useTheme()
    const [scheduledReports, setScheduledReports] = useState<GetScheduledReportDTO[]>([]);

    useEffect(() => {
        const load = async () => {
            const data = await getUserScheduledReports();
            setScheduledReports(data);
        };
        load();
    }, [refreshKey]);

    return (
        <View style={{ flex: 1, padding: 16 }}>
            <Text style={{ fontSize: 20, fontWeight: "bold", marginBottom: 12, color : colors.text }}>
                Scheduled Reports
            </Text>

            <FlatList
                data={scheduledReports}
                keyExtractor={(item) => item.id.toString()}
                renderItem={({ item }) => (
                    <View
                        style={{
                            paddingVertical: 12,
                            borderBottomWidth: 1,
                            borderBottomColor: colors.text,
                        }}
                    >
                        <Text
                            style={{
                                color: '#2563eb',
                                fontWeight: 'bold',
                            }}
                        >
                            {getRepoName(item.repoUri)}
                        </Text>

                        {item.type === 'ONE_TIME' ? (
                            <>
                                <Text style={{ color: colors.text }}>
                                    One-time report
                                </Text>

                                <Text style={{ color: colors.text }}>
                                    Run at: {formatDate(item.nextRunAt!)}
                                </Text>
                            </>
                        ) : (
                            <>
                                <Text style={{ color: colors.text }}>
                                    Periodic report
                                </Text>

                                <Text style={{ color: colors.text }}>
                                    Next run: {formatDate(item.nextRunAt!)}
                                </Text>
                            </>
                        )}

                        {item.isCancelled && (
                            <Text style={{ color: 'red' }}>
                                Cancelled
                            </Text>
                        )}
                    </View>
                )}
            />
        </View>
    );
}

const formatDate = (date: string) =>
    new Date(date).toLocaleString("en-US", {
        year: "numeric",
        month: "long",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });

const getRepoName = (uri: string) => uri.split("/").filter(Boolean).pop();