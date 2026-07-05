import {UserReportDTO} from "@/models/UserReportDTO";
import { useTheme } from "@/constants/themeProvider";
import {Pressable, View, Text} from "react-native";
import {getRepositoryParts} from "@/constants/RepositoryParts";

type Props = {
    report: UserReportDTO,
    onOpen: () => void;
    onDownload: () => void;
};

export default function UserReportItem({report, onOpen, onDownload}: Props){
    const {colors} = useTheme();

    const repositoryParts = getRepositoryParts(report.repoUri)

    return (
        <View
            style={{
                flexDirection: "row",
                alignItems: "center",
                justifyContent: "space-between",
                borderWidth: 0.5,
                borderColor: colors.text,
                backgroundColor: colors.background,
                minHeight: 90,
                paddingHorizontal: 16,
                paddingVertical: 2,
            }}
        >
            <Pressable
                onPress={onOpen}
                style={{ flex: 1, marginRight: 16 }}
            >
                <Text
                    style={{
                        color: colors.text,
                        fontSize: 16,
                        fontWeight: "700",
                        marginTop: 2,
                        textDecorationLine: "underline",
                    }}
                    numberOfLines={1}
                >
                    {repositoryParts.name}
                </Text>

                <Text
                    style={{
                        color: colors.text,
                        opacity: 0.7,
                        marginTop: 4,
                    }}
                >
                    {formatDate(report.createdAt)}
                </Text>
            </Pressable>

            <Pressable
                onPress={onDownload}
                style={{
                    paddingVertical: 8,
                    paddingHorizontal: 14,
                    backgroundColor: "#2563eb",
                    borderRadius: 6,
                }}
            >
                <Text
                    style={{
                        color: "white",
                        fontWeight: "600",
                    }}
                >
                    Download
                </Text>
            </Pressable>
        </View>
    );
}

const formatDate = (date: string) =>
    new Date(date).toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "2-digit",
    });