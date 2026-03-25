export const API_BASE_URL = "http://localhost:8080/api";

export async function apiGet(path: string) {
  const res = await fetch(`${API_BASE_URL}/${path}`);
  if (!res.ok) {
    throw new Error(`GET ${path} falhou com status ${res.status}`);
  }
  return res.json();
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

export async function apiPostBlob(path: string, body: object) {
  const res = await fetch(`${API_BASE_URL}/${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(`POST ${path} falhou com status ${res.status}`);
  }
  return res.blob();
}
