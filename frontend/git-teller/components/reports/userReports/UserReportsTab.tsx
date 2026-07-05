import { useEffect, useState } from "react";
import {View, Text, FlatList, Pressable, ScrollView} from "react-native";
import { useTheme } from "@/constants/themeProvider";
import {UserReportDTO} from "@/models/UserReportDTO";
import {downloadPdf, getUserReports} from "@/services/UserReportService";
import {useAnalysisInfoStore} from "@/store/useAnalysisInfoStore";
import {getReportAnalysis} from "@/services/UserReportService";
import {useRouter} from "expo-router";
import UserReportsList from "@/components/reports/userReports/GeneratedReports/UserReportsList";
import {ScheduledReportJobListItemDTO} from "@/models/scheduledReport/ScheduledReportJobListItemDTO";
import {getUserQueuedJobs} from "@/services/ScheduledReportService";
import QueuedScheduledReportList from "@/components/reports/userReports/QueuedReports/QueuedScheduledReportList";

export default function UserReportsTab(){
    const router = useRouter();

    const { colors } = useTheme()
    const [queued, setQueued] = useState<ScheduledReportJobListItemDTO[]>([])
    const [reports, setReports] = useState<UserReportDTO[]>([]);

    const setResult = useAnalysisInfoStore((s) => s.setResult);
    const setReportId = useAnalysisInfoStore((s) => s.setReportId);
    const setProjectName = useAnalysisInfoStore((s) => s.setProjectName);

    useEffect(() => {
        void handleLoadQueuedJobs()
        void handleLoadUserReports()
    }, []);

    const handleLoadQueuedJobs = async() => {
        try {
            const queuedReportData = await getUserQueuedJobs();
            setQueued(queuedReportData);
        } catch (err) {
            console.error("Failed to load queued reports", err);
        }
    }

    const handleLoadUserReports = async() => {
        try {
            const generatedReportData = await getUserReports();
            setReports(generatedReportData);
        } catch (err) {
            console.error("Failed to load reports", err);
        }
    }

    const handleLoadAnalysis = async (report: UserReportDTO) => {
        try {
            const analysis = await getReportAnalysis(report.id);

            setResult(analysis);
            setReportId(report.id);

            const projectName = getRepoName(report.repoUri)
            if(projectName != undefined) setProjectName(projectName);

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
        <ScrollView
            style={{ flex: 1, backgroundColor: colors.background }}
            contentContainerStyle={{ padding: 16 }}
        >
            {queued.length > 0 && (
                <QueuedScheduledReportList jobs={queued} />
            )}

            <UserReportsList
                reports={reports}
                onOpen={handleLoadAnalysis}
                onDownload={handleDownload}
            />
        </ScrollView>
    );
}
const getRepoName = (uri: string) => uri.split("/").filter(Boolean).pop();