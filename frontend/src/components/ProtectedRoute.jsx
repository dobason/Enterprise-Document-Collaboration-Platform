import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute() {
  const { isAuthenticated, loading, user } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="spinner" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // ADMIN chỉ dùng giao diện admin - đẩy về /admin nếu truy cập trang user
  if (user?.role === 'ADMIN' && !location.pathname.startsWith('/admin')) {
    return <Navigate to="/admin/dashboard" replace />;
  }

  return <Outlet />;
}
