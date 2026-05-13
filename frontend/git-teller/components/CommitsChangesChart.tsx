import React from "react";
import { Dimensions, View } from "react-native";
import { StackedBarChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { useTheme } from "@/constants/themeProvider";

const screenWidth = Dimensions.get("window").width;

export default function CommitsChangesChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
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
    <View>
      <StackedBarChart
        data={chartData}
        width={screenWidth * 0.7}
        height={400}
        hideLegend = {false}
        formatYLabel={() => ""}
        chartConfig={{
          propsForLabels: {
            dx: 5
          },
          backgroundColor: colors.background,
          backgroundGradientFrom: colors.background,
          backgroundGradientTo: colors.background,
          color: () => colors.icon,
          labelColor: () => colors.text,
        }}
        style={{
          alignItems : "center",
          marginVertical: 8,
          borderRadius: 16,
        }}
      />
    </View>
  );
}