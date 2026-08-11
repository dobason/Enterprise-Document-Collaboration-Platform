import { apiFetch } from './client';

export async function getVersions(docId) {
  const res = await apiFetch(`/documents/${docId}/versions`);
  return res.items || [];
}

export async function createVersion(docId, content, userId) {
  // Backend lấy createdBy từ user đang đăng nhập (bỏ qua userId)
  return apiFetch(`/documents/${docId}/versions`, {
    method: 'POST',
    body: { content },
  });
}

export async function rollbackVersion(docId, versionId) {
  return apiFetch(`/documents/${docId}/versions/rollback`, {
    method: 'POST',
    body: { versionId },
  });
}
