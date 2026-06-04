import React from "react";
import { Dimensions, View, Text } from "react-native";
import { ContributionGraph } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { useTheme } from "@/constants/themeProvider";

const screenWidth = Dimensions.get("window").width;

export default function HeatMpaCommits({data,}: {data: Record<string, CommitDTO[]>;}) {
  const { colors } = useTheme();
  const [selectedDay, setSelectedDay] = React.useState<{
    date: string;
    count: number;
  } | null>(null);
  const map: Record<string, number> = {};

  const normalizeDate = (d: any) => {
    if (!d) return "";

    const date = new Date(d);

    if (isNaN(date.getTime())) return "";

    return date.toISOString().slice(0, 10);
  };

  Object.values(data).forEach((commits) => {
    commits.forEach((c) => {
      const day = new Date(c.timestamp).toISOString().slice(0, 10);
      map[day] = (map[day] || 0) + 1;
    });
  });

  const heatMapData = Object.entries(map).map(([date, count]) => ({
    date,
    count,
  }));

  const LEGEND = [
    { label: "Less", opacity: 0.1 },
    { label: "0.3", opacity: 0.3 },
    { label: "0.5", opacity: 0.5 },
    { label: "0.7", opacity: 0.7 },
    { label: "More", opacity: 1 },
  ];

  return (
    <View style={{
          alignItems: "center",
        }}>
      <ContributionGraph
        values={heatMapData}
        endDate={new Date()}
        numDays={105}
        width={screenWidth * 0.4}
        height={350}
        squareSize={25}
        gutterSize={10}
        chartConfig={{
          backgroundColor: colors.backgroundCard,
          backgroundGradientFrom: colors.backgroundCard,
          backgroundGradientTo: colors.backgroundCard,
          color: (opacity = 1) => `rgba(34, 197, 94, ${opacity})`,
          labelColor: () => colors.text,
        }}
        tooltipDataAttrs={() => ({})}
        onDayPress={(value: any) => {
          const rawDate = value?.date;
          const date = normalizeDate(rawDate);
          if (selectedDay?.date === date) {
            setSelectedDay(null)
            return;
          }
          setSelectedDay({
              date,
              count: map[date] ?? 0,
          });
        }}
      />

      {selectedDay && (
  <View
    style={{
      padding: 10,
      borderRadius: 8,
      backgroundColor: colors.backgroundCard,
      alignItems: "center",
    }}
  >
    <Text style={{ color: colors.text, fontWeight: "600" }}>
      {selectedDay.date}
    </Text>

    <Text style={{ color: colors.icon, marginTop: 4 }}>
      {selectedDay.count} commits
    </Text>
  </View>
)}

      <View
        style={{
          flexDirection: "row",
          alignItems: "center",
          justifyContent: "center",
          marginTop: 10,
        }}
      >
        <Text style={{ color: colors.text, marginRight: 8 }}>Less</Text>

        {LEGEND.map((l) => (
          <View
            key={l.opacity}
            style={{
              width: 12,
              height: 12,
              marginHorizontal: 2,
              borderRadius: 2,
              backgroundColor: `rgba(34, 197, 94, ${l.opacity})`,
            }}
          />
        ))}

        <Text style={{ color: colors.text, marginLeft: 8 }}>More</Text>
      </View>
    </View>
  );
}