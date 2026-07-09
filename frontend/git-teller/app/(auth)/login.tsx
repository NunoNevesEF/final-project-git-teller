import { useEffect, useRef, useState } from "react";
import { View, Text, TextInput, Pressable, Alert, ActivityIndicator } from "react-native";
import { Link, router, useLocalSearchParams } from "expo-router";
import { useAuth } from "@/store/AuthProvider";
import { useCommonStyles } from "@/constants/useCommonStyles";
import OAuthRedirectButton from "@/components/login/OAuthButton";

const DEFAULT_API_BASE = "https://git-teller-api.up.railway.app";
const LOGIN_PATH = "/api/public/auth/login";
const API_BASE = DEFAULT_API_BASE;

export default function Login() {
    const { signIn } = useAuth();

    const params = useLocalSearchParams<{
        accessToken?: string | string[];
        refreshToken?: string | string[];
        needsUsername?: string | string[];
        username?: string | string[];
    }>();

    const [Email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const oauthHandledRef = useRef(false);
    const commonStyles = useCommonStyles();

    // Handle OAuth redirect login if accessToken is present in URL params
    useEffect(() => {
        if (oauthHandledRef.current) return;

        const firstParam = (value?: string | string[]) =>
            typeof value === "string" ? value : Array.isArray(value) ? value[0] : undefined;

        const accessToken = firstParam(params.accessToken);
        const refreshToken = firstParam(params.refreshToken);
        const needsUsername = firstParam(params.needsUsername) === "true";
        const username = firstParam(params.username);

        if (!accessToken) return;

        oauthHandledRef.current = true;

        const finalizeOAuthLogin = async () => {
            try {
                setLoading(true);
                setErrorMessage(null);

                if (needsUsername) {
                    await signIn({ accessToken, refreshToken });
                    router.replace("./username");
                    return;
                }

                await signIn({ accessToken, refreshToken, username: username || "User" });
                router.replace("../home");
            } catch (err: any) {
                oauthHandledRef.current = false;
                setErrorMessage(err?.message || "Unable to complete Google login.");
            } finally {
                setLoading(false);
            }
        };

        finalizeOAuthLogin();
    }, [params.accessToken, params.refreshToken, params.needsUsername, params.username, signIn]);

    const handleLogin = async () => {
        setLoading(true);
        setErrorMessage(null);

        try {
            const url = `${API_BASE}${LOGIN_PATH}?email=${encodeURIComponent(Email.trim())}&password=${encodeURIComponent(password)}`;
            const response = await fetch(url, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
            });

            if (!response.ok) {
                let message = `Login failed (${response.status})`;
                try {
                    const text = await response.text();
                    if (text) {
                        try {
                            const json = JSON.parse(text);
                            message = json?.message || json?.error || JSON.stringify(json) || message;
                        } catch {
                            message = text;
                        }
                    }
                } catch {}
                throw new Error(message);
            }

            const data = await response.json();
            const accessToken = data?.id_token ?? data?.token ?? data?.accessToken ?? data?.tokenValue;
            const refreshToken = data?.refreshToken;

            const username =
                data?.username ??
                data?.user?.username ??
                data?.name ??
                (Email.includes("@") ? Email.split("@")[0] : Email);

            if (!accessToken) {
                throw new Error("Login succeeded but no access token was returned.");
            }

            await signIn({ accessToken, refreshToken, username });
            router.replace("../home");
        } catch (err: any) {
            const message = err?.message || "Unable to login. Please try again.";
            setErrorMessage(message);
            Alert.alert("Login error", message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <View style={[commonStyles.screen, commonStyles.centered]}>
            <Link href="../signup" asChild>
                <Pressable style={commonStyles.topLeftActionButton}>
                    <Text style={commonStyles.topLeftActionButtonText}>Sign Up</Text>
                </Pressable>
            </Link>

            <Text style={commonStyles.authTitle}>Log in</Text>

            <TextInput
                placeholder="Email"
                value={Email}
                onChangeText={setEmail}
                style={commonStyles.authInput}
                autoCapitalize="none"
                keyboardType="email-address"
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
                style={[commonStyles.oauthButton, loading && commonStyles.buttonDisabled]}
                onPress={handleLogin}
                disabled={loading}
            >
                {loading ? <ActivityIndicator color="#fff" /> : <Text style={commonStyles.oauthButtonText}>Log In</Text>}
            </Pressable>

            <OAuthRedirectButton
                label="Continue with GitHub"
                provider="github"
                apiBase={API_BASE}
                disabled={loading}
                onError={setErrorMessage}
            />

            <OAuthRedirectButton
                label="Continue with Gitlab"
                provider="gitlab"
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
