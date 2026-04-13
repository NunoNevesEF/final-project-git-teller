import CustomTextInput from '@/components/textInput';
import { useState } from 'react';
import {View, Button, Pressable, Text} from 'react-native';
import { analyzeRepo } from '../services/GitCommunicationService';
import {Link, useRouter} from 'expo-router';
import { useAnalysisStore } from '@/store/useAnalysisStore';
import RepositorySearchForm from '@/components/RepositorySearchForm';
/**
 * Non Authenticated first page that user interacts with
 * @returns
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
        if (searchType === 'url') {
            return text;
        } else {
            const baseUrl = platform === 'github' ? 'https://github.com' : 'https://gitlab.com';
            return `${baseUrl}/${username}/${projectName}`;
        }
    };

    const handleSubmit = async () => {
        const input = searchType === 'url' ? text : `${username}/${projectName}`;

        if (input.length > 0) {
            try {
                const url = buildUrl();
                const result = await analyzeRepo(url);
                setResult(result);
                router.push("/Info");
            } catch (error) {
                console.error("An error has occurred: ", error);
            }
        }
    };


    return (
        <View style={{ flex: 1 }}>
            <Link href="/auth" asChild>
                <Pressable
                    style={{
                        position: 'absolute',
                        top: 12,
                        left: 12,
                        zIndex: 10,
                        paddingVertical: 8,
                        paddingHorizontal: 12,
                        borderRadius: 8,
                        backgroundColor: '#1f2937',
                    }}
                >
                    <Text style={{ color: '#fff', fontWeight: '600' }}>
                        LogIn / SignUp
                    </Text>
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

