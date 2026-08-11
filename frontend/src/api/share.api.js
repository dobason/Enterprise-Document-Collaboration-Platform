import { apiFetch } from './client';

export async function shareDocument(docId, email, ttlHours = 24) {
  return apiFetch(`/documents/${docId}/share`, {
    method: 'POST',
    body: { email, ttlHours },
  });
}

export async function getShareLink(docId) {
  try {
    return await apiFetch(`/documents/${docId}/share`);
  } catch {
    return null;
  }
}

export async function listShares(docId) {
  const res = await apiFetch(`/documents/${docId}/shares`);
  return res.items || [];
}
