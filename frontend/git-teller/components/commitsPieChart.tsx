import React from "react";
import { Dimensions, View } from "react-native";
import { PieChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";

const screenWidth = Dimensions.get("window").width;

export default function CommitsPieChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
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
          marginLeft : screenWidth/3,
          alignItems : "center",
          marginVertical: 20
        }}
      />
    </View>
  );
}