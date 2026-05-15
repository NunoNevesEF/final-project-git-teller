import React from "react";
import { Dimensions, View } from "react-native";
import { PieChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";

const screenWidth = Dimensions.get("window").width;

export default function AdditionsChangesChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
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