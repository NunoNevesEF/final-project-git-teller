import React from 'react';
import { View, Text, FlatList, ActivityIndicator, Pressable, Linking } from 'react-native';
import { RepositorySummary } from '@/services/GithubService';
import { commonStyles } from '@/constants/commonStyles';

export default function GithubReposList({
                                            repos,
                                            loading,
                                            error,
                                            onRetry,
                                        }: {
    repos: RepositorySummary[] | null;
    loading: boolean;
    error: string | null;
    onRetry?: () => void;
}) {
    if (loading) return <ActivityIndicator />;

    if (error) {
        return (
            <View>
                <Text style={commonStyles.errorText}>{error}</Text>
                {onRetry ? (
                    <Pressable style={commonStyles.primaryButton} onPress={onRetry}>
                        <Text style={commonStyles.primaryButtonText}>Retry</Text>
                    </Pressable>
                ) : null}
            </View>
        );
    }

    if (!repos || repos.length === 0) {
        return <Text>Nenhum repositório encontrado.</Text>;
    }

    return (
        <FlatList
            data={repos}
            keyExtractor={(item) => String(item.id)}
            renderItem={({ item }) => (
                <Pressable
                    onPress={() => {
                        Linking.openURL(item.htmlUrl).catch(() => {}); // apenas abre no browser por agora
                    }}
                    style={[commonStyles.repoItem]}
                >
                    <Text style={commonStyles.repoName}>{item.fullName}</Text>
                    {item.description ? <Text style={commonStyles.repoDesc}>{item.description}</Text> : null}
                    <View style={{ flexDirection: 'row', gap: 12 }}>
                        <Text style={commonStyles.repoMeta}> Stars - {item.starsCount}</Text>
                        <Text style={commonStyles.repoMeta}> Forks - {item.forksCount}</Text>
                        {item.language ? <Text style={commonStyles.repoMeta}>{item.language}</Text> : null}
                    </View>
                </Pressable>
            )}
        />
    );
}