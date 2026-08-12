import React from 'react';
import DashboardPage from './DashboardPage';

export default function AdminDashboardPage() {
  return (
    <div className="admin-dashboard">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">System Dashboard</h1>
        <p className="text-sm text-slate-500">Global overview of the entire system</p>
      </div>
      <DashboardPage />
    </div>
  );
}
