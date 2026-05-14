import { useCommonStyles } from "@/constants/useCommonStyles";
import { Pressable, Text, Linking, Platform } from "react-native";

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
    const styles = useCommonStyles();
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
        <Pressable
            style={[styles.oauthButton, disabled && styles.buttonDisabled]}
            onPress={handlePress}
            disabled={disabled}
        >
            <Text style={styles.oauthButtonText}>{label}</Text>
        </Pressable>
    );
}
