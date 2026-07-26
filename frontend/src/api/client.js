// Helper gọi API kèm JWT token (P1 hoàn thiện: lấy token thật từ Cognito session sau khi login)
import { CONFIG } from "./config";

export async function apiFetch(path, { method = "GET", token, body } = {}) {
  const res = await fetch(`${CONFIG.API_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `API error ${res.status}`);
  }
  return res.json();
}

// TODO (P1): viết các hàm cụ thể — loginUser(), getUploadUrl(), listDocuments(), searchDocuments(),
// createFolder(), shareDocument(), submitForApproval(), getDashboardStats()
