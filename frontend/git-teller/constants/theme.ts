/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://reactnativeunistyles.vercel.app), etc.
 */

import { Platform } from "react-native";

const tintColorLight = "#0a7ea4";
const tintColorDark = "#fff";

export const Colors = {
  light: {
    text: "#11181C",
    background: "#f9fafb",
    tint: tintColorLight,
    icon: "#687076",
    tabIconDefault: "#687076",
    tabIconSelected: tintColorLight,
    backgroundCard: "#fefefe",
  },
  dark: {
    text: "#ECEDEE",
    background: "#0d141e",
    tint: tintColorDark,
    icon: "#9BA1A6",
    tabIconDefault: "#9BA1A6",
    tabIconSelected: tintColorDark,
    backgroundCard: "#0d141e",
  },
};

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: "system-ui",
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: "ui-serif",
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: "ui-rounded",
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: "ui-monospace",
  },
  default: {
    sans: "normal",
    serif: "serif",
    rounded: "normal",
    mono: "monospace",
  },
  web: {
    sans: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    serif: "Georgia, 'Times New Roman', serif",
    rounded:
      "'SF Pro Rounded', 'Hiragino Maru Gothic ProN', Meiryo, 'MS PGothic', sans-serif",
    mono: "SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace",
  },
});

export const colors = {
  background: "#f5f5f5",
  surface: "#ffffff",
  textPrimary: "#333333",
  textMuted: "#666666",
  border: "#dddddd",
  primary: "#007AFF",
  primaryText: "#ffffff",
  neutralButton: "#e8e8e8",
};

export const spacing = {
  xs: 8,
  sm: 12,
  md: 16,
  lg: 20,
  xl: 30,
};

export const radius = {
  sm: 6,
  md: 10,
};

export const fontSize = {
  sm: 12,
  md: 14,
  lg: 16,
  xl: 28,
};

const COLORS = [
  "#e6194b",
  "#3cb44b",
  "#4363d8",
  "#f58231",
  "#911eb4",
  "#42d4f4",
  "#f032e6",
  "#bfef45",
  "#fabed4",
  "#469990",
  "#dcbeff",
  "#9a6324",
  "#ffe119",
  "#800000",
  "#aaffc3",
  "#808000",
  "#ffd8b1",
  "#000075",
];

const colorCache = new Map<string, string>();

export const generateColor = (str: string) => {
  if (colorCache.has(str)) return colorCache.get(str)!;

  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash * 31 + str.charCodeAt(i)) >>> 0;
  }

  const color = COLORS[hash % COLORS.length];

  colorCache.set(str, color);
  return color;
};
