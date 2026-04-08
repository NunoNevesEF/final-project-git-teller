import React from "react";
import { Dimensions, View, useColorScheme } from "react-native";
import { LineChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { Colors, generateColor } from "@/constants/theme";

const screenWidth = Dimensions.get("window").width;

export default function CommitsChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const theme = useColorScheme();
  const themeColors = Colors[theme ?? "light"];
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

  const maxCommits = Math.max(
    ...datasets.flatMap(d => d.data)
  );

  const segments = maxCommits <= 10 ? maxCommits : 6;

  return (
    <View>
      <LineChart
        data={{
          labels,
          datasets,
        }}
        width={screenWidth*0.7} 
        height={220}
        formatYLabel={(yValue) => Math.round(Number(yValue)).toString()}
        segments={segments}
        chartConfig={{
          backgroundColor: themeColors.background,
          backgroundGradientFrom: themeColors.background,
          backgroundGradientTo: themeColors.background,
          color: () => themeColors.icon,
          labelColor: () => themeColors.icon,
          style: {
            borderRadius: 16
          },
          propsForDots: {
            r: "6",
            strokeWidth: "2",
            stroke: themeColors.tint
          }
        }}
        style={{
          alignItems : "center",
          marginVertical: 8,
          borderRadius: 16
        }}
      />
    </View>
  );
}