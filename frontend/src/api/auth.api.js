import { mockEngine } from './mock/engine';

const TOKEN_KEY = 'edms_token';
const USER_KEY = 'edms_user';

export async function login(email, password) {
  if (!email || !password) {
    throw new Error('Email and password are required');
  }

  // Find user by email in mock data
  const result = await mockEngine.query('users', { q: email });
  let user = result.items.find((u) => u.email === email);

  // If not found, create a mock user with random role
  if (!user) {
    const roles = ['VIEWER', 'EDITOR', 'MANAGER'];
    const randomRole = roles[Math.floor(Math.random() * roles.length)];
    user = {
      id: `u_mock_${Date.now()}`,
      email,
      name: email.split('@')[0],
      role: randomRole,
      department: 'General',
      avatar: null,
    };
  }

  // Generate mock token
  const token = `mock_jwt_${btoa(email)}_${Date.now()}`;

  // Store in localStorage
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));

  return { token, user };
}

export function logout() {
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
