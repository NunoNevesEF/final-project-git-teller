import { useRouter } from "expo-router";
import { useTheme } from "@/constants/themeProvider";
import { useEffect, useState } from "react";
import { ScrollView } from "react-native";
import ScheduledReportsList from "@/components/reports/scheduledReports/ScheduledReportList";
import {getUserScheduledReports, pauseScheduledReport, resumeScheduledReport, deleteScheduledReport} from "@/services/ScheduledReportService";
import { GetScheduledReportDTO } from "@/models/scheduledReport/GetScheduledReportDTO";

interface Props {refreshKey: number;}

export default function ScheduledReportsTab({ refreshKey }: Props) {
    const router = useRouter();
    const { colors } = useTheme();

    const [scheduledReports, setScheduledReports] = useState<GetScheduledReportDTO[]>([]);

    const load = async () => {
        const data = await getUserScheduledReports();
        setScheduledReports(data);
    };

    useEffect(() => {
        void load();
    }, [refreshKey]);

    const handlePauseResume = async (report: GetScheduledReportDTO) => {
        if (report.type !== "PERIODIC") return;

        if (report.active) {
            await pauseScheduledReport(report.id);
        } else {
            await resumeScheduledReport(report.id);
        }

        await load();
    };

    const handleDelete = async (id: number) => {
        await deleteScheduledReport(id)
        await load();
    };

    return (
        <ScrollView
            style={{ flex: 1, backgroundColor: colors.background }}
            contentContainerStyle={{ padding: 16 }}
        >
            <ScheduledReportsList
                reports={scheduledReports}
                onPause={(id) => {
                    const report = scheduledReports.find(r => r.id === id);
                    if (report) {handlePauseResume(report);}
                }}
                onDelete={handleDelete}
            />
        </ScrollView>
    );
}