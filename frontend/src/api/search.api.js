import { apiFetch } from './client';

// Backend /search hỗ trợ: q, type, status.
// Tham số `tag` không được backend hỗ trợ nên được bỏ qua.
export async function searchDocuments({ q, tag, type, status } = {}) {
  const qs = new URLSearchParams();

  if (q) qs.set('q', q);
  if (type) qs.set('type', type);
  if (status) qs.set('status', status);

  const query = qs.toString();
  return apiFetch(`/search${query ? `?${query}` : ''}`);
}
