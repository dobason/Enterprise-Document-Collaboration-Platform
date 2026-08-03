import { apiFetch } from './client';

// LƯU Ý: các endpoint approval nằm ở /approval/* (không phải /documents/{id}/approval/*)
// và nhận body { documentId }. Sau mỗi hành động, refetch document đầy đủ để
// ApprovalPage có thể setDoc(result) với toàn bộ thông tin.

async function runApprovalAction(path, docId, extra = {}) {
  await apiFetch(path, {
    method: 'POST',
    body: { documentId: docId, ...extra },
  });
  return apiFetch(`/documents/${docId}`);
}

export async function submitForApproval(docId) {
  return runApprovalAction('/approval/submit', docId);
}

export async function approveDocument(docId) {
  return runApprovalAction('/approval/approve', docId);
}

export async function rejectDocument(docId) {
  return runApprovalAction('/approval/reject', docId, { reason: 'No reason specified' });
}

export async function getApprovalHistory(docId) {
  const res = await apiFetch(`/approval/history?documentId=${encodeURIComponent(docId)}`);
  return res.items || [];
}
