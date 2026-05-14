import { View, Text } from "react-native";
import { useState } from "react";
import { fonts, useCommonStyles } from "@/constants/useCommonStyles";
import { useTheme } from "@/constants/themeProvider";
import { Ionicons } from "@expo/vector-icons";

export default function ChartCard({
  title,
  description,
  children,
  showToolTip = true,
  icon
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
  showToolTip?:boolean;
  icon?: keyof typeof Ionicons.glyphMap; 
}) {
  const [open, setOpen] = useState(false);
  const commonStyles = useCommonStyles();
  const { colors } = useTheme()

  return (
    <View
      style={commonStyles.CardChart}
    >
      <View
        style={{
          flexDirection: "row",
          justifyContent: "space-between",
          marginBottom: 8,
        }}
      >
        <View
          style={[
            {
              flexDirection: "row",
              alignItems: "center",
              gap: 6,
            },
            icon && { marginLeft: 16 },
          ]}
        >
          {icon && (
            <Ionicons name={icon} size={18} color="#4A90E2" />
          )}
          <Text style={commonStyles.titleText}>
            {title}
          </Text>
        </View>

        {showToolTip && <Ionicons name="information-circle-outline" size={20} color="#4A90E2" onPress={() => setOpen(!open)}/>}
      </View>

      <View>{children}</View>

      {open && description && (
        <View style={{ marginTop: 8 }}>
          <Text style={{ color: colors.text, fontSize: 16, fontFamily: fonts.regular, padding: 16 }}>
            {description}
          </Text>
        </View>
      )}
    </View>
  );
}