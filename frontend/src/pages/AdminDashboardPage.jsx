import React, { useState, useEffect } from 'react';
import { getDashboardStats } from '../api/dashboard.api';
import { useToast } from '../context/ToastContext';
import {
  FileText,
  Users,
  Folder,
  Building2,
  Clock,
  CheckCircle2,
  BarChart3,
} from 'lucide-react';

export default function AdminDashboardPage() {
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

  const s = stats || {};
  const maxDeptCount = s.docsByDepartment?.length > 0
    ? s.docsByDepartment.reduce((max, d) => (d.count > max ? d.count : max), 0)
    : 1;

  const cards = [
    { label: 'Total Documents', value: s.totalDocuments || 0, icon: FileText, color: 'bg-primary-50 text-primary-600' },
    { label: 'Total Users', value: s.totalUsers || 0, icon: Users, color: 'bg-purple-50 text-purple-600' },
    { label: 'Total Folders', value: s.totalFolders || 0, icon: Folder, color: 'bg-amber-50 text-amber-600' },
    { label: 'Departments', value: s.totalDepartments || 0, icon: Building2, color: 'bg-cyan-50 text-cyan-600' },
    { label: 'Pending Approvals', value: s.pendingApprovals || 0, icon: Clock, color: 'bg-orange-50 text-orange-600' },
    { label: 'Approved This Month', value: s.approvedThisMonth || 0, icon: CheckCircle2, color: 'bg-green-50 text-green-600' },
  ];

  return (
    <div>
      <div className="mb-6">
        <h1 className="page-title">System Dashboard</h1>
        <p className="page-subtitle">Global overview of the entire system</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {cards.map((card) => (
          <div key={card.label} className="card p-5">
            <div className="flex items-center gap-4">
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${card.color}`}>
                <card.icon size={24} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-2xl font-bold text-slate-800">{card.value}</p>
                <p className="text-sm text-slate-500 mt-0.5">{card.label}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Documents by Department */}
        <div className="card p-6">
          <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
            <BarChart3 size={16} /> Documents by Department
          </h3>
          {s.docsByDepartment?.length > 0 ? (
            <div className="space-y-3">
              {s.docsByDepartment.map((dept) => (
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
          ) : <p className="text-sm text-slate-400">No data available</p>}
        </div>

        {/* Documents by Status */}
        <div className="card p-6">
          <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
            <FileText size={16} /> Documents by Status
          </h3>
          {s.docsByStatus?.length > 0 ? (
            <div className="space-y-3">
              {s.docsByStatus.map((item) => {
                const total = s.totalDocuments || 1;
                const pct = Math.round((item.count / total) * 100);
                const barColor =
                  item.status === 'APPROVED' ? 'bg-green-500' :
                  item.status === 'PENDING' ? 'bg-cyan-500' :
                  item.status === 'REJECTED' ? 'bg-red-500' : 'bg-amber-500';
                return (
                  <div key={item.status}>
                    <div className="flex items-center justify-between text-sm mb-1">
                      <span className={`badge ${
                        item.status === 'APPROVED' ? 'badge-approved' :
                        item.status === 'PENDING' ? 'badge-pending' :
                        item.status === 'REJECTED' ? 'badge-rejected' : 'badge-draft'
                      }`}>{item.status}</span>
                      <span className="text-slate-800 font-medium">{item.count} ({pct}%)</span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                      <div className={`${barColor} h-full rounded-full`} style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : <p className="text-sm text-slate-400">No data available</p>}
        </div>
      </div>
    </div>
  );
}
