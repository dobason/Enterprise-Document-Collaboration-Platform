import { apiFetch, getToken } from './client';

const TOKEN_KEY = 'edms_token';
const USER_KEY = 'edms_user';

export async function login(email, password) {
  if (!email || !password) {
    throw new Error('Email and password are required');
  }

  const data = await apiFetch('/auth/login', {
    method: 'POST',
    body: { email, password },
  });

  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));

  return { token: data.token, user: data.user };
}

export async function register(name, email, password) {
  if (!name || !email || !password) {
    throw new Error('All fields are required');
  }

  const data = await apiFetch('/auth/register', {
    method: 'POST',
    body: { name, email, password },
  });

  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));

  return { token: data.token, user: data.user };
}

export async function logout() {
  // Best-effort: báo backend logout, không quan trọng kết quả
  try {
    await apiFetch('/auth/logout', { method: 'POST' });
  } catch {
    // ignore
  }
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getCurrentUser() {
  const token = localStorage.getItem(TOKEN_KEY);
  const userStr = localStorage.getItem(USER_KEY);

  if (!token || !userStr) {
    return null;
  }

  try {
    const user = JSON.parse(userStr);
    return { token, user };
  } catch {
    return null;
  }
}

export function isAuthenticated() {
  return !!localStorage.getItem(TOKEN_KEY);
}

export { getToken };
