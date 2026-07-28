import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ErrorBoundary from './components/ErrorBoundary';
import ToastContainer from './components/Toast';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import DocumentListPage from './pages/DocumentListPage';
import VersionHistoryPage from './pages/VersionHistoryPage';
import SearchPage from './pages/SearchPage';
import PermissionManagerPage from './pages/PermissionManagerPage';
import ApprovalPage from './pages/ApprovalPage';
import FolderDetailPage from './pages/FolderDetailPage';
import DashboardPage from './pages/DashboardPage';

const DocumentEditorPage = lazy(() => import('./pages/DocumentEditorPage'));

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route element={<ProtectedRoute />}>
                <Route element={<Layout />}>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/dashboard" element={<DashboardPage />} />
                  <Route path="/documents" element={<DocumentListPage />} />
                  <Route path="/documents/:id" element={
                    <Suspense fallback={<div className="loading-center"><div className="spinner" /><p>Loading editor\u2026</p></div>}>
                      <DocumentEditorPage />
                    </Suspense>
                  } />
                  <Route path="/documents/:id/versions" element={<VersionHistoryPage />} />
                  <Route path="/documents/:id/permissions" element={<PermissionManagerPage />} />
                  <Route path="/documents/:id/approval" element={<ApprovalPage />} />
                  <Route path="/search" element={<SearchPage />} />
                  <Route path="/folders/:id" element={<FolderDetailPage />} />
                  <Route path="*" element={<Navigate to="/documents" replace />} />
                </Route>
              </Route>
            </Routes>
          </BrowserRouter>
          <ToastContainer />
        </ToastProvider>
      </AuthProvider>
    </ErrorBoundary>
  );
}
