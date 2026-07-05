import { Pressable, Text } from "react-native";
import BaseScheduledReportItem from "./BaseScheduledReportItem";
import { OneTimeScheduledReportDTO } from "@/models/scheduledReport/GetScheduledReportDTO";
import { useTheme } from "@/constants/themeProvider";

type Props = {
    report: OneTimeScheduledReportDTO;
    onDelete: () => void;
};

export default function OneTimeScheduledReportItem({report, onDelete}: Props) {
    const {colors} = useTheme()

    return (
        <BaseScheduledReportItem
            report={report}
            actions={
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
            }
        />
    );
}