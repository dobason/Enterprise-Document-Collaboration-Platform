import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import * as authApi from '../api/auth.api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Khôi phục phiên từ localStorage (giữ đăng nhập khi refresh trang).
  useEffect(() => {
    const session = authApi.getCurrentUser();
    if (session) {
      setUser(session.user);
      setToken(session.token);
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email, password) => {
    const result = await authApi.login(email, password);
    setUser(result.user);
    setToken(result.token);
    return result;
  }, []);

  const logout = useCallback(() => {
    authApi.logout();
    setUser(null);
    setToken(null);
  }, []);

  const value = {
    user,
    token,
    loading,
    isAuthenticated: !!token,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
