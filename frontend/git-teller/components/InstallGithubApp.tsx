import React, { useEffect, useState } from 'react';
import { View, Text, Pressable, Linking, Platform, ActivityIndicator, FlatList } from 'react-native';
import { commonStyles } from '@/constants/commonStyles';
import { getGithubAppInstallations, linkGithubAppInstallation, GitHubInstallationCandidate } from '@/services/GithubService';

export default function InstallGitHubApp() {
    const [loading, setLoading] = useState(true);
    const [state, setState] = useState<any>(null);
    const [error, setError] = useState<string | null>(null);
    const [linkingId, setLinkingId] = useState<number | null>(null);

    const loadState = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getGithubAppInstallations();
            setState(data);
        } catch (err: any) {
            console.error('Error fetching GitHub App installations state', err);
            setError('Unable to fetch installation state');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadState();
    }, []);

    const handleInstallClick = async () => {
        if (!state?.installUrl) return;
        try {
            const url = state.installUrl;
            if (Platform.OS === 'web') {
                window.open(url, '_blank');
            } else {
                await Linking.openURL(url);
            }
        } catch (err: any) {
            console.error('Failed to open install URL', err);
        }
    };

    const handleLink = async (installationId: number) => {
        setLinkingId(installationId);
        try {
            await linkGithubAppInstallation(installationId);
            await loadState();
        } catch (err: any) {
            console.error('Failed to link installation', err);
            setError('Failed to link installation');
        } finally {
            setLinkingId(null);
        }
    };

    if (loading) return <ActivityIndicator />;

    return (
        <View style={{ marginBottom: 12 }}>
            <Text style={commonStyles.sectionTitle}>GitHub App</Text>

            <Text style={{ marginBottom: 8 }}>
                Install the GitHub App to give short-lived, repo-scoped access. You will be able to choose which repositories to allow on GitHub.
            </Text>

            <Pressable style={commonStyles.primaryButton} onPress={handleInstallClick}>
                <Text style={commonStyles.primaryButtonText}>Install GitHub App</Text>
            </Pressable>

            {state?.message ? <Text style={{ marginTop: 8 }}>{state.message}</Text> : null}

            <Text style={{ marginTop: 12, fontWeight: '600' }}>Detected installations</Text>

            {state?.discoveredInstallations?.length ? (
                <FlatList
                    data={state.discoveredInstallations as GitHubInstallationCandidate[]}
                    keyExtractor={(item) => String(item.installationId)}
                    renderItem={({ item }) => (
                        <View style={{ marginTop: 8, padding: 8, borderWidth: 1, borderColor: '#ddd', borderRadius: 6 }}>
                            <Text style={{ fontWeight: '600' }}>{item.accountLogin ?? 'Unknown account'}</Text>
                            <Text>Installation id: {item.installationId}</Text>
                            <Text>Repo selection: {item.repositorySelection ?? 'N/A'}</Text>
                            <Pressable
                                style={[commonStyles.secondaryButton, { marginTop: 8 }]}
                                onPress={() => handleLink(item.installationId)}
                                disabled={linkingId === item.installationId}
                            >
                                <Text style={commonStyles.secondaryButtonText}>
                                    {linkingId === item.installationId ? 'Linking...' : 'Link this installation'}
                                </Text>
                            </Pressable>
                        </View>
                    )}
                />
            ) : (
                <Text style={{ marginTop: 8 }}>No installations discovered for your GitHub account. Install the app and choose repositories, then return here.</Text>
            )}

            {state?.linkedInstallationIds?.length ? (
                <View style={{ marginTop: 12 }}>
                    <Text style={{ fontWeight: '600' }}>Linked installation ids:</Text>
                    <Text>{state.linkedInstallationIds.join(', ')}</Text>
                </View>
            ) : null}

            {error ? <Text style={commonStyles.errorText}>{error}</Text> : null}
        </View>
    );
}