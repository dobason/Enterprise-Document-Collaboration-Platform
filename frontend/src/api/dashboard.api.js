import { apiFetch } from './client';

export async function getDashboardStats() {
  return apiFetch('/dashboard/stats');
}
