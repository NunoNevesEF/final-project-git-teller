import React from "react";
import { ScrollView, View } from "react-native";
import { LineChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"
import ChartLegend from "./ChartLegend";

export default function CommitsChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
  const { A4_WIDTH } = REPORT_LAYOUT;
  const groupByDayCumulative = (commits: CommitDTO[], labels: string[]) => {
    const map: Record<string, number> = {};
    let cumulative = 0;

    labels.forEach(day => {
      const dayCount = commits.filter(c => 
        new Date(c.timestamp).toISOString().split("T")[0] === day
      ).length;

      cumulative += dayCount;
      map[day] = cumulative;
    });

    return map;
  };

  const allDaysSet = new Set<string>();

  Object.values(data).forEach(commits => {
    commits.forEach(c => {
      const day = new Date(c.timestamp).toISOString().split("T")[0];
      allDaysSet.add(day);
    });
  });

  const labels = Array.from(allDaysSet).sort();

  const step = Math.ceil(labels.length / 6);
  const visibleLabels = labels.map((label, index) =>
    index % step === 0 ? label : ""
  );

  const datasets = Object.entries(data).map(
    ([user, commits]) => {
      const grouped = groupByDayCumulative(commits, labels);
      

      return {
        data: labels.map(day => grouped[day]),
        color: () => generateColor(user),
        strokeWidth: 2,
      };
    }
  );

  const users = Object.keys(data);

  const maxCommits = Math.max(
    ...datasets.flatMap(d => d.data)
  );

  const segments = maxCommits <= 10 ? maxCommits : 6;

  return (
    <View>
      <ChartLegend users={users}></ChartLegend>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator
        contentContainerStyle={{
          flexGrow: 1,
          justifyContent: "center",
        }}
      >
    <View
      style={{
        flex: 1,
        alignItems: "center",
      }}
    >
      <LineChart
        data={{
          labels: visibleLabels,
          datasets,
        }}
        width={A4_WIDTH}
        height={220}
        withShadow={false}
        formatYLabel={(yValue) => Math.round(Number(yValue)).toString()}
        segments={segments}
        chartConfig={{
          backgroundColor: colors.backgroundCard,
          backgroundGradientFrom: colors.backgroundCard,
          backgroundGradientTo: colors.backgroundCard,
          color: () => colors.icon,
          labelColor: () => colors.icon,
        }}
        style={{
          marginVertical: 8,
          borderRadius: 16,
        }}
      />
    </View>
  </ScrollView>
    </View>
  );
}