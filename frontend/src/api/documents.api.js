import { apiFetch } from './client';

export async function listDocuments(params = {}) {
  const qs = new URLSearchParams();

  ['page', 'limit', 'sortBy', 'sortOrder', 'folderId'].forEach((key) => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      qs.set(key, params[key]);
    }
  });

  const query = qs.toString();
  return apiFetch(`/documents${query ? `?${query}` : ''}`);
}

export async function getDocument(id) {
  try {
    return await apiFetch(`/documents/${id}`);
  } catch (err) {
    if (err.message.includes('404') || err.message.toLowerCase().includes('not found')) {
      throw new Error(`Document ${id} not found`);
    }
    throw err;
  }
}

export async function deleteDocument(id) {
  return apiFetch(`/documents/${id}`, { method: 'DELETE' });
}

export async function createDocument(data) {
  return apiFetch('/documents', {
    method: 'POST',
    body: {
      title: data.title,
      type: data.type,
      folderId: data.folderId,
      content: data.content,
    },
  });
}

export async function updateDocument(id, data) {
  const body = {};
  ['title', 'content', 'folderId'].forEach((key) => {
    if (data[key] !== undefined && data[key] !== null) {
      body[key] = data[key];
    }
  });

  return apiFetch(`/documents/${id}`, { method: 'PATCH', body });
}
