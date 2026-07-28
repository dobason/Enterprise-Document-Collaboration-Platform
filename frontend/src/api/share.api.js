import { mockEngine } from './mock/engine';

const shareLinks = {};

export async function shareDocument(docId, email, ttlHours = 24) {
  const linkId = `share_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
  const fakeLink = `https://edms.app/share/${linkId}`;

  const shareRecord = {
    id: linkId,
    documentId: docId,
    sharedWithEmail: email,
    expiresAt: new Date(Date.now() + ttlHours * 3600000).toISOString(),
    createdAt: new Date().toISOString(),
    link: fakeLink,
  };

  shareLinks[linkId] = shareRecord;

  // Also save to mock engine
  await mockEngine.create('shares', shareRecord);

  return shareRecord;
}

export async function getShareLink(docId) {
  const result = await mockEngine.query('shares', { documentId: docId });
  return result.items[0] || null;
}

export async function listShares(docId) {
  const result = await mockEngine.query('shares', { documentId: docId });
  return result.items;
}
