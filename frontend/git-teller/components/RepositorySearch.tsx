import { useState } from 'react';
import { useRouter } from 'expo-router';
import RepositorySearchForm, { LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType } from '@/components/RepositorySearchForm';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { useAnalysisInfoStore } from '@/store/useAnalysisInfoStore';
import LoadingComponent from './LoadingComponent';


export default function RepositorySearch() {
  const router = useRouter();
  const setResult = useAnalysisInfoStore((state) => state.setResult);
  const setProjectNameStore = useAnalysisInfoStore((s) => s.setProjectName);
  const setReportId = useAnalysisInfoStore((state) => state.setReportId);

  const [searchType, setSearchType] = useState<'url' | 'project'>('url');
  const [platform, setPlatform] = useState<'github' | 'gitlab'>('github');
  const [text, setText] = useState('https://github.com/NunoNevesEF/final-project-git-teller');
  const [projectName, setProjectName] = useState('final-project-git-teller');
  const [usernameRepo, setUsernameRepo] = useState('NunoNevesEF');
  const [loading, setLoading] = useState(false);
  const [llmAnalysisEnabled, setLlmAnalysisEnabled] = useState(false);
  const [requestedAnalyses, setRequestedAnalyses] = useState<AnalysisType[]>(['DEFAULT']);

  const [llmFilterType, setLlmFilterType] = useState<LlmFilterType>('dateRange');
  const [commitShas, setCommitShas] = useState('');
  const [fromDate, setFromDate] = useState('2026-05-15');
  const [toDate, setToDate] = useState('2026-05-16');
  const [promptComplexity, setPromptComplexity] = useState<PromptComplexity>('SIMPLE');
  const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('DIFF');

  const buildUrl = () => {
    if (searchType === 'url') return text;
    const base = platform === 'github' ? 'https://github.com' : 'https://gitlab.com';
    return `${base}/${usernameRepo}/${projectName}`;
  };

  const handleSubmit = async () => {
    const url = buildUrl();
    if (!url) return;

    try {
      setLoading(true);

        const byShas =
            llmAnalysisEnabled && llmFilterType === 'shas' && commitShas
                ? {
                    repoURI: url,
                    commitShas: commitShas.split(',').map((s) => s.trim()).filter(Boolean),
                    promptComplexity,
                    analysisMode,
                    requestedAnalyses,
                }
                : undefined;

        const byDateRange =
            llmAnalysisEnabled && llmFilterType === 'dateRange' && fromDate && toDate
                ? {
                    repoURI: url,
                    fromDate: `${fromDate}T00:00:00Z`,
                    toDate: `${toDate}T23:59:59Z`,
                    promptComplexity,
                    analysisMode,
                    requestedAnalyses,
                }
                : undefined;

        const result = await analyzeRepo(url, llmAnalysisEnabled, byShas, byDateRange);



        setResult(result);
        setProjectNameStore(projectName);
        setReportId(null);
        /*
            setInput({
      repositoryUrl: url,
      repositoryName: projectName,
      repositoryOwner: usernameRepo,
      platform,
      llmAnalysisEnabled,
    });
         */


      router.push('/Info');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <RepositorySearchForm
        searchType={searchType}
        onSearchTypeChange={setSearchType}
        platform={platform}
        onPlatformChange={setPlatform}
        text={text}
        onTextChange={setText}
        projectName={projectName}
        onProjectNameChange={setProjectName}
        username={usernameRepo}
        onUsernameChange={setUsernameRepo}
        llmAnalysisEnabled={llmAnalysisEnabled}
        onLlmAnalysisEnabledChange={setLlmAnalysisEnabled}
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
        onSubmit={handleSubmit}
        requestedAnalyses={requestedAnalyses}
        onRequestedAnalysesChange={setRequestedAnalyses}
      />

      <LoadingComponent visible={loading} />
    </>
  );
}