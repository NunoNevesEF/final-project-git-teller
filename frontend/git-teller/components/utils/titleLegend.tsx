import React from "react";
import { Text } from "react-native";
import { useCommonStyles } from "@/constants/useCommonStyles";

export default function Title({ text, size = 18 }: any) {
  const commonStyles = useCommonStyles();

  return (
    <Text
      style={commonStyles.titleText}>
      {text}
    </Text>
  );
}