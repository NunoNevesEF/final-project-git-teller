import React from "react";
import { View } from "react-native";
import { StackedBarChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { useTheme } from "@/constants/themeProvider";

export default function CommitsChangesChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
  const [width, setWidth] = React.useState(0);
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
    <View
        style={{
          width: "100%",
          alignItems: "center"
        }}
        onLayout={(e) => setWidth(e.nativeEvent.layout.width)}
      >
        {width > 0 && (
      <StackedBarChart
        data={chartData}
        width={width * 0.7}
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
      />
      )}
    </View>
  );
}