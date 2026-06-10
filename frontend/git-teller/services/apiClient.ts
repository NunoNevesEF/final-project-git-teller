import Constants from "expo-constants";
import {getTokens} from "@/services/secureStore";

const debuggerHost = Constants.expoConfig?.hostUri;
const host = debuggerHost?.split(":")[0] ?? "localhost";

export const API_BASE_URL = `http://${host}:8080/api`;

export async function apiGet(path: string, token?: string | null) {
  const res = await fetch(`${API_BASE_URL}/${path}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (!res.ok) {
    throw new Error(`GET ${path} falhou com status ${res.status}`);
  }

  return res.json();
}

export async function apiGetBlob(path: string, token?: string | null) {
  const res = await fetch(`${API_BASE_URL}/${path}`, {
    method: "GET",
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (!res.ok) {
    throw new Error(`GET ${path} falhou com status ${res.status}`);
  }

  return res.blob();
}

export async function apiPost(path: string, body: object) {
  const res = await fetch(`${API_BASE_URL}/${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(`POST ${path} falhou com status ${res.status}`);
  }
  return res.json();
}

export async function apiPostBlob(
  path: string,
  body: unknown,
  token?: string | null,
) {
  const res = await fetch(`${API_BASE_URL}/${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error(`POST ${path} falhou com status ${res.status}`);
  }

  return res.blob();
}

export async function apiGetAuthenticated(path: string){
    const { accessToken } = await getTokens()

    if(accessToken == null) throw new Error(`access token must exist for authenticated operation`)

    const res = await fetch(`${API_BASE_URL}/${path}`,{
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`
        }
    });

    if(!res.ok){
        throw new Error(`GET ${path} falhou com status ${res.status}`)
    }

    return res.json()
}

export async function apiPostAuthenticated(path: string, body: object){
    const { accessToken } = await getTokens();

    if(accessToken == null) throw new Error(`access token must exist for authenticated operation`)

    const res = await fetch(`${API_BASE_URL}/${path}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`
        },
        body: JSON.stringify(body)
    });

    if(!res.ok){
        throw new Error(`POST ${path} falhou com status ${res.status}`);
    }

    return res.json()
}