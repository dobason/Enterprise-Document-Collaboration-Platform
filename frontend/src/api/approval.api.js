import { mockEngine } from './mock/engine';
import { getDataStore } from './mock/data';

export async function submitForApproval(docId) {
  // Change status from DRAFT to PENDING
  const doc = await mockEngine.get('documents', docId);
  if (!doc) throw new Error('Document not found');
  if (doc.status !== 'DRAFT') throw new Error('Only DRAFT documents can be submitted');

  await mockEngine.update('documents', docId, { status: 'PENDING' });

  // Create approval log
  await mockEngine.create('approvals', {
    documentId: docId,
    action: 'SUBMIT',
    fromStatus: 'DRAFT',
    toStatus: 'PENDING',
    timestamp: new Date().toISOString(),
  });

  return { ...doc, status: 'PENDING' };
}

export async function approveDocument(docId) {
  const doc = await mockEngine.get('documents', docId);
  if (!doc) throw new Error('Document not found');
  if (doc.status !== 'PENDING') throw new Error('Only PENDING documents can be approved');

  await mockEngine.update('documents', docId, { status: 'APPROVED' });

  await mockEngine.create('approvals', {
    documentId: docId,
    action: 'APPROVE',
    fromStatus: 'PENDING',
    toStatus: 'APPROVED',
    timestamp: new Date().toISOString(),
  });

  return { ...doc, status: 'APPROVED' };
}

export async function rejectDocument(docId) {
  const doc = await mockEngine.get('documents', docId);
  if (!doc) throw new Error('Document not found');
  if (doc.status !== 'PENDING') throw new Error('Only PENDING documents can be rejected');

  await mockEngine.update('documents', docId, { status: 'REJECTED' });

  await mockEngine.create('approvals', {
    documentId: docId,
    action: 'REJECT',
    fromStatus: 'PENDING',
    toStatus: 'REJECTED',
    timestamp: new Date().toISOString(),
  });

  return { ...doc, status: 'REJECTED' };
}

export async function getApprovalHistory(docId) {
  const result = await mockEngine.query('approvals', { documentId: docId });
  return result.items;
}
