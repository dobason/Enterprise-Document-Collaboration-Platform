import { mockEngine } from './mock/engine';

export async function searchDocuments({ q, tag, type, status } = {}) {
  const criteria = {};

  if (q) criteria.q = q;
  if (tag) criteria.tags = Array.isArray(tag) ? tag : [tag];
  if (type) criteria.types = Array.isArray(type) ? type : [type];
  if (status) criteria.statuses = Array.isArray(status) ? status : [status];

  return mockEngine.search('documents', criteria);
}
