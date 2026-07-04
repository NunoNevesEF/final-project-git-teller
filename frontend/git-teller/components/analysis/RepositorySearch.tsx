import { useEffect, useState } from 'react';
import { useRouter } from 'expo-router';
import RepositorySearchForm, { LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType } from '@/components/analysis/RepositorySearchForm';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { useAnalysisInfoStore } from '@/store/useAnalysisInfoStore';
import LoadingComponent from '../utils/LoadingComponent';


export default function RepositorySearch() {
  const router = useRouter();
  const gitAccountId = useAnalysisInfoStore((s) => s.gitAccountId);
  const setGitAccountId = useAnalysisInfoStore((s) => s.setGitAccountId);
  const repoURI = useAnalysisInfoStore((s) => s.repoURI);
  const setRepoURI = useAnalysisInfoStore((s) => s.setRepoURI);
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

  const [llmFilterType, setLlmFilterType] = useState<LlmFilterType>('detailedSettings');
  const [commitShas, setCommitShas] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [promptComplexity, setPromptComplexity] = useState<PromptComplexity>('SIMPLE');
  const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('DIFF');

  useEffect(() => {
    if (repoURI) {
      setText(repoURI);
    }
  }, [repoURI]);

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
                    commitShas: commitShas.split(',').map((s) => s.trim()).filter(Boolean),
                    promptComplexity,
                    analysisMode,
                    requestedAnalyses,
                }
                : undefined;

        const byDetailedSettings =
            llmAnalysisEnabled && llmFilterType === 'detailedSettings'
                ? {
                    promptComplexity,
                    analysisMode,
                    requestedAnalyses,
                }
                : undefined;
        
        const dateFilter =
          fromDate && toDate
            ? {
                beginDate: `${fromDate}T00:00:00Z`,
                endDate: `${toDate}T23:59:59Z`,
              }
            : null;

        const request = {
          repoURI: url,
          gitAccountId: gitAccountId,
          dateFilter: dateFilter,
          llmRequest: llmAnalysisEnabled
            ? {
                flag: true,
                byShas: llmFilterType === 'shas' ? byShas : null,
                byDetailedSettings: llmFilterType === 'detailedSettings' ? byDetailedSettings : null,
              }
            : null,
        };

        setRepoURI("");
        setGitAccountId(null);
        
        const result = await analyzeRepo(request);


        setResult(result);
        setProjectNameStore(projectName);
        setReportId(null);

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