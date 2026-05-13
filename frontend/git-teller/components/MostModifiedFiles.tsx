import React from "react";
import { View, Text } from "react-native";
import { useTheme } from "@/constants/themeProvider";
import { ModifiedFile } from "@/models/ModifiedFile";
import { fonts } from "@/constants/useCommonStyles";

export default function MostModifiedFiles({ data}: {data: ModifiedFile[]}) {
  const { colors } = useTheme();

  return (
    <View>
      {data.map((file) => {

        return (
          <View
            key={file.first}
            style={{
              marginBottom: 16,
            }}
          >
            <Text
              numberOfLines={1}
              style={{
                color: colors.text,
                marginBottom: 4,
                marginLeft : 30,
                fontWeight: "600",
                fontFamily: fonts.regular
              }}
            >
              {file.first}
            </Text>

            <Text
              style={{
                color: colors.icon,
                marginTop: 4,
                marginLeft : 30,
                fontSize: 12,
                fontFamily: fonts.regular
              }}
            >
              {file.second} commits
            </Text>
          </View>
        );
      })}
    </View>
  );
}