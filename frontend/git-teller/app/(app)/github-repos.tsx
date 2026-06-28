import React, {useEffect, useState} from 'react';
import {View, Text} from 'react-native';
import {useRouter, Redirect} from 'expo-router';
import {useAuth} from '@/store/AuthProvider';
import GithubReposList from '@/components/GithubReposList';
import {getMyGithubRepos, RepositorySummary} from '@/services/GithubService';
import {analyzeRepo} from '@/services/GitCommunicationService';
import {useAnalysisInfoStore} from '@/store/useAnalysisInfoStore';
import {commonStyles} from '@/constants/commonStyles';
import LoadingComponent from '@/components/utils/LoadingComponent';

export default function GithubReposPage() {
    const {isAuthenticated, loading} = useAuth();
    const router = useRouter();
    const setRepoURI = useAnalysisInfoStore((s) => s.setRepoURI);
    const setResult = useAnalysisInfoStore((state) => state.setResult);
    const setProjectName = useAnalysisInfoStore((state) => state.setProjectName);
    const setReportId = useAnalysisInfoStore((state) => state.setReportId)

    const [repos, setRepos] = useState<RepositorySummary[] | null>(null);
    const [reposLoading, setReposLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const getProjectName = (url: string) =>
        url.split("/").filter(Boolean).pop() ?? "";

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

    const handleAnalyze = async (
        repo: RepositorySummary,
    ) => {
        try {
            setIsLoading(true)
            
            const request = {
                repoURI: repo.htmlUrl,
                gitAccountId: repo.gitAccountId,
                dateFilter: null,
                llmRequest: null
            }

            const result = await analyzeRepo(request);

            setResult(result);
            setProjectName(getProjectName(repo.htmlUrl));
            setReportId(null)


            router.push('/Info');
        } catch (err) {
            console.error('Error analyzing repo:', err);
        } finally {
            setIsLoading(false)
        }
    };

    const handleSearchFilter = (repo: RepositorySummary) => {
        setRepoURI(repo.htmlUrl);
        router.push("/(app)/home");
    };

    if (loading) return null;
    if (!isAuthenticated) return <Redirect href="../login"/>;

    return (
        <View style={commonStyles.screen}>
            <LoadingComponent visible={isLoading} />
            <Text style={commonStyles.pageSubtitle}>GitHub Repositories</Text>

            <View style={commonStyles.reposList}>
                <GithubReposList
                    repos={repos}
                    loading={reposLoading}
                    error={error}
                    onRetry={loadRepos}
                    onAnalyzeWithLlm={(repo) => handleSearchFilter(repo)}
                    onAnalyzeWithoutLlm={(repo) => handleAnalyze(repo)}
                />
            </View>
        </View>
    );
}