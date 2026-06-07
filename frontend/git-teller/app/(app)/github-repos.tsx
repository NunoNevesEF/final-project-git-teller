import React, { useEffect, useState } from 'react';
import { View, Text } from 'react-native';
import { useRouter, Redirect } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import GithubReposList from '@/components/GithubReposList';
import { getMyGithubRepos, RepositorySummary } from '@/services/GithubService';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { commonStyles } from '@/constants/commonStyles';

export default function GithubReposPage() {
    const { isAuthenticated, loading } = useAuth();
    const router = useRouter();
    const setResult = useAnalysisStore((state) => state.setResult);
    const setInput = useAnalysisStore((state) => state.setInput);

    const [repos, setRepos] = useState<RepositorySummary[] | null>(null);
    const [reposLoading, setReposLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [analyzingId, setAnalyzingId] = useState<number | null>(null);
    const [analyzingWithLlm, setAnalyzingWithLlm] = useState<boolean | null>(null);

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

    const handleAnalyze = async (repo: RepositorySummary, llmAnalysisEnabled: boolean) => {
        try {
            setAnalyzingId(repo.id);
            setAnalyzingWithLlm(llmAnalysisEnabled);
            const result = await analyzeRepo(repo.htmlUrl, llmAnalysisEnabled);

            setResult(result);

            const [repositoryOwner = '', repositoryName = repo.name] = repo.fullName.split('/');
            setInput({
                repositoryUrl: repo.htmlUrl,
                repositoryName,
                repositoryOwner,
                platform: 'github',
                llmAnalysisEnabled,
            });
            router.push('/Info');
        } catch (err) {
            console.error('Error analyzing repo:', err);
        } finally {
            setAnalyzingId(null);
            setAnalyzingWithLlm(null);
        }
    };

    if (loading) return null;
    if (!isAuthenticated) return <Redirect href="../login" />;

    return (
        <View style={commonStyles.screen}>
            <Text style={commonStyles.pageTitle}>My Repos</Text>
            <Text style={commonStyles.pageSubtitle}>GitHub Repositories</Text>

            <View style={commonStyles.reposList}>
                <GithubReposList
                    repos={repos}
                    loading={reposLoading}
                    error={error}
                    onRetry={loadRepos}
                    onAnalyzeWithLlm={(repo) => handleAnalyze(repo, true)}
                    onAnalyzeWithoutLlm={(repo) => handleAnalyze(repo, false)}
                />
            </View>

            {analyzingId !== null ? (
                <Text style={{ textAlign: 'center', marginTop: 12 }}>
                    {analyzingWithLlm ? 'Analyzing repository with LLM...' : 'Analyzing repository without LLM...'}
                </Text>
            ) : null}
        </View>
    );
}
