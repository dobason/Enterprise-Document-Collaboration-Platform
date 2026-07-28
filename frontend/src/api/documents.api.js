import { mockEngine } from './mock/engine';

export async function listDocuments(params = {}) {
  return mockEngine.query('documents', params);
}

export async function getDocument(id) {
  const doc = await mockEngine.get('documents', id);
  if (!doc) throw new Error(`Document ${id} not found`);
  return doc;
}

export async function deleteDocument(id) {
  return mockEngine.delete('documents', id);
}

export async function createDocument(data) {
  return mockEngine.create('documents', {
    ...data,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    status: data.status || 'DRAFT',
  });
}

export async function updateDocument(id, data) {
  return mockEngine.update('documents', id, {
    ...data,
    updatedAt: new Date().toISOString(),
  });
}
