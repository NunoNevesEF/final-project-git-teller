import { Pressable, Text, StyleSheet, Linking, Platform } from "react-native";

type Props = {
    label: string;
    provider: "google" | "github";
    apiBase?: string;
    disabled?: boolean;
    onError?: (message: string) => void;
};

const DEFAULT_API_BASE = "http://localhost:8080";

export default function OAuthRedirectButton({
                                                label,
                                                provider,
                                                apiBase = DEFAULT_API_BASE,
                                                disabled,
                                                onError,
                                            }: Props) {
    const handlePress = async () => {
        const url = `${apiBase}/oauth2/authorization/${provider}`;
        try {
            if (Platform.OS === "web") {
                window.location.href = url;
            } else {
                await Linking.openURL(url);
            }
        } catch (err: any) {
            onError?.(err?.message || `Unable to start ${provider} login.`);
        }
    };

    return (
        <Pressable style={[styles.button, disabled && styles.disabled]} onPress={handlePress} disabled={disabled}>
            <Text style={styles.text}>{label}</Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        padding: 15,
        borderRadius: 10,
        alignItems: "center",
        marginTop: 10,
        backgroundColor: "#24292e", // GitHub dark
    },
    disabled: { opacity: 0.7 },
    text: { color: "#fff", fontWeight: "bold", fontSize: 16 },
});
