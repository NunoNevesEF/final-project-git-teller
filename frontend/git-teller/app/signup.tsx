import { useState } from 'react';
import { View, Text, TextInput, Pressable, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';

const DEFAULT_API_BASE = 'http://localhost:8080';

export default function signup() {
    const router = useRouter();

    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const API_BASE = DEFAULT_API_BASE;
    const handleSubmit = async () => {

        setLoading(true);
        setErrorMessage(null);
        try {

            const url =
                `${API_BASE}/api/public/accounts/signup?` +
                `email=${encodeURIComponent(email.trim())}` +
                `&username=${encodeURIComponent(username.trim())}` +
                `&password=${encodeURIComponent(password)}`;

            const response = await fetch(url, {
                method: 'POST',
            });

            if (!response.ok) {
                let message = `Signup failed (${response.status})`;
                try {
                    const text = await response.text();
                    if (text) {
                        try {
                            const json = JSON.parse(text);
                            message = json?.message || JSON.stringify(json) || message;
                        } catch {
                            message = text;
                        }
                    }
                } catch {
                }
                throw new Error(message);
            }

            const data = await response.json();
            console.log('User created:', data);

            Alert.alert('Success', 'Account created!');
            // go back to previous screen (e.g., login)
            router.replace("/");
        } catch (error: any) {

        } finally {
            setLoading(false);
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Create Account</Text>

            <TextInput
                placeholder="Email"
                value={email}
                onChangeText={setEmail}
                style={styles.input}
                autoCapitalize="none"
                keyboardType="email-address"
                editable={!loading}
            />

            <TextInput
                placeholder="Username"
                value={username}
                onChangeText={setUsername}
                style={styles.input}
                autoCapitalize="none"
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

            <Pressable style={[styles.button, loading && styles.buttonDisabled]} onPress={handleSubmit} disabled={loading}>
                {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Sign Up</Text>}
            </Pressable>

        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        padding: 20,
        backgroundColor: '#fff',
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        marginBottom: 30,
        textAlign: 'center',
    },
    input: {
        borderWidth: 1,
        borderColor: '#ddd',
        padding: 15,
        borderRadius: 10,
        marginBottom: 15,
    },
    button: {
        backgroundColor: '#007AFF',
        padding: 15,
        borderRadius: 10,
        alignItems: 'center',
        marginTop: 10,
    },
    buttonDisabled: {
        opacity: 0.7,
    },
    buttonText: {
        color: '#fff',
        fontWeight: 'bold',
        fontSize: 16,
    },
    link: {
        marginTop: 20,
        textAlign: 'center',
        color: '#007AFF',
    },
    error: {
        color: 'red',
        marginBottom: 8,
        textAlign: 'center',
    },
    hint: {
        marginTop: 20,
    },
    hintText: {
        color: '#666',
        fontSize: 12,
        textAlign: 'center',
    },
});
