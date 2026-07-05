import { FlatList, Text, View } from "react-native";
import { useTheme } from "@/constants/themeProvider";
import { ScheduledReportJobListItemDTO } from "@/models/scheduledReport/ScheduledReportJobListItemDTO";
import QueuedScheduledReportItem from "./QueuedScheduledReportItem";

type Props = {
    jobs: ScheduledReportJobListItemDTO[];
};

export default function QueuedScheduledReportList({ jobs }: Props) {
    const { colors } = useTheme();

    if (jobs.length === 0) {
        return null;
    }

    return (
        <>
            <Text
                style={{
                    fontSize: 20,
                    fontWeight: "bold",
                    marginBottom: 12,
                    color: colors.text,
                    paddingVertical: 2,
                }}
            >
                Queued Reports
            </Text>

            {jobs.map((job, index) => (
                <View
                    key={`${job.repoUri}-${job.scheduledFor}`}
                    style={{
                        marginBottom: index === jobs.length - 1 ? 2 : 4,
                    }}
                >
                    <QueuedScheduledReportItem job={job} />
                </View>
            ))}
        </>
    );
}