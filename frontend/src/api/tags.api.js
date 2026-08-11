import { apiFetch } from './client';

export async function getTags(docId) {
  const res = await apiFetch(`/documents/${docId}/tags`);
  return res.items || [];
}

export async function addTag(docId, tagName) {
  return apiFetch(`/documents/${docId}/tags`, {
    method: 'POST',
    body: { name: tagName },
  });
}

export async function removeTag(docId, docTagId) {
  return apiFetch(`/documents/${docId}/tags/${docTagId}`, { method: 'DELETE' });
}

export async function getAllTags() {
  const res = await apiFetch('/tags');
  return res.items || [];
}
