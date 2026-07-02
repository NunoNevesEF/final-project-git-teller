import React, {useEffect, useState} from 'react';
import {View, Text} from 'react-native';
import {useRouter, Redirect} from 'expo-router';
import {useAuth} from '@/store/AuthProvider';
import GithubReposList from '@/components/gitRepos/GithubReposList';
import {getMyGithubRepos, RepositorySummary, UserRepositoriesDto} from '@/services/GithubService';
import {analyzeRepo, ByShasRequest, ByDateRangeRequest, analyseRepoWithToken} from '@/services/GitCommunicationService';
import {useAnalysisInfoStore} from '@/store/useAnalysisInfoStore';
import {commonStyles} from '@/constants/commonStyles';
import LlmAnalysisSettings, {
    LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType,
} from '@/components/analysis/LlmAnalysisSettings';
import LoadingComponent from '@/components/utils/LoadingComponent';
import {OAuthLinkedAccountListItemDTO} from "@/models/account/OAuthLinkedAccountListItemDTO";
import {listGitAccounts} from "@/services/AccountService";
import {AccountDropdownMenu} from "@/components/gitRepos/AccountsDropdownMenu";
import PaginationButton from "@/components/utils/PaginationButton";

export default function GithubReposPage() {
    const {isAuthenticated, loading} = useAuth();
    const router = useRouter();
    const setResult = useAnalysisInfoStore((state) => state.setResult);
    const setProjectName = useAnalysisInfoStore((state) => state.setProjectName);
    const setReportId = useAnalysisInfoStore((state) => state.setReportId)

    const firstPage = 1
    const [currPage, setCurrPage] = useState<number>(firstPage)
    const [lastPage, setLastPage] = useState<number>(firstPage)

    const [accountList, setAccountList] = useState<OAuthLinkedAccountListItemDTO[]>([])
    const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null)

    const [repos, setRepos] = useState<UserRepositoriesDto>({lastPage: lastPage, repositories: []});
    const [selectedRepo, setSelectedRepo] = useState<RepositorySummary | null>(null);
    const [reposLoading, setReposLoading] = useState(true);

    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const [settingsModalVisible, setSettingsModalVisible] = useState(false);
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
        handleLoadRepoList()
    }, []);

    useEffect(() => {
        if(selectedAccountId !== null){ handleLoadRepoData() }
    }, [selectedAccountId, currPage])

    const handleLoadRepoList = async () => {
        const accountsData = await listGitAccounts();
        setAccountList(accountsData);

        if(accountsData.length > 0){ setSelectedAccountId(accountsData[0].id) }
    };

    const handleLoadRepoData = async() => {
        setReposLoading(true);
        setError(null);

        try{
            const repoData = await getMyGithubRepos({gitLinkedAccountId: selectedAccountId, currPage: currPage});
            setLastPage(repoData.lastPage ?? lastPage);
            setRepos(repoData);
        } catch (err: any) {
            const status = err?.response?.status;
            setError(getRepoErrorMessage(status));
            if(!status) { console.error('Error loading repos', err);}
        } finally {
            setReposLoading(false);
        }
    }

    const handleAccountChange = (id: number) => {
        if(selectedAccountId === id) return;
        setCurrPage(firstPage);
        setLastPage(firstPage);
        setSelectedAccountId(id);
    }

    const handleAnalyze = async (
        repo: RepositorySummary,
        llmAnalysisEnabled: boolean,
        byShas?: ByShasRequest,
        byDateRange?: ByDateRangeRequest,
    ) => {
        try {
            if(selectedAccountId == null){ console.error('No git account selected'); return; }

            setIsLoading(true)
            const result = await analyseRepoWithToken(repo.htmlUrl, llmAnalysisEnabled, selectedAccountId, byShas, byDateRange);

            setResult(result);
            setProjectName(getProjectName(repo.htmlUrl));
            setReportId(null)

            router.push('/Info');
        } catch (err: any) {
            console.error('Error analyzing repo:', err);
        } finally {
            setIsLoading(false)
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

    const getRepoErrorMessage = (status?: number): string => {
        switch(status){
            case 401: return 'GitTeller User Authentication Error. Try login in again.';
            case 403: return 'Github Token Invalid.';
            case 404: return 'Git Account or Repository not found.';
            case 429: return 'Github rate-limited. Try again later.';
            case 503: return 'GitHub service unavailable. Try again later.';
            default: return 'Error loading repos. Try again.';
        }
    }

    if (loading) return null;
    if (!isAuthenticated) return <Redirect href="../login"/>;

    return (
        <View style={commonStyles.screen}>
            <LoadingComponent visible={isLoading} />
            <Text style={commonStyles.pageSubtitle}>GitHub Repositories</Text>

            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                <Text style={{ marginRight: 10 }}>ACCOUNT</Text>

                <AccountDropdownMenu
                    accounts={accountList}
                    selectedAccountId={selectedAccountId}
                    onSelect={handleAccountChange}
                />
            </View>

            <View style={commonStyles.reposList}>
                <GithubReposList
                    repos={repos.repositories}
                    loading={reposLoading}
                    error={error}
                    onRetry={handleLoadRepoData}
                    onAnalyzeWithLlm={(repo) => openLlmSettings(repo)}
                    onAnalyzeWithoutLlm={(repo) => handleAnalyze(repo, false)}
                />
            </View>

            <PaginationButton currPage={currPage} lastPage={lastPage} setCurrPage={setCurrPage}></PaginationButton>

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