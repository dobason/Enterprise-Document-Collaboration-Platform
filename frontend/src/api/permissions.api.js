import { mockEngine } from './mock/engine';
import { getDataStore } from './mock/data';

export async function getPermissions(docId) {
  const store = getDataStore();
  const docPerms = store.permissions?.filter((p) => p.documentId === docId) || [];

  return docPerms.map((perm) => {
    const user = store.users?.find((u) => u.id === perm.userId);
    return {
      ...perm,
      userName: user?.name || 'Unknown',
      userEmail: user?.email || 'unknown@edms.vn',
    };
  });
}

export async function grantPermission(docId, userId, role) {
  return mockEngine.create('permissions', {
    documentId: docId,
    userId,
    role,
  });
}

export async function removePermission(docId, permissionId) {
  return mockEngine.delete('permissions', permissionId);
}

export async function updatePermission(docId, permissionId, role) {
  return mockEngine.update('permissions', permissionId, { role });
}

export async function getUserRole(docId, userId) {
  const store = getDataStore();
  const perm = store.permissions?.find(
    (p) => p.documentId === docId && p.userId === userId
  );
  return perm?.role || null;
}
