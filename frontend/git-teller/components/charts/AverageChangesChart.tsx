import React from "react";
import { ScrollView, View } from "react-native";
import { BarChart } from "react-native-chart-kit";
import { CommitAnalysis } from "@/models/CommitAnalysis";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"
import ChartLegend from "./ChartLegend";

export default function AverageChangesChart({ data }: { data: Record<string, CommitAnalysis[]> }) {
  const { colors } = useTheme()
  const { A4_WIDTH } = REPORT_LAYOUT;
  const users = Object.keys(data);

  const avgChangesByUser = Object.fromEntries(
    Object.entries(data).map(([user, commits]) => {

        const totalChanges = commits.reduce(
        (sum, commit) => sum + commit.additions + commit.deletions,0);

        const average = commits.length === 0 ? 0: totalChanges / commits.length;

        return [user, Number(average.toFixed(1))];
    })
  );

  const maxCommits = Math.max(
    ...Object.values(avgChangesByUser)
  );

  const segments = maxCommits <= 10 ? maxCommits : 6;

  return (
    <View>
      <ChartLegend users={users}></ChartLegend>
      <ScrollView horizontal showsHorizontalScrollIndicator contentContainerStyle={{
          flexGrow: 1,
          justifyContent: "center",
        }}
      >
        <BarChart
          data={{
              labels: [],
              datasets: [{
                  data: users.map(user => avgChangesByUser[user]),
                  colors: users.map(user => () => generateColor(user))
              }]
          }}
          withCustomBarColorFromData
          width={A4_WIDTH} 
          height={220}
          fromZero={true}
          yAxisLabel=""
          yAxisSuffix=""
          segments={segments}
          chartConfig={{
            propsForBackgroundLines: {
              translateX: "60"
            },
            backgroundColor: colors.backgroundCard,
            backgroundGradientFrom: colors.backgroundCard,
            backgroundGradientTo: colors.backgroundCard,
            color: () => colors.icon,
            labelColor: () => colors.icon,
            decimalPlaces: 0,
            style: {
              borderRadius: 16
            }
          }}
          style={{
            alignItems : "center",
            marginVertical: 20
          }}
        />
    </ScrollView>
  </View>
  );
}