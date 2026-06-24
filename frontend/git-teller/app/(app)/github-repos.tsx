import React, {useEffect, useState} from 'react';
import {View, Text} from 'react-native';
import {useRouter, Redirect} from 'expo-router';
import {useAuth} from '@/store/AuthProvider';
import GithubReposList from '@/components/GithubReposList';
import {getMyGithubRepos, RepositorySummary} from '@/services/GithubService';
import {analyzeRepo, ByShasRequest, ByDateRangeRequest, analyseRepoWithToken} from '@/services/GitCommunicationService';
import {useAnalysisInfoStore} from '@/store/useAnalysisInfoStore';
import {commonStyles} from '@/constants/commonStyles';
import LlmAnalysisSettings, {
    LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType,
} from '@/components/analysis/LlmAnalysisSettings';
import LoadingComponent from '@/components/utils/LoadingComponent';

export default function GithubReposPage() {
    const {isAuthenticated, loading} = useAuth();
    const router = useRouter();
    const setResult = useAnalysisInfoStore((state) => state.setResult);
    const setProjectName = useAnalysisInfoStore((state) => state.setProjectName);
    const setReportId = useAnalysisInfoStore((state) => state.setReportId)

    const [repos, setRepos] = useState<RepositorySummary[] | null>(null);
    const [reposLoading, setReposLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [analyzingId, setAnalyzingId] = useState<number | null>(null);
    const [analyzingWithLlm, setAnalyzingWithLlm] = useState<boolean | null>(null);
    const [isLoading, setIsLoading] = useState(false);


    const [settingsModalVisible, setSettingsModalVisible] = useState(false);
    const [selectedRepo, setSelectedRepo] = useState<RepositorySummary | null>(null);
    const [llmFilterType, setLlmFilterType] = useState<LlmFilterType>('overview');
    const [commitShas, setCommitShas] = useState('');
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [promptComplexity, setPromptComplexity] = useState<PromptComplexity>('SIMPLE');
    const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('DIFF');
    const [requestedAnalyses, setRequestedAnalyses] = useState<AnalysisType[]>(['DEFAULT']);

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
        llmAnalysisEnabled: boolean,
        byShas?: ByShasRequest,
        byDateRange?: ByDateRangeRequest,
    ) => {
        try {
            setAnalyzingId(repo.id);
            setAnalyzingWithLlm(llmAnalysisEnabled);
            setIsLoading(true)
            const result = await analyseRepoWithToken(repo.htmlUrl, llmAnalysisEnabled, repo.gitAccountId, byShas, byDateRange);

            setResult(result);
            setProjectName(getProjectName(repo.htmlUrl));
            setReportId(null)


            router.push('/Info');
        } catch (err) {
            console.error('Error analyzing repo:', err);
        } finally {
            setIsLoading(false)
            setAnalyzingId(null);
            setAnalyzingWithLlm(null);
        }
    };

    const openLlmSettings = (repo: RepositorySummary) => {
        setSelectedRepo(repo);
        setSettingsModalVisible(true);
    };

    const closeLlmSettings = () => {
        setSettingsModalVisible(false);
        setSelectedRepo(null);
    };

    const handleConfirmLlmAnalysis = () => {
        if (!selectedRepo) return;
        const repo = selectedRepo;
        const url = repo.htmlUrl;

        const byShas =
            llmFilterType === 'shas' && commitShas
                ? {
                    repoURI: url,
                    commitShas: commitShas.split(',').map((s) => s.trim()).filter(Boolean),
                    promptComplexity,
                    analysisMode,
                    requestedAnalyses,
                }
                : undefined;

        const byDateRange =
            llmFilterType === 'dateRange' && fromDate && toDate
                ? {
                    repoURI: url,
                    fromDate: `${fromDate}T00:00:00Z`,
                    toDate: `${toDate}T23:59:59Z`,
                    promptComplexity,
                    analysisMode,
                    requestedAnalyses,
                }
                : undefined;

        setSettingsModalVisible(false);
        setSelectedRepo(null);
        handleAnalyze(repo, true, byShas, byDateRange);
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
                    onAnalyzeWithLlm={(repo) => openLlmSettings(repo)}
                    onAnalyzeWithoutLlm={(repo) => handleAnalyze(repo, false)}
                />
            </View>

            <LlmAnalysisSettings
                visible={settingsModalVisible}
                onClose={closeLlmSettings}
                onConfirm={handleConfirmLlmAnalysis}
                confirmLabel="Start Analysis"
                llmFilterType={llmFilterType}
                onLlmFilterTypeChange={setLlmFilterType}
                commitShas={commitShas}
                onCommitShasChange={setCommitShas}
                fromDate={fromDate}
                onFromDateChange={setFromDate}
                toDate={toDate}
                onToDateChange={setToDate}
                promptComplexity={promptComplexity}
                onPromptComplexityChange={setPromptComplexity}
                analysisMode={analysisMode}
                onAnalysisModeChange={setAnalysisMode}
                requestedAnalyses={requestedAnalyses}
                onRequestedAnalysesChange={setRequestedAnalyses}
            />
        </View>
    );
}