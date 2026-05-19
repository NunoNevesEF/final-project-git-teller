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

export type GitHubInstallationCandidate = {
    installationId: number;
    accountLogin?: string | null;
    repositorySelection?: string | null;
    appSlug?: string | null;
};

export type GitHubInstallationsState = {
    installUrl: string;
    discoveredInstallations: GitHubInstallationCandidate[];
    linkedInstallationIds: number[];
    message?: string | null;
};

export async function getMyGithubRepos(): Promise<RepositorySummary[]> {
    const resp = await authApiClient.get<RepositorySummary[]>('/api/github/repos');
    return resp.data;
}

/** New: fetch install state (install URL + discovered installations + linked ids) */
export async function getGithubAppInstallations(): Promise<GitHubInstallationsState> {
    const resp = await authApiClient.get<GitHubInstallationsState>('/api/github/app/installations');
    return resp.data;
}

/** New: link an installation (installationId) for the current authenticated user */
export async function linkGithubAppInstallation(installationId: number): Promise<{ linkedInstallationId: number }> {
    const resp = await authApiClient.post<{ linkedInstallationId: number }>('/api/github/app/installations/link', {
        installationId
    });
    return resp.data;
}