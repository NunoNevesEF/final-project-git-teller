import React from "react";
import { View, Text } from "react-native";
import { useTheme } from "@/constants/themeProvider";
import { Ionicons } from "@expo/vector-icons";
import { fonts } from "@/constants/useCommonStyles";

export default function GitInputInfo({value1,label1,value2,label2}: {
  value1: string;
  label1: string;
  value2: string;
  label2: string;
  icon: keyof typeof Ionicons.glyphMap;
}) {
  const { colors } = useTheme();

  return (
    <View
      style={{
        flex: 1,
        padding: 16,
        backgroundColor: colors.backgroundCard,
        marginTop: -26
      }}
    >
      <Text
        style={{
          color: colors.text,
          fontFamily: fonts.bold,
          marginBottom: 4,
        }}
      >
        {label1}
      </Text>

      <Text
        style={{
          color: colors.text,
          fontFamily: fonts.regular,
          marginBottom: 12,
        }}
      >
        {value1}
      </Text>

      <Text
        style={{
          color: colors.text,
          fontFamily: fonts.bold,
          marginBottom: 4,
        }}
      >
        {label2}
      </Text>

      <Text style={{ color: colors.text, fontFamily: fonts.regular }}>
        {value2}
      </Text>
    </View>
  );
}