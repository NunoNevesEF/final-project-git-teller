import { useState } from 'react';
import { View, Text, Pressable } from 'react-native';
import { Redirect, useRouter } from 'expo-router';
import { useAuth } from '@/store/AuthProvider';
import RepositorySearchForm from '@/components/RepositorySearchForm';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { useCommonStyles } from '@/constants/useCommonStyles';


export default function HomePage() {
    const { isAuthenticated, username, loading, signOut } = useAuth();
    const setInput = useAnalysisStore((state) => state.setInput);
    const router = useRouter();
    const [searchType, setSearchType] = useState<'url' | 'project'>('url');
    const [platform, setPlatform] = useState<'github' | 'gitlab'>('github');
    const [text, setText] = useState('https://github.com/NunoNevesEF/final-project-git-teller');
    const [projectName, setProjectName] = useState('final-project-git-teller');
    const [usernameRepo, setUsernameRepo] = useState('NunoNevesEF');
    const setResult = useAnalysisStore((state) => state.setResult);
    const commonStyles = useCommonStyles();



    const buildUrl = (): string => {
        if (searchType === 'url') {
            return text;
        }
        const baseUrl = platform === 'github' ? 'https://github.com' : 'https://gitlab.com';
        return `${baseUrl}/${usernameRepo}/${projectName}`;
    };

    const handleSubmit = async () => {
        const input = searchType === 'url' ? text : `${usernameRepo}/${projectName}`;
        if (input.length > 0) {
            try {
                const url = buildUrl();
                const result = await analyzeRepo(url);
                setResult(result);
                setInput({
                    repositoryUrl: url,
                    repositoryName: projectName,
                    repositoryOwner: usernameRepo,
                    platform,
                });
                router.push('/Info');
            } catch (error) {
                console.error('An error has occurred: ', error);
            }
        }
    };

    if (loading) return null;
    if (!isAuthenticated) return <Redirect href="/login" />;


    return (
        <View style={[commonStyles.screen, commonStyles.centered]}>
            <Text style={commonStyles.pageTitle}>Hello User {username ?? 'User'}</Text>
            <Text style={commonStyles.pageSubtitle}>Search a repository</Text>

            <Pressable
                style={[commonStyles.primaryButton, commonStyles.fullWidth, { marginTop: 16 }]}
                onPress={() => router.push('/github-repos')}
            >
                <Text style={commonStyles.primaryButtonText}>GitHub Repos</Text>
            </Pressable>
            <Pressable
                style={[commonStyles.primaryButton, commonStyles.fullWidth, { marginTop: 16 }]}
                onPress={() => router.push('/user-reports')}
            >
                <Text style={commonStyles.primaryButtonText}>My reports</Text>
            </Pressable>

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


        </View>
    );
}
