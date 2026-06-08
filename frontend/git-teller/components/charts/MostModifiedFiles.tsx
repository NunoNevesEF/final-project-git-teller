import React from "react";
import { View, Text } from "react-native";
import { useTheme } from "@/constants/themeProvider";
import { ModifiedFile } from "@/models/ModifiedFile";
import { Ionicons } from "@expo/vector-icons";

function getFileIcon(extension: string, color: string) {
  switch (extension) {
    case "kt":
    case "java":
    case "ts":
    case "js":
      return <Ionicons name="code-slash-outline" size={18} color={color} />;

    case "json":
    case "xml":
    case "yml":
      return <Ionicons name="document-text-outline" size={18} color={color} />;

    case "md":
      return <Ionicons name="reader-outline" size={18} color={color} />;

    default:
      return <Ionicons name="document-outline" size={18} color={color} />;
  }
}

export default function MostModifiedFiles({
  data,
}: {
  data: ModifiedFile[];
}) {
  const { colors } = useTheme();

  const formatDate = (timestamp: number) =>
    new Date(timestamp * 1000).toLocaleDateString("pt-PT", {
      year: "numeric",
      month: "short",
      day: "2-digit",
    });

  return (
    <View
      style={{
        borderRadius: 12,
        overflow: "hidden",
      }}
    >
      <View
        style={{
          flexDirection: "row",
          paddingVertical: 10,
          paddingHorizontal: 12,
          backgroundColor: colors.backgroundCard,
        }}
      >
        <Text style={{ flex: 2, color: colors.text, fontWeight: "600" }}>
          File
        </Text>
        <Text style={{ flex: 1, textAlign: "center", color: colors.text, fontWeight: "600" }}>
          Changes
        </Text>
        <Text style={{ flex: 1.2, textAlign: "right", color: colors.text, fontWeight: "600" }}>
          Date
        </Text>
      </View>

      {data.map((file) => (
        <View
          key={file.path}
          style={{
            flexDirection: "row",
            paddingVertical: 12,
            paddingHorizontal: 12,
            alignItems: "center",
          }}
        >
          <View style={{ flex: 2, flexDirection: "row", alignItems: "center" }}>
            <View style={{ marginRight: 8 }}>
              {getFileIcon(file.extension, colors.text)}
            </View>

            <Text
  style={{
    color: colors.text,
    flexShrink: 1,
  }}
>
  {file.path}
</Text>
          </View>

          <Text style={{ flex: 1, textAlign: "center", color: colors.text }}>
            {file.changes}
          </Text>

          <Text style={{ flex: 1.2, textAlign: "right", color: colors.icon }}>
            {formatDate(file.lastModified)}
          </Text>
        </View>
      ))}
    </View>
  );
}