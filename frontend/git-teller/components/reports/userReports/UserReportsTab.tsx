import { useEffect, useState } from "react";
import { View, Text, FlatList, Pressable } from "react-native";
import { useTheme } from "@/constants/themeProvider";
import {UserReportDTO} from "@/models/UserReportDTO";
import {downloadPdf, getUserReports} from "@/services/UserReportService";
import {useAnalysisInfoStore} from "@/store/useAnalysisInfoStore";
import {getReportAnalysis} from "@/services/UserReportService";
import {useRouter} from "expo-router";

export default function UserReportsTab(){
    const router = useRouter();

    const { colors } = useTheme()
    const [reports, setReports] = useState<UserReportDTO[]>([]);

    const setResult = useAnalysisInfoStore((s) => s.setResult);
    const setReportId = useAnalysisInfoStore((s) => s.setReportId);
    const setProjectName = useAnalysisInfoStore((s) => s.setProjectName);

    useEffect(() => {
        const load = async () => {
            const data = await getUserReports();
            setReports(data);
        };
        load();
    }, []);

    const handleLoadAnalysis = async (report: UserReportDTO) => {
        try {
            const analysis = await getReportAnalysis(report.id);

            setResult(analysis);
            setReportId(report.id);
            setProjectName(getRepoName(report.repoUri));

            router.push("/Info");
        } catch (err) {
            console.error("Failed to load analysis", err);
        }
    };

    const handleDownload = async (id: number) => {
        try {
            const blob = await downloadPdf(id);

            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `report-${id}.pdf`;
            a.click();
            URL.revokeObjectURL(url);
        } catch (e) {
            console.error("Download failed", e);
        }
    };

    return (
        <View style={{ flex: 1, padding: 16, backgroundColor: colors.background }}>
            <Text style={{ fontSize: 20, fontWeight: "bold", marginBottom: 12, color : colors.text }}>
                My Reports
            </Text>

            <FlatList
                data={reports}
                keyExtractor={(item) => item.id.toString()}
                renderItem={({ item }) => (
                    <View
                        style={{
                            flexDirection: "row",
                            justifyContent: "space-between",
                            alignItems: "center",
                            paddingVertical: 12,
                            borderBottomWidth: 1,
                            borderBottomColor: colors.text,
                        }}>
                        <Text style={{ color: colors.text }}>
                            {formatDate(item.createdAt)}
                        </Text>

                        <Pressable onPress={() => handleLoadAnalysis(item)}>
                            <Text
                                style={{
                                    color: "#2563eb",
                                    fontSize: 12,
                                    marginTop: 2,
                                    textDecorationLine: "underline",
                                }}
                                numberOfLines={1}
                            >
                                {getRepoName(item.repoUri)}
                            </Text>
                        </Pressable>

                        <Pressable
                            onPress={() => handleDownload(item.id)}
                            style={{
                                paddingVertical: 6,
                                paddingHorizontal: 12,
                                backgroundColor: "#2563eb",
                                borderRadius: 6,
                            }}
                        >
                            <Text style={{ color: "white" }}>Download</Text>
                        </Pressable>
                    </View>
                )}
            />
        </View>
    );
}

const formatDate = (date: string) =>
    new Date(date).toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "2-digit",
    }); //TODO: REPLACE en-US with timezone

const getRepoName = (uri: string) => uri.split("/").filter(Boolean).pop();