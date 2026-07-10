import apiClient from "./authApiClient";

type GetMyGithubReposProps = {
  gitLinkedAccountId: number | null;
  currPage: number;
};

export type UserRepositoriesDto = {
  lastPage: number | null;
  repositories: RepositorySummary[];
};

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

export async function getMyGithubRepos({
  gitLinkedAccountId,
  currPage,
}: GetMyGithubReposProps): Promise<UserRepositoriesDto> {
  const resp = await apiClient.get<UserRepositoriesDto>(
    `/api/git-provider/repos/${gitLinkedAccountId}/${currPage}`,
  );
  return resp.data;
}
