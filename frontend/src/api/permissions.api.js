import { apiFetch, getStoredUser } from './client';

export async function getPermissions(docId) {
  const res = await apiFetch(`/documents/${docId}/permissions`);
  return res.items || [];
}

export async function grantPermission(docId, userId, role) {
  return apiFetch(`/documents/${docId}/permissions`, {
    method: 'POST',
    body: { userId, role },
  });
}

export async function removePermission(docId, permissionId) {
  return apiFetch(`/documents/${docId}/permissions/${permissionId}`, { method: 'DELETE' });
}

export async function updatePermission(docId, permissionId, role) {
  return apiFetch(`/documents/${docId}/permissions/${permissionId}`, {
    method: 'PUT',
    body: { role },
  });
}

// Backend lấy role của user ĐANG ĐĂNG NHẬP trên document (bỏ qua tham số userId).
// Nếu user chưa được cấp quyền trên document (NONE), fallback về role toàn cục của user.
export async function getUserRole(docId, userId) {
  let role = null;
  try {
    const res = await apiFetch(`/documents/${docId}/permissions/role`);
    role = res?.role;
  } catch {
    // ignore - fallback bên dưới
  }

  if (!role || role === 'NONE') {
    const user = getStoredUser();
    role = user?.role || null;
  }

  return role;
}
