import { useState } from 'react';
import { View, Pressable, Text } from 'react-native';
import { analyzeRepo } from '@/services/GitCommunicationService';
import { Link, useRouter } from 'expo-router';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import RepositorySearchForm from '@/components/RepositorySearchForm';
import { commonStyles } from '@/constants/commonStyles';

/**
 * Non Authenticated first page that user interacts with
 */
export default function Index() {
    const router = useRouter();
    const [searchType, setSearchType] = useState<'url' | 'project'>('url');
    const [platform, setPlatform] = useState<'github' | 'gitlab'>('github');
    const [text, setText] = useState('https://github.com/NunoNevesEF/final-project-git-teller');
    const [projectName, setProjectName] = useState('final-project-git-teller');
    const [username, setUsername] = useState('NunoNevesEF');
    const setResult = useAnalysisStore((state) => state.setResult);

    const buildUrl = (): string => {
        if (searchType === 'url') return text;
        const baseUrl = platform === 'github' ? 'https://github.com' : 'https://gitlab.com';
        return `${baseUrl}/${username}/${projectName}`;
    };

    const handleSubmit = async () => {
        const input = searchType === 'url' ? text : `${username}/${projectName}`;
        if (!input.length) return;

        try {
            const url = buildUrl();
            const result = await analyzeRepo(url);
            setResult(result);
            router.push('/Info');
        } catch (error) {
            console.error('An error has occurred: ', error);
        }
    };

    return (
        <View style={commonStyles.root}>
            <Link href="/login" asChild>
                <Pressable style={commonStyles.topLeftActionButton}>
                    <Text style={commonStyles.topLeftActionButtonText}>Log In</Text>
                </Pressable>
            </Link>

            <RepositorySearchForm
                searchType={searchType}
                onSearchTypeChange={setSearchType}
                platform={platform}
                onPlatformChange={setPlatform}
                text={text}
                onTextChange={setText}
                projectName={projectName}
                onProjectNameChange={setProjectName}
                username={username}
                onUsernameChange={setUsername}
                onSubmit={handleSubmit}
            />
        </View>
    );
}
