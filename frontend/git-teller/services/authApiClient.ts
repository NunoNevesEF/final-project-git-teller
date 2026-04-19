import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { clearTokens, getTokens, saveTokens } from './secureStore';

const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080';

type RefreshResponse = {
    accessToken: string;
    refreshToken?: string;
    expiresAt?: number;
};

type RetryConfig = AxiosRequestConfig & { _retry?: boolean };

type FailedQueueItem = {
    resolve: (token: string) => void;
    reject: (error: unknown) => void;
};

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

let isRefreshing = false;
let failedQueue: FailedQueueItem[] = [];

/**
 * Adds or updates the Bearer token in the request headers.
 */
function setAuthorizationHeader(config: AxiosRequestConfig, token: string) {
    config.headers = config.headers || {};
    (config.headers as Record<string, string>).Authorization = `Bearer ${token}`;
}

/**
 * Resolves/rejects all requests that were waiting for token refresh.
 */
function processQueue(error: unknown, token?: string) {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else if (token) resolve(token);
    });
    failedQueue = [];
}


/**
 * Calls backend refresh endpoint and returns fresh auth tokens.
 */
async function requestTokenRefresh(refreshTokenValue: string): Promise<RefreshResponse> {
    const response = await axios.post<RefreshResponse>(
        `${API_BASE_URL}/api/public/auth/refresh-token`,
        null, // no JSON body
        {
            params: { refreshToken: refreshTokenValue }, // query param for @RequestParam
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true,
        }
    );
    return response.data;
}



// Attach access token automatically
apiClient.interceptors.request.use(
    async (config) => {
        const { accessToken } = await getTokens();
        if (accessToken) {
            setAuthorizationHeader(config, accessToken);
        }
        return config;
    },
    (error) => Promise.reject(error)
);


// Interceta respostas com erro: se vier 401 (token expirado), tenta refrescar o access token uma única vez.
// Enquanto um refresh está em curso, os outros pedidos 401 ficam em fila e são retomados com o novo token.
// Se o refresh falhar, limpa os tokens e propaga o erro (sessão inválida / voltar ao login).
apiClient.interceptors.response.use(
    (response: AxiosResponse) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as RetryConfig | undefined;

        if (!originalRequest || error.response?.status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                failedQueue.push({
                    resolve: (token: string) => {
                        setAuthorizationHeader(originalRequest, token);
                        resolve(apiClient(originalRequest));
                    },
                    reject,
                });
            });
        }

        isRefreshing = true;

        try {
            const { refreshToken } = await getTokens();
            if (!refreshToken) throw new Error('No refresh token available');

            console.log("Refreshing token...");

            const refreshed = await requestTokenRefresh(refreshToken);
            const newAccessToken = refreshed.accessToken;
            const newRefreshToken = refreshed.refreshToken ?? refreshToken;

            await saveTokens({
                accessToken: newAccessToken,
                refreshToken: newRefreshToken,
                expiresAt: refreshed.expiresAt,
            });

            processQueue(null, newAccessToken);

            setAuthorizationHeader(originalRequest, newAccessToken);
            return apiClient(originalRequest);
        } catch (refreshError) {
            processQueue(refreshError);
            await clearTokens();
            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    }
);

const authApiClient = {
    client: apiClient,
    get: <T = unknown>(url: string, config?: AxiosRequestConfig) => apiClient.get<T>(url, config),
    post: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
        apiClient.post<T>(url, data, config),
    put: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
        apiClient.put<T>(url, data, config),
    delete: <T = unknown>(url: string, config?: AxiosRequestConfig) => apiClient.delete<T>(url, config),
    refreshToken: requestTokenRefresh,
};

export default authApiClient;


