import { Text, View } from "react-native";
import { useTheme } from "@/constants/themeProvider";
import {GetScheduledReportDTO} from "@/models/scheduledReport/GetScheduledReportDTO";
import OneTimeScheduledReportItem from "./OneTimeScheduledReportItem";
import PeriodicScheduledReportItem from "./PeriodicScheduledReportItem";

type Props = {
    reports: GetScheduledReportDTO[];
    onPause: (id: number) => void;
    onDelete: (id: number) => void;
};

export default function ScheduledReportsList({reports, onPause, onDelete}: Props) {
    const { colors } = useTheme();

    return (
        <>
            <Text
                style={{
                    fontSize: 20,
                    fontWeight: "bold",
                    marginBottom: 12,
                    color: colors.text,
                }}
            >
                Scheduled Reports
            </Text>

            {reports.map((report, index) => (
                <View
                    key={report.id}
                    style={{
                        marginBottom: index === reports.length - 1 ? 2 : 4,
                    }}
                >
                    {report.type === "PERIODIC" ? (
                        <PeriodicScheduledReportItem
                            report={report}
                            onPause={() => onPause(report.id)}
                            onDelete={() => onDelete(report.id)}
                        />
                    ) : (
                        <OneTimeScheduledReportItem
                            report={report}
                            onDelete={() => onDelete(report.id)}
                        />
                    )}
                </View>
            ))}
        </>
    );
}