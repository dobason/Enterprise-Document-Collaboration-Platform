import { apiFetch, getStoredUser, getToken } from './client';
import { CONFIG } from './config';

const uploadCache = {};

function putFileWithProgress(url, file, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open('PUT', url);

    // xhr.setRequestHeader('Authorization', `Bearer ${getToken()}`); 

    xhr.setRequestHeader('Content-Type', file.type || 'application/octet-stream');
    
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable && onProgress) {
        onProgress(Math.round((e.loaded / e.total) * 100));
      }
    };
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) resolve();
      else reject(new Error(`Upload failed: HTTP ${xhr.status}`));
    };
    xhr.onerror = () => reject(new Error('Upload failed: network error'));
    xhr.send(file);
  });
}

export async function getUploadUrl(fileName, fileType) {
  const res = await apiFetch('/upload/url', {
    method: 'POST',
    body: { fileName, fileType },
  });

  uploadCache[res.fileId] = { fileName, fileType };

  return { url: res.url, fileId: res.fileId, fields: res.fields || {} };
}

export async function confirmUpload(fileId) {
  const cached = uploadCache[fileId] || {};
  const user = getStoredUser();

  return apiFetch('/upload/confirm', {
    method: 'POST',
    body: {
      fileId,
      fileName: cached.fileName || 'uploaded-file',
      fileType: cached.fileType || 'application/octet-stream',
      ownerId: user?.id || 'u1',
    },
  });
}

export async function uploadFile(file, onProgress) {
  const { url, fileId } = await getUploadUrl(file.name, file.type);
  await putFileWithProgress(url, file, onProgress);
  return confirmUpload(fileId);
}

export function getFileUrl(s3Key) {
  if (!s3Key) return '#';
  return `${CONFIG.CLOUDFRONT_URL}/${s3Key}`;
}