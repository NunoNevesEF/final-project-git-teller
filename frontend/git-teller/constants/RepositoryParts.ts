export interface RepositoryParts {
    provider: string;
    author: string;
    name: string;
}

export function getRepositoryParts(repoUrl: string): RepositoryParts {
    const url = new URL(repoUrl);

    const [author, name] = url.pathname.slice(1).split("/");

    return {
        provider: url.hostname.replace("www.", "").split(".")[0],
        author,
        name,
    };
}