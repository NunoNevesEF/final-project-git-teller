import { useState } from 'react';
import { useRouter } from 'expo-router';
import RepositorySearchForm from '@/components/RepositorySearchForm';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { useAnalysisStore } from '@/store/useAnalysisStore';

export default function RepositorySearch() {
  const router = useRouter();
  const setInput = useAnalysisStore((state) => state.setInput);
  const setResult = useAnalysisStore((state) => state.setResult);

  const [searchType, setSearchType] = useState<'url' | 'project'>('url');
  const [platform, setPlatform] = useState<'github' | 'gitlab'>('github');
  const [text, setText] = useState('https://github.com/NunoNevesEF/final-project-git-teller');
  const [projectName, setProjectName] = useState('final-project-git-teller');
  const [usernameRepo, setUsernameRepo] = useState('NunoNevesEF');

  const buildUrl = () => {
    if (searchType === 'url') return text;

    const base =
      platform === 'github'
        ? 'https://github.com'
        : 'https://gitlab.com';

    return `${base}/${usernameRepo}/${projectName}`;
  };

  const handleSubmit = async () => {
    const url = buildUrl();
    if (!url) return;

    const result = await analyzeRepo(url);

    setResult(result);
    setInput({
      repositoryUrl: url,
      repositoryName: projectName,
      repositoryOwner: usernameRepo,
      platform,
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
      onSubmit={handleSubmit}
    />
  );
}