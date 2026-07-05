import { Pressable, Text } from "react-native";
import BaseScheduledReportItem from "./BaseScheduledReportItem";
import { PeriodicScheduledReportDTO } from "@/models/scheduledReport/GetScheduledReportDTO";
import { useTheme } from "@/constants/themeProvider";

type Props = {
    report: PeriodicScheduledReportDTO;
    onPause: () => void;
    onDelete: () => void;
};

export default function PeriodicScheduledReportItem({report, onPause, onDelete}: Props) {
    const { colors } = useTheme();

    return (
        <BaseScheduledReportItem
            report={report}
            actions={
                <>
                    {!report.isCancelled && (
                        <Pressable
                            onPress={onPause}
                            style={{
                                backgroundColor: colors.text,
                                paddingHorizontal: 16,
                                paddingVertical: 8,
                                borderRadius: 6,
                            }}
                        >
                            <Text
                                style={{
                                    color: colors.background,
                                    fontWeight: "600",
                                }}
                            >
                                {report.active ? "⏸" : "▶"}
                            </Text>
                        </Pressable>
                    )}

                    <Pressable
                        onPress={onDelete}
                        style={{
                            backgroundColor: colors.text,
                            paddingHorizontal: 16,
                            paddingVertical: 8,
                            borderRadius: 6,
                        }}
                    >
                        <Text
                            style={{
                                color: colors.background,
                                fontWeight: "600",
                            }}
                        >
                            Delete
                        </Text>
                    </Pressable>
                </>
            }
        >

        </BaseScheduledReportItem>
    );
}