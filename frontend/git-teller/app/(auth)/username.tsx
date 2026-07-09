import { useState } from "react";
import { View, Text, TextInput, Pressable, ActivityIndicator } from "react-native";
import { router } from "expo-router";
import { useAuth } from "@/store/AuthProvider";
import { useCommonStyles } from "@/constants/useCommonStyles";
import apiClient from "@/services/authApiClient";

export default function Username() {
    const { accessToken, signIn } = useAuth();
    const commonStyles = useCommonStyles();

    const [username, setUsername] = useState("");
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleSubmit = async () => {
        if (!username.trim()) {
            setErrorMessage("Please choose a username.");
            return;
        }

        setLoading(true);
        setErrorMessage(null);

        try {
            await apiClient.put("/api/private/accounts/username", null, {
                params: { username: username.trim() },
            });

            await signIn({ accessToken: accessToken!, username: username.trim() });
            router.replace("../home");
        } catch (err: any) {
            if (err?.response?.status === 409) {
                setErrorMessage("That username is taken, choose another.");
            } else {
                setErrorMessage("Unable to set username. Please try again.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <View style={[commonStyles.screen, commonStyles.centered]}>
            <Text style={commonStyles.authTitle}>Choose a username</Text>

            <TextInput
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
                style={commonStyles.authInput}
                autoCapitalize="none"
                editable={!loading}
            />

            {errorMessage ? <Text style={commonStyles.errorText}>{errorMessage}</Text> : null}

            <Pressable
                style={[commonStyles.oauthButton, loading && commonStyles.buttonDisabled]}
                onPress={handleSubmit}
                disabled={loading}
            >
                {loading ? <ActivityIndicator color="#fff" /> : <Text style={commonStyles.oauthButtonText}>Continue</Text>}
            </Pressable>
        </View>
    );
}
