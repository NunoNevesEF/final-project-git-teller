import React from "react";
import { ScrollView, View , Text} from "react-native";
import { StackedBarChart } from "react-native-chart-kit";
import { CommitAnalysis } from "@/models/CommitAnalysis";
import { useTheme } from "@/constants/themeProvider";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"

export default function CommitsChangesChart({ data }: { data: Record<string, CommitAnalysis[]> }) {
  const { colors } = useTheme()
  const { A4_WIDTH } = REPORT_LAYOUT;
  const users = Object.keys(data);

  const additions = users.map(user =>
    data[user].reduce((sum, c) => sum + c.additions, 0)
  );

  const deletions = users.map(user =>
    data[user].reduce((sum, c) => sum + c.deletions, 0)
  );

  const chartData = {
    labels: users,
    data: users.map((_, i) => [additions[i], deletions[i]]),
    legend: ["Lines added", "Lines Deleted"],
    barColors: ["#4caf50", "#f44336"]
  }

  return (
    <ScrollView horizontal showsHorizontalScrollIndicator contentContainerStyle={{
      flexGrow: 1,
      justifyContent: "center",
    }}>
      <View>
        <StackedBarChart
          data={chartData}
          width={A4_WIDTH}
          height={400}
          hideLegend={true}
          formatYLabel={() => ""}
          chartConfig={{
            backgroundColor: colors.backgroundCard,
            backgroundGradientFrom: colors.backgroundCard,
            backgroundGradientTo: colors.backgroundCard,
            color: () => colors.icon,
            labelColor: () => colors.text,
          }}
        />

        <View
          style={{
            position: "absolute",
          }}
        >
          {users.map((user, i) => (
            <View
              key={user}
              style={{
                position: "absolute",
                left: (A4_WIDTH / users.length) * i + 25
              }}
            >
              <View>
                <Text style={{ color: "#4caf50", fontSize: 10 }}>
                  +{additions[i]}
                </Text>
                <Text style={{ color: "#f44336", fontSize: 10 }}>
                  -{deletions[i]}
                </Text>
              </View>
            </View>
          ))}
        </View>
      </View>
    </ScrollView>
  );
}