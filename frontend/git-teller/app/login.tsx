import { useState } from "react";
import { View, Text, TextInput, Pressable, StyleSheet, Alert, ActivityIndicator } from "react-native";
import { useRouter } from "expo-router";


const DEFAULT_API_BASE = "http://localhost:8080";
const LOGIN_PATH = "/api/public/auth/login";
const API_BASE = DEFAULT_API_BASE;

export default function Login() {
    const router = useRouter();
    const [Email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);



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
                } catch {
                    /* ignore */
                }
                throw new Error(message);
            }

            const data = await response.json();
            console.log("Login response:", data);


            const token = data?.id_token ?? data?.token ?? data?.accessToken ?? data?.tokenValue;
            if (token) {
                // TODO: token store
                console.log("Received token:", token);
            }
            router.replace("/");
        } catch (err: any) {
        } finally {
            setLoading(false);
        }
    };


    return (
        <View style={styles.container}>
            <Text style={styles.title}>Log in</Text>

            <TextInput
                placeholder="Email"
                value={Email}
                onChangeText={setEmail}
                style={styles.input}
                autoCapitalize="none"
                keyboardType="email-address"
                editable={!loading}
            />

            <TextInput
                placeholder="Password"
                value={password}
                onChangeText={setPassword}
                style={styles.input}
                secureTextEntry
                editable={!loading}
            />

            {errorMessage ? <Text style={styles.error}>{errorMessage}</Text> : null}

            <Pressable style={[styles.button, loading && styles.buttonDisabled]} onPress={handleLogin} disabled={loading}>
                {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Log In</Text>}
            </Pressable>

        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, justifyContent: "center", padding: 20, backgroundColor: "#fff" },
    title: { fontSize: 28, fontWeight: "bold", marginBottom: 30, textAlign: "center" },
    input: { borderWidth: 1, borderColor: "#ddd", padding: 15, borderRadius: 10, marginBottom: 15 },
    button: { backgroundColor: "#007AFF", padding: 15, borderRadius: 10, alignItems: "center", marginTop: 10 },
    buttonDisabled: { opacity: 0.7 },
    buttonText: { color: "#fff", fontWeight: "bold", fontSize: 16 },
    link: { marginTop: 20, textAlign: "center", color: "#007AFF" },
    error: { color: "red", marginBottom: 8, textAlign: "center" },
    hint: { marginTop: 20 },
    hintText: { color: "#666", fontSize: 12, textAlign: "center" },
});
