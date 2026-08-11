import { apiFetch } from './client';

export async function getOCRResult(docId) {
  return apiFetch(`/documents/${docId}/ocr`);
}

export async function requestOCR(docId) {
  return apiFetch(`/documents/${docId}/ocr`, { method: 'POST' });
}
