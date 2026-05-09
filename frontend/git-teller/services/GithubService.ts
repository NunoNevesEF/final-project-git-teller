import authApiClient from './authApiClient';

export type RepositorySummary = {
    id: number;
    name: string;
    fullName: string;
    htmlUrl: string;
    description?: string | null;
    private: boolean;
    language?: string | null;
    starsCount: number;
    forksCount: number;
    updatedAt: string;
};

export async function getMyGithubRepos(): Promise<RepositorySummary[]> {
    const resp = await authApiClient.get<RepositorySummary[]>('/api/github/repos');
    return resp.data;
}