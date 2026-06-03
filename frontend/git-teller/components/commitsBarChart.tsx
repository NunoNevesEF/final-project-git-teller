import React from "react";
import { View } from "react-native";
import { BarChart } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { generateColor } from "@/constants/theme";
import { useTheme } from "@/constants/themeProvider";


export default function CommitsBarChart({ data }: { data: Record<string, CommitDTO[]> }) {
  const { colors } = useTheme()
  const [width, setWidth] = React.useState(0);
  const users = Object.keys(data);

  const commitsCountByUser = Object.fromEntries(
    Object.entries(data).map(([user, commits]) => [user, commits.length])
  );

  const maxCommits = Math.max(
    ...Object.values(commitsCountByUser)
  );

  const segments = maxCommits <= 10 ? maxCommits : 6;

  return (
    <View style={{ width: "100%" }} onLayout={(e) => setWidth(e.nativeEvent.layout.width)}>
      {width > 0 && (
      <BarChart
        data={{
            labels: users,
            datasets: [{
                data: users.map(user => commitsCountByUser[user]),
                colors: users.map(user => () => generateColor(user))
            }]
        }}
        withCustomBarColorFromData
        width={width*0.6} 
        height={220}
        fromZero={true}
        yAxisLabel=""
        yAxisSuffix=""
        segments={segments}
        chartConfig={{
          propsForBackgroundLines: {
            translateX: "60"
          },
          backgroundColor: colors.background,
          backgroundGradientFrom: colors.background,
          backgroundGradientTo: colors.background,
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
      )}
    </View>
  );
}