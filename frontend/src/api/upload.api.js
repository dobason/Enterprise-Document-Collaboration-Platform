import { mockEngine } from './mock/engine';
import { getDataStore } from './mock/data';

const fakeUploads = {};

export async function getUploadUrl(fileName, fileType) {
  const fileId = `file_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
  const fakeUrl = `https://mock-upload.edms.local/${fileId}/${encodeURIComponent(fileName)}`;

  fakeUploads[fileId] = {
    fileId,
    fileName,
    fileType,
    status: 'pending',
    uploadedAt: new Date().toISOString(),
  };

  return { url: fakeUrl, fileId, fields: {} };
}

export async function confirmUpload(fileId) {
  const upload = fakeUploads[fileId];
  if (!upload) throw new Error('Upload not found');

  upload.status = 'completed';

  // Create document entry in mock data
  const doc = await mockEngine.create('documents', {
    title: upload.fileName.replace(/\.[^/.]+$/, ''),
    type: upload.fileType.toUpperCase(),
    fileName: upload.fileName,
    ownerId: 'u1',
    status: 'DRAFT',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  });

  return doc;
}

export async function uploadFile(file) {
  // Simplified mock upload - simulate the full flow
  const { fileId } = await getUploadUrl(file.name, file.type);
  await new Promise((resolve) => setTimeout(resolve, 1500));
  return confirmUpload(fileId);
}
