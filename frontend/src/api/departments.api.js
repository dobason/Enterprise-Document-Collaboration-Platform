import { apiFetch } from './client';

export async function listDepartments() {
  return apiFetch('/departments');
}

export async function createDepartment(data) {
  return apiFetch('/departments', {
    method: 'POST',
    body: data,
  });
}

export async function updateDepartment(id, data) {
  return apiFetch(`/departments/${id}`, {
    method: 'PUT',
    body: data,
  });
}

export async function getDepartmentUsers(id) {
  return apiFetch(`/departments/${id}/users`);
}
