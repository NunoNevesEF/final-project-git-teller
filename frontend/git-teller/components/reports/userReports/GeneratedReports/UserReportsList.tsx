import {FlatList, Text, View} from "react-native";
import {UserReportDTO} from "@/models/UserReportDTO";
import UserReportItem from "./UserReportItem";
import {useTheme} from "@/constants/themeProvider";

type Props = {
    reports: UserReportDTO[];
    onOpen: (report: UserReportDTO) => void;
    onDownload: (id: number) => void;
};

export default function UserReportsList({reports, onOpen, onDownload}: Props) {
    const {colors} = useTheme();

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
                My Reports
            </Text>

            {reports.map((report, index) => (
                <View
                    style={{marginBottom: index === reports.length - 1 ? 2: 4}}
                >
                    <UserReportItem
                        key={report.id}
                        report={report}
                        onOpen={() => onOpen(report)}
                        onDownload={() => onDownload(report.id)}
                    />
                </View>
            ))}
        </>
    );
}