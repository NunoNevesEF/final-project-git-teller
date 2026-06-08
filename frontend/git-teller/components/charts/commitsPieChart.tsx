import React from "react";
import { Platform, View } from "react-native";
import { PieChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"
import ChartLegend from "./ChartLegend";

export default function CommitsPieChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
  const isMobile = Platform.OS !== "web" 
  const { A4_WIDTH } = REPORT_LAYOUT;
  const totalCommits = Object.values(data)
    .flat()
    .length;

  const sortedEntries = Object.entries(data)
    .map(([user, commits]) => ({
      user,
      commits,
      count: commits.length,
    }))
    .sort((a, b) => b.count - a.count);

  const pieData = sortedEntries.map(({ user, count }) => {
    const percent = ((count / totalCommits) * 100).toFixed(0);
    return {
      name: `\u00A0\u00A0${user}\u00A0\u00A0(${percent}%)`,
      population: count,
      color: generateColor(user),
      legendFontColor: colors.text,
      legendFontSize: 14
    };
  });

  return (
    <View style={isMobile ? {} : { alignItems: "center" }}>
      {(isMobile && <ChartLegend users={pieData.map(item => item.name)} standardUsers={sortedEntries.map(item => item.user)} />)}
      <PieChart
        data={pieData}
        width={A4_WIDTH - 50}
        height={220}
        chartConfig={{
          color: () => colors.icon,
          labelColor: () => colors.text,
        }}
        hasLegend={!isMobile}
        accessor="population"
        backgroundColor="transparent"
        paddingLeft="0"
        absolute
        style={{
          marginVertical: 20
        }}
      />
    </View>
  );
}