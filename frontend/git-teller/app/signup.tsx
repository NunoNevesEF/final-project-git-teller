import { useState } from "react";
import { View, Text, TextInput, Pressable, Alert, ActivityIndicator } from "react-native";
import { useRouter } from "expo-router";
import OAuthRedirectButton from "../components/OAuthButton";
import { commonStyles } from "@/constants/commonStyles";

const DEFAULT_API_BASE = "http://localhost:8080";
const SIGNUP_PATH = "/authApiClient/public/accounts/signup";
const API_BASE = DEFAULT_API_BASE;

export default function Signup() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleSubmit = async () => {
        setLoading(true);
        setErrorMessage(null);

        try {
            const url =
                `${API_BASE}${SIGNUP_PATH}?email=${encodeURIComponent(email.trim())}` +
                `&username=${encodeURIComponent(username.trim())}` +
                `&password=${encodeURIComponent(password)}`;

            const response = await fetch(url, { method: "POST" });

            if (!response.ok) {
                let message = `Signup failed (${response.status})`;
                const text = await response.text();
                if (text) message = text;
                throw new Error(message);
            }

            Alert.alert("Success", "Account created!");
            router.replace("/login");
        } catch (err: any) {
            setErrorMessage(err?.message || "Signup failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <View style={[commonStyles.screen, commonStyles.centered]}>
            <Text style={commonStyles.authTitle}>Sign Up</Text>

            <TextInput
                placeholder="Email"
                value={email}
                onChangeText={setEmail}
                style={commonStyles.authInput}
                autoCapitalize="none"
                keyboardType="email-address"
                editable={!loading}
            />
            <TextInput
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
                style={commonStyles.authInput}
                autoCapitalize="none"
                editable={!loading}
            />
            <TextInput
                placeholder="Password"
                value={password}
                onChangeText={setPassword}
                style={commonStyles.authInput}
                secureTextEntry
                editable={!loading}
            />

            {errorMessage ? <Text style={commonStyles.errorText}>{errorMessage}</Text> : null}

            <Pressable
                style={[commonStyles.primaryButton, loading && commonStyles.buttonDisabled, commonStyles.fullWidth]}
                onPress={handleSubmit}
                disabled={loading}
            >
                {loading ? <ActivityIndicator color="#fff" /> : <Text style={commonStyles.primaryButtonText}>Create account</Text>}
            </Pressable>

            <OAuthRedirectButton
                label="Sign up with GitHub"
                provider="github"
                apiBase={API_BASE}
                disabled={loading}
                onError={setErrorMessage}
            />

            <OAuthRedirectButton
                label="Continue with Google"
                provider="google"
                apiBase={API_BASE}
                disabled={loading}
                onError={setErrorMessage}
            />
        </View>
    );
}
