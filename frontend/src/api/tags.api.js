import { mockEngine } from './mock/engine';
import { getDataStore } from './mock/data';

export async function getTags(docId) {
  const store = getDataStore();
  const docTags = store.documentTags?.filter((dt) => dt.documentId === docId) || [];
  const allTags = store.tags || [];

  return docTags.map((dt) => {
    const tag = allTags.find((t) => t.id === dt.tagId);
    return tag ? { ...tag, docTagId: dt.id } : null;
  }).filter(Boolean);
}

export async function addTag(docId, tagName) {
  const store = getDataStore();

  // Find existing tag or create new one
  let tag = store.tags?.find((t) => t.name.toLowerCase() === tagName.toLowerCase());
  if (!tag) {
    tag = await mockEngine.create('tags', { name: tagName });
  }

  // Check if tag already assigned to document
  const exists = store.documentTags?.some(
    (dt) => dt.documentId === docId && dt.tagId === tag.id
  );
  if (exists) return tag;

  const docTag = await mockEngine.create('documentTags', {
    documentId: docId,
    tagId: tag.id,
  });

  return { ...tag, docTagId: docTag.id };
}

export async function removeTag(docId, docTagId) {
  return mockEngine.delete('documentTags', docTagId);
}

export async function getAllTags() {
  const store = getDataStore();
  return store.tags || [];
}
