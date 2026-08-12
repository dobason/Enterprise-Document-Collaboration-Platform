import React, { useState, useEffect } from 'react';
import { getDashboardStats } from '../api/dashboard.api';
import StatsCard from '../components/StatsCard';
import { useToast } from '../context/ToastContext';
import {
  FileText,
  Clock,
  CheckCircle2,
  Building2,
  BarChart3,
} from 'lucide-react';

export default function DashboardPage() {
  const { addToast } = useToast();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const data = await getDashboardStats();
        if (!cancelled) setStats(data);
      } catch (err) {
        if (!cancelled) addToast('Failed to load dashboard', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [addToast]);

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading dashboard\u2026</p>
      </div>
    );
  }

  

  const maxDeptCount = stats?.docsByDepartment?.length > 0
    ? stats.docsByDepartment.reduce((max, d) => (d.count > max ? d.count : max), 0)
    : 1;

  return (
    <div className="max-w-6xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">Dashboard</h1>
        <p className="text-sm text-slate-500 mt-1">Overview of your document management system</p>
      </div>

      {/* Stats cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatsCard
          icon={FileText}
          label="Total Documents"
          value={stats?.totalDocuments || 0}
          color="primary"
        />
        <StatsCard
          icon={Clock}
          label="Pending Approvals"
          value={stats?.pendingApprovals || 0}
          color="warning"
        />
        <StatsCard
          icon={CheckCircle2}
          label="Approved This Month"
          value={stats?.approvedThisMonth || 0}
          color="success"
        />
        <StatsCard
          icon={Building2}
          label="Departments"
          value={stats?.totalDepartments || 0}
          color="info"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Documents by Department chart */}
        <div className="card p-6">
          <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
            <BarChart3 size={16} />
            Documents by Department
          </h3>

          {stats?.docsByDepartment?.length > 0 ? (
            <div className="space-y-3">
              {stats.docsByDepartment.map((dept) => (
                <div key={dept.name}>
                  <div className="flex items-center justify-between text-sm mb-1">
                    <span className="text-slate-600">{dept.name}</span>
                    <span className="text-slate-800 font-medium">{dept.count}</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                    <div
                      className="bg-primary-500 h-full rounded-full transition-[width] duration-500"
                      style={{ width: `${(dept.count / maxDeptCount) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-400">No data available</p>
          )}
        </div>

        {/* Documents by Status */}
        <div className="card p-6">
          <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
            <FileText size={16} />
            Documents by Status
          </h3>

          {stats?.docsByStatus?.length > 0 ? (
            <div className="space-y-3">
              {stats.docsByStatus.map((item) => {
                const total = stats.totalDocuments || 1;
                const pct = Math.round((item.count / total) * 100);
                const barColor =
                  item.status === 'APPROVED' ? 'bg-green-500' :
                  item.status === 'PENDING' ? 'bg-cyan-500' :
                  item.status === 'REJECTED' ? 'bg-red-500' :
                  'bg-amber-500';

                return (
                  <div key={item.status}>
                    <div className="flex items-center justify-between text-sm mb-1">
                      <div className="flex items-center gap-2">
                        <span className={`badge ${
                          item.status === 'APPROVED' ? 'badge-approved' :
                          item.status === 'PENDING' ? 'badge-pending' :
                          item.status === 'REJECTED' ? 'badge-rejected' :
                          'badge-draft'
                        }`}>
                          {item.status}
                        </span>
                      </div>
                      <span className="text-slate-800 font-medium">
                        {item.count} ({pct}%)
                      </span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                      <div
                        className={`${barColor} h-full rounded-full transition-[width] duration-500`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-sm text-slate-400">No data available</p>
          )}
        </div>
      </div>
    </div>
  );
}
