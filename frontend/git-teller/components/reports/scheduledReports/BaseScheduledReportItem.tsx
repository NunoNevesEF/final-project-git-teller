import { Pressable, Text, View } from "react-native";
import { ReactNode } from "react";
import { useTheme } from "@/constants/themeProvider";
import { getRepositoryParts } from "@/constants/RepositoryParts";
import { GetScheduledReportDTO } from "@/models/scheduledReport/GetScheduledReportDTO";

type Props = {
    report: GetScheduledReportDTO;
    onOpen: () => void;
    children?: ReactNode;
    actions: ReactNode;
};

export default function BaseScheduledReportItem({report, onOpen, children, actions}: Props) {
    const { colors } = useTheme();

    const repository = getRepositoryParts(report.repoUri);

    return (
        <View
            style={{
                flexDirection: "row",
                justifyContent: "space-between",
                borderWidth: 0.5,
                borderColor: colors.text,
                backgroundColor: colors.background,
                paddingHorizontal: 16,
                paddingVertical: 10,
            }}
        >
            <Pressable
                onPress={onOpen}
                style={{
                    flex: 1,
                    marginRight: 16,
                }}
            >
                <Text
                    numberOfLines={1}
                    style={{
                        color: colors.text,
                        fontSize: 16,
                        fontWeight: "700",
                        textDecorationLine: "underline",
                    }}
                >
                    {repository.name}
                </Text>

                <Text
                    style={{
                        color: colors.text,
                        opacity: 0.7,
                        marginTop: 4,
                    }}
                >
                    {repository.provider} - {repository.author}
                </Text>

                <Text
                    style={{
                        color: colors.text,
                        opacity: 0.7,
                    }}
                >
                    Type: {report.type === "PERIODIC" ? "Periodic" : "One-time"}
                </Text>

                {children}

                {report.nextRunAt && (
                    <Text
                        style={{
                            color: colors.text,
                            opacity: 0.7,
                        }}
                    >
                        Next Run: {formatDateTime(report.nextRunAt)}
                    </Text>
                )}

                {report.lastRunAt && (
                    <Text
                        style={{
                            color: colors.text,
                            opacity: 0.7,
                        }}
                    >
                        Last Run: {formatDateTime(report.lastRunAt)}
                    </Text>
                )}

                {report.isCancelled && report.cancellationReason && (
                    <Text
                        style={{
                            color: colors.text,
                            fontWeight: "600",
                            marginTop: 4,
                        }}
                    >
                        Cancelled: {report.cancellationReason}
                    </Text>
                )}
            </Pressable>

            <View
                style={{
                    justifyContent: "center",
                    gap: 8,
                }}
            >
                {actions}
            </View>
        </View>
    );
}

const formatDateTime = (date: string) =>
    new Date(date).toLocaleString("en-US", {
        month: "short",
        day: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });