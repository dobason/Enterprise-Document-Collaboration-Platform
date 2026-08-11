import { apiFetch } from './client';

// Danh bạ người dùng hệ thống (dùng cho PermissionManagerPage)
export async function listUsers() {
  const res = await apiFetch('/users');
  return res.items || [];
}
