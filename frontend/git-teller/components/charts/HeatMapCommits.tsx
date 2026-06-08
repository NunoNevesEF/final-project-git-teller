import React from "react";
import { View, Text, ScrollView } from "react-native";
import { ContributionGraph } from "react-native-chart-kit";
import { CommitDTO } from "@/models/CommitDTO";
import { REPORT_LAYOUT } from "@/constants/chartDescriptions"
import { useTheme } from "@/constants/themeProvider";

export default function HeatMapCommits({data,}: {data: Record<string, CommitDTO[]>;}) {
  const { colors } = useTheme();
  const { A4_WIDTH } = REPORT_LAYOUT;
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
      <ScrollView horizontal showsHorizontalScrollIndicator>
      <ContributionGraph
        values={heatMapData}
        endDate={new Date()}
        numDays={105}
        width={A4_WIDTH - 150}
        height={320}
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
  </ScrollView>

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