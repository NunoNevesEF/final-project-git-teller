import { useState } from 'react';
import { useRouter } from 'expo-router';
import RepositorySearchForm, { LlmFilterType, PromptComplexity, AnalysisMode, AnalysisType } from '@/components/RepositorySearchForm';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';


export default function RepositorySearch() {
  const router = useRouter();
  const setInput = useAnalysisStore((state) => state.setInput);
  const setResult = useAnalysisStore((state) => state.setResult);

  const [searchType, setSearchType] = useState<'url' | 'project'>('url');
  const [platform, setPlatform] = useState<'github' | 'gitlab'>('github');
  const [text, setText] = useState('');
  const [projectName, setProjectName] = useState('');
  const [usernameRepo, setUsernameRepo] = useState('');
  const [llmAnalysisEnabled, setLlmAnalysisEnabled] = useState(false);
  const [requestedAnalyses, setRequestedAnalyses] = useState<AnalysisType[]>(['DEFAULT']);

  const [llmFilterType, setLlmFilterType] = useState<LlmFilterType>('dateRange');
  const [commitShas, setCommitShas] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
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
    setInput({
      repositoryUrl: url,
      repositoryName: projectName,
      repositoryOwner: usernameRepo,
      platform,
      llmAnalysisEnabled,
    });

    router.push('/Info');
  };

  return (
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
  );
}