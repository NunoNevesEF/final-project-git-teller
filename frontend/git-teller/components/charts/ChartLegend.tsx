import { View, Text } from "react-native";
import { generateColor } from "@/constants/theme";
import { useCommonStyles } from "@/constants/useCommonStyles";

export default function ChartLegend({users,}: {users: string[];}) {
  const commonStyles = useCommonStyles();
  return (
    <View
        style={{
            flexDirection: "row",
            flexWrap: "wrap",
            justifyContent: "center",
            gap: 12,
            marginBottom : 12
        }}
    >
        {users.map(user => (
            <View
            key={user}
            style={{
                flexDirection: "row",
                alignItems: "center",
            }}
            >
            <View
                style={{
                width: 12,
                height: 12,
                borderRadius: 6,
                backgroundColor: generateColor(user),
                marginRight: 6,
                }}
            />
            <Text style={commonStyles.legendStyles}>{user}</Text>
            </View>
        ))}
        </View>
  );
}