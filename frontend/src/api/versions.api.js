import { mockEngine } from './mock/engine';

export async function getVersions(docId) {
  // Get versions for a document, sorted by versionNumber desc
  const result = await mockEngine.query('versions', { documentId: docId, sortBy: 'versionNumber', sortOrder: 'desc' });
  return result.items;
}

export async function createVersion(docId, content, userId) {
  const versions = await getVersions(docId);
  const maxVersion = versions.length > 0 ? Math.max(...versions.map(v => v.versionNumber)) : 0;

  return mockEngine.create('versions', {
    documentId: docId,
    versionNumber: maxVersion + 1,
    content: typeof content === 'string' ? content : JSON.stringify(content),
    createdBy: userId,
    createdAt: new Date().toISOString(),
  });
}

export async function rollbackVersion(docId, versionId) {
  const version = await mockEngine.get('versions', versionId);
  if (!version) throw new Error('Version not found');

  // Create a new version with the rolled-back content
  return createVersion(docId, version.content, version.createdBy);
}

export async function getVersion(id) {
  return mockEngine.get('versions', id);
}
