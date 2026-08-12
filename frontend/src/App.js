import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ErrorBoundary from './components/ErrorBoundary';
import ToastContainer from './components/Toast';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import AdminLayout from './components/AdminLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DocumentListPage from './pages/DocumentListPage';
import VersionHistoryPage from './pages/VersionHistoryPage';
import SearchPage from './pages/SearchPage';
import PermissionManagerPage from './pages/PermissionManagerPage';
import ApprovalPage from './pages/ApprovalPage';
import FolderDetailPage from './pages/FolderDetailPage';
import FolderListPage from './pages/FolderListPage';
import DashboardPage from './pages/DashboardPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import AdminDepartmentsPage from './pages/AdminDepartmentsPage';
import AdminDepartmentUsersPage from './pages/AdminDepartmentUsersPage';
import AdminFoldersPage from './pages/AdminFoldersPage';
import AdminFolderDetailPage from './pages/AdminFolderDetailPage';
import PublicSharePage from './pages/PublicSharePage';

const DocumentEditorPage = lazy(() => import('./pages/DocumentEditorPage'));

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/share/:shareToken" element={<PublicSharePage />} />
              
              <Route element={<ProtectedRoute />}>
                {/* Standard User Layout */}
                <Route element={<Layout />}>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/dashboard" element={<DashboardPage />} />
                  <Route path="/documents" element={<DocumentListPage />} />
                  <Route path="/documents/:id" element={
                    <Suspense fallback={<div className="loading-center"><div className="spinner" /><p>Loading editor...</p></div>}>
                      <DocumentEditorPage />
                    </Suspense>
                  } />
                  <Route path="/documents/:id/versions" element={<VersionHistoryPage />} />
                  <Route path="/documents/:id/permissions" element={<PermissionManagerPage />} />
                  <Route path="/documents/:id/approval" element={<ApprovalPage />} />
                  <Route path="/search" element={<SearchPage />} />
                  <Route path="/folders" element={<FolderListPage />} />
                  <Route path="/folders/:id" element={<FolderDetailPage />} />
                </Route>

                {/* Admin Layout */}
                <Route path="/admin" element={<AdminLayout />}>
                  <Route index element={<Navigate to="dashboard" replace />} />
                  <Route path="dashboard" element={<AdminDashboardPage />} />
                  <Route path="departments" element={<AdminDepartmentsPage />} />
                  <Route path="departments/:id/users" element={<AdminDepartmentUsersPage />} />
                  <Route path="users" element={<AdminUsersPage />} />
                  <Route path="folders" element={<AdminFoldersPage />} />
                  <Route path="folders/:id" element={<AdminFolderDetailPage />} />
                  <Route path="documents/:id" element={
                    <Suspense fallback={<div className="loading-center"><div className="spinner" /><p>Loading editor...</p></div>}>
                      <DocumentEditorPage />
                    </Suspense>
                  } />
                  <Route path="documents/:id/permissions" element={<PermissionManagerPage />} />
                  <Route path="documents/:id/versions" element={<VersionHistoryPage />} />
                </Route>

                <Route path="*" element={<Navigate to="/documents" replace />} />
              </Route>
            </Routes>
          </BrowserRouter>
          <ToastContainer />
        </ToastProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
}
