import { apiFetch } from './client';

// Danh bạ người dùng hệ thống (dùng cho PermissionManagerPage)
export async function listUsers() {
  const res = await apiFetch('/users');
  return res.items || [];
}

export async function updateUserRole(userId, role) {
  return apiFetch(`/users/${userId}/role`, {
    method: 'PUT',
    body: { role },
  });
}

export async function createUser(data) {
  return apiFetch('/users', {
    method: 'POST',
    body: data,
  });
}

export async function updateUserDepartment(userId, departmentId) {
  return apiFetch(`/users/${userId}/department`, {
    method: 'PUT',
    body: { departmentId },
  });
}
