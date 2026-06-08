import { useEffect, useState } from "react";
import { View, Text, FlatList, Pressable } from "react-native";
import { getUserReports, downloadReport } from "@/services/ReportGenerationService";
import { useTheme } from "@/constants/themeProvider";

type UserReport = {
  id: number;
  createdAt: string;
};

const formatDate = (date: string) =>
    new Date(date).toLocaleDateString("pt-PT", {
    year: "numeric",
    month: "long",
    day: "2-digit",
});

export default function UserReports() {
    const { colors } = useTheme()
    const [reports, setReports] = useState<UserReport[]>([]);

    useEffect(() => {
        const load = async () => {
        const data = await getUserReports();
        setReports(data);
        };
        load();
    }, []);

    const handleDownload = async (id: number) => {
        try {
            const blob = await downloadReport(id);

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