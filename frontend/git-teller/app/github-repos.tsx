import React, { useEffect, useState } from 'react';
import { View, Text, Pressable } from 'react-native';
import { Link } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import { Redirect } from 'expo-router';
import GithubReposList from '@/components/GithubReposList';
import { getMyGithubRepos, RepositorySummary } from '@/services/GithubService';
import { commonStyles } from '@/constants/commonStyles';

export default function GithubReposPage() {
    const { isAuthenticated, loading } = useAuth();
    const [repos, setRepos] = useState<RepositorySummary[] | null>(null);
    const [reposLoading, setReposLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadRepos();
    }, []);

    const loadRepos = async () => {
        setReposLoading(true);
        setError(null);
        try {
            const data = await getMyGithubRepos();
            setRepos(data);
        } catch (err: any) {
            if (err?.response?.status === 401) {
                setError('Não existe uma conta GitHub associada a este utilizador.');
            } else {
                setError('Error loading repos. Try again.');
                console.error('Error loading repos:', err);
            }
        } finally {
            setReposLoading(false);
        }
    };

    if (loading) return null;
    if (!isAuthenticated) return <Redirect href="/login" />;

    return (
        <View style={[commonStyles.screen]}>


            <Text style={commonStyles.pageTitle}>My Repos</Text>
            <Text style={commonStyles.pageSubtitle}>GitHub Repositorys</Text>

            <View style={commonStyles.reposList}>
                <GithubReposList
                    repos={repos}
                    loading={reposLoading}
                    error={error}
                    onRetry={loadRepos}
                />
            </View>
        </View>
    );
}