import { apiFetch } from './client';

export async function listFolders() {
  const res = await apiFetch('/folders');
  return res.items || [];
}

export async function getFolder(id) {
  return apiFetch(`/folders/${id}`);
}

export async function createFolder(name, department, ownerId) {
  return apiFetch('/folders', {
    method: 'POST',
    body: { name, department: department || 'General' },
  });
}

export async function deleteFolder(id) {
  return apiFetch(`/folders/${id}`, { method: 'DELETE' });
}
