import { apiFetch } from './client';

export async function getDashboardStats() {
  return apiFetch('/dashboard/stats');
}

export async function getMyDocuments() {
  return apiFetch('/dashboard/my-docs');
}
