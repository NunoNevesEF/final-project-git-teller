import { StyleSheet } from "react-native";
import { spacing, fontSize } from "./theme";
import { useTheme } from "./themeProvider";

export const fonts = {
  regular: "Montserrat_400Regular",
  bold: "Montserrat_700Bold",
};

export const useCommonStyles  = () => {
  const { colors:c } = useTheme();

  return StyleSheet.create({
    screen: {
      flex: 1,
      padding: spacing.lg,
      backgroundColor: c.background,
    },
    formTitle: {
      fontSize: fontSize.xl,
      marginBottom: spacing.xl,
      fontFamily: fonts.bold,
      color: c.text,
    },
    pageTitle: {
      fontSize: 26,
      fontWeight: "700",
      textAlign: "center",
      color: c.text,
      fontFamily: fonts.regular
    },
    pageSubtitle: {
      fontSize: 14,
      color: c.icon,
      textAlign: "center",
      fontFamily: fonts.regular
    },
    optionButton: {
      backgroundColor: c.background,
      borderWidth: 1,
      borderColor: c.icon,
      paddingVertical: 6,
      paddingHorizontal: 12,
      borderRadius: 6,
      fontFamily: fonts.regular
    },
    optionButtonActive: {
      backgroundColor: c.tint,
    },
    optionButtonText: {
      color: c.text,
      fontFamily: fonts.regular
    },
    optionButtonTextActive: {
      color: c.background,
      fontFamily: fonts.regular
    },
    dangerButton: {
      backgroundColor: "#ef4444",
    },
    dangerButtonText: {
      color: "#fff",
    },
    topLeftActionButton: {
      alignSelf: 'flex-start',
      borderWidth: 1,
      borderColor: c.tint,
      paddingVertical: 10,
      paddingHorizontal: 12,
      borderRadius: 8,
      marginBottom: 10,
      backgroundColor: c.background,
      fontFamily: fonts.regular
    },
    topLeftActionButtonText: {
      textAlign: "center",
      color: c.text,
    },
    searchInput: {
      width: '100%',
      borderWidth: 1,
      borderColor: c.tint,
      paddingVertical: 10,
      paddingHorizontal: 12,
      borderRadius: 8,
      marginBottom: 10,
      backgroundColor: c.background,
      color: c.text,
      fontFamily: fonts.regular
    },
    authInput: {
      width: '60%',
      borderWidth: 1,
      borderColor: c.tint,
      paddingVertical: 10,
      paddingHorizontal: 12,
      borderRadius: 8,
      marginBottom: 10,
      backgroundColor: c.background,
      color: c.text,
      fontFamily: fonts.regular
    },

    authTitle: {
      fontSize: 22,
      fontWeight: '700',
      marginBottom: 18,
      textAlign: 'center',
      color: c.text,
      fontFamily: fonts.regular
    },
    primaryButton: {
      backgroundColor: c.tint,
      paddingVertical: 10,
      paddingHorizontal: 12,
      borderRadius: 8,
      alignItems: 'center',
      justifyContent: 'center',
      marginTop: 8,
      fontFamily: fonts.regular
    },
    primaryButtonText: {
      color: c.background,
      fontFamily: fonts.regular
    },
    errorText: {
      color: 'red',
      marginBottom: 8,
      textAlign: 'center',
    },
    buttonDisabled: {
      opacity: 0.7,
    },
    fullWidth: {
      width: '100%',
    },
    centered: {
      justifyContent: "center",
      alignItems: "center",
    },
    optionGroup: {
      flexDirection: "row",
      gap: spacing.sm,
      marginBottom: spacing.lg,
      width: "100%",
      justifyContent: "center",
    },
    inputSection: {
      borderColor: c.icon,
      width: "100%",
      marginBottom: spacing.lg,
      gap: spacing.sm,
      maxWidth: 300,
      alignSelf: "center"
    },
    root: {
      flex: 1,
    },
    oauthButton: {
      width: '15%',
      alignSelf: 'center',
      padding: 15,
      borderRadius: 10,
      alignItems: "center",
      marginTop: 10,
      backgroundColor: c.icon,
      fontFamily: fonts.regular
    },
    oauthButtonText: {
      color: c.text,
      fontWeight: "bold",
      fontSize: 16,
      fontFamily: fonts.regular
    },
    titleText: {
      color: c.text,
      fontWeight: "bold",
      fontSize: 16,
      padding : 20,
      fontFamily: fonts.regular
    },
    CardChart: {
      fontFamily: fonts.regular,
      backgroundColor: c.backgroundCard,
      borderRadius: 24,
      padding: 12,
      marginBottom: 16,

      borderWidth: 1,
      borderColor: "rgba(255, 255, 255, 0.06)",

      shadowColor: "#000",
      shadowOpacity: 0.35,
      shadowRadius: 16,
      shadowOffset: {
        width: 0,
        height: 8,
      },
      elevation: 6,
      ...( {
    breakInside: "avoid",
    pageBreakInside: "avoid",
  } as any )
    }
  });
};
