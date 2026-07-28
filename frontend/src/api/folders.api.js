import { mockEngine } from './mock/engine';

export async function listFolders() {
  const result = await mockEngine.query('folders');
  return result.items;
}

export async function getFolder(id) {
  return mockEngine.get('folders', id);
}

export async function createFolder(name, department, ownerId) {
  return mockEngine.create('folders', {
    name,
    department: department || 'General',
    ownerId: ownerId || 'u1',
    createdAt: new Date().toISOString(),
  });
}

export async function deleteFolder(id) {
  return mockEngine.delete('folders', id);
}
