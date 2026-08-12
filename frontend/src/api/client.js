// HTTP client dùng chung cho toàn bộ API layer.
// Tự động đính kèm JWT token từ localStorage (key: edms_token) vào mọi request.
import { CONFIG } from "./config";

export const API_BASE_URL = CONFIG.API_URL;

export function getToken() {
  return localStorage.getItem("edms_token");
}

export function getStoredUser() {
  try {
    const raw = localStorage.getItem("edms_user");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export async function apiFetch(path, { method = "GET", token, body } = {}) {
  const authToken = token || getToken();

  const res = await fetch(`${CONFIG.API_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    // Parse lỗi theo chuẩn backend: { "error": "..." } hoặc { "message": "..." }
    let message = `API error ${res.status}`;
    try {
      const err = await res.json();
      message = err?.error || err?.message || message;
    } catch {
      // body rỗng hoặc không phải JSON
    }
    if (res.status === 401) {
      // Token hết hạn / không hợp lệ -> xóa phiên
      localStorage.removeItem("edms_token");
      localStorage.removeItem("edms_user");
    }
    throw new Error(message);
  }

  // 204 No Content (DELETE...) hoặc body rỗng -> trả null
  if (res.status === 204) return null;

  const text = await res.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}
