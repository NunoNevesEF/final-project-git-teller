import React from "react";
import { Dimensions, View } from "react-native";
import { PieChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";

const screenWidth = Dimensions.get("window").width;

export default function DeletionsChangesChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
  const totaldeletions = Object.values(data)
  .flat()
  .reduce((sum, commit) => sum + commit.deletions, 0);

  const sortedEntries = Object.entries(data)
    .map(([user, commits]) => {
        const deletions = commits.reduce((sum, commit) => sum + commit.deletions,0);
        return {user,deletions,};
    }).sort((a, b) => b.deletions - a.deletions);

  const pieData = sortedEntries.map(({ user, deletions }) => {
  const percent = ((deletions / totaldeletions) * 100).toFixed(0);

  return {
      name: `\u00A0\u00A0${user}\u00A0\u00A0(${percent}%)`,
      population: deletions,
      color: generateColor(user),
      legendFontColor: colors.text,
      legendFontSize: 14,
    };
  });

  return (
    <View>
      <PieChart
        data={pieData}
        width={screenWidth * 0.4}
        height={220}
        chartConfig={{
          color: () => colors.icon,
          labelColor: () => colors.text,
        }}
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