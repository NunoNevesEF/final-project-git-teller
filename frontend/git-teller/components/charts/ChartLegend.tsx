import { View, Text } from "react-native";
import { generateColor } from "@/constants/theme";
import { useCommonStyles } from "@/constants/useCommonStyles";

/**
 * 
 * @param standardUsers is used when it is required to pass a diferente set of users in the first param
 * ex : in pie charts we are required to pass the name of the user + percentage, but we still need
 *      to genere color with the user's name only for consistency purposes in the page.
 * Users will be used as default but in special cases where standardUsers is not null, it will be used to generate color
 */

export default function ChartLegend({users, standardUsers}: {users: string[]; standardUsers?: string[]}) {
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
        {users.map((user, index) => (
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
                    backgroundColor: generateColor(
                    standardUsers?.[index] ?? user
                    ),
                    marginRight: 6,
                }}
                />

                <Text style={commonStyles.legendStyles}>
                {user}
                </Text>
            </View>
))}
        </View>
  );
}