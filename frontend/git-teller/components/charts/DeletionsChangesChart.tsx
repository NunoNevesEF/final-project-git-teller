import React from "react";
import { Platform, View } from "react-native";
import { PieChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"
import ChartLegend from "./ChartLegend";

export default function DeletionsChangesChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
  const { A4_WIDTH } = REPORT_LAYOUT;
  const isMobile = Platform.OS !== "web"   
  const totaldeletions = Object.values(data).flat().reduce((sum, commit) => sum + commit.deletions, 0);
  const shortUser = (user: string) => user.length > 18 ? user.slice(0, 18) + "…" : user;


  const sortedEntries = Object.entries(data)
    .map(([user, commits]) => {
        const deletions = commits.reduce((sum, commit) => sum + commit.deletions,0);
        return {user,deletions,};
    }).sort((a, b) => b.deletions - a.deletions);

  const pieData = sortedEntries.map(({ user, deletions }) => {
  const percent = ((deletions / totaldeletions) * 100).toFixed(0);

  return {
      name: `\u00A0\u00A0${shortUser(user)}\u00A0\u00A0(${percent}%)`,
      population: deletions,
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
        accessor="population"
        backgroundColor="transparent"
        paddingLeft="0"
        absolute
        hasLegend={!isMobile}
        style={{
          alignItems : "center",
          marginVertical: 20
        }}
      />
    </View>
  );
}