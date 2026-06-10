import React from "react";
import { Platform, View } from "react-native";
import { PieChart } from "react-native-chart-kit";
import { CommitAnalysis } from "@/models/CommitAnalysis";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"
import ChartLegend from "./ChartLegend";

export default function AdditionsChangesChart({ data }: { data: Record<string, CommitAnalysis[]> }) {
  const { colors } = useTheme()
  const { A4_WIDTH } = REPORT_LAYOUT;
  const isMobile = Platform.OS !== "web"   
  const totalAdditions = Object.values(data).flat().reduce((sum, commit) => sum + commit.additions, 0);

  const shortUser = (user: string) => user.length > 18 ? user.slice(0, 18) + "…" : user;

  const sortedEntries = Object.entries(data)
    .map(([user, commits]) => {
        const additions = commits.reduce((sum, commit) => sum + commit.additions,0);
        return {user,additions,};
    }).sort((a, b) => b.additions - a.additions);

    const pieData = sortedEntries.map(({ user, additions }) => {
    const percent = ((additions / totalAdditions) * 100).toFixed(0);

    return {
      name: `\u00A0\u00A0${shortUser(user)}\u00A0\u00A0(${percent}%)`,
      population: additions,
      color: generateColor(user),
      legendFontColor: colors.text,
      legendFontSize: 14,
      };
  });

  return (
    <View>
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
          alignItems : "center",
          marginVertical: 20
        }}
      />
    </View>
  );
}