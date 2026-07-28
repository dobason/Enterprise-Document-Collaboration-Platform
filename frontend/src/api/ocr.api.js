import { getDataStore } from './mock/data';

export async function getOCRResult(docId) {
  const store = getDataStore();
  const ocr = store.ocrResults?.find((o) => o.documentId === docId);

  if (!ocr) {
    // Simulate processing for documents without OCR
    return {
      status: 'processing',
      text: null,
    };
  }

  return {
    status: ocr.status,
    text: ocr.text,
    extractedAt: ocr.extractedAt || new Date().toISOString(),
  };
}

export async function requestOCR(docId) {
  const store = getDataStore();

  // Check if OCR already exists
  const existing = store.ocrResults?.find((o) => o.documentId === docId);
  if (existing) {
    return { status: 'completed', text: existing.text };
  }

  // Simulate OCR processing delay
  await new Promise((resolve) => setTimeout(resolve, 2000));

  // Add mock OCR result
  const mockOcr = {
    id: `ocr_${Date.now()}`,
    documentId: docId,
    status: 'completed',
    text: `OCR Extracted Text for document ${docId}\n\nThis is simulated OCR output.\n\n[Processed at ${new Date().toLocaleString()}]\n\nThe quick brown fox jumps over the lazy dog.\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit.\nSed do eiusmod tempor incididunt ut labore et dolore magna aliqua.`,
    extractedAt: new Date().toISOString(),
  };

  if (!store.ocrResults) store.ocrResults = [];
  store.ocrResults.push(mockOcr);

  return { status: 'completed', text: mockOcr.text };
}
