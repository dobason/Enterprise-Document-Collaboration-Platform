import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyDocuments } from '../api/dashboard.api';
import { useToast } from '../context/ToastContext';
import {
  FileText,
  Clock,
  CheckCircle2,
  XCircle,
  Folder,
} from 'lucide-react';

export default function DashboardPage() {
  const { addToast } = useToast();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const result = await getMyDocuments();
        if (!cancelled) setData(result);
      } catch (err) {
        if (!cancelled) addToast('Failed to load dashboard', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [addToast]);

  const statusBadge = (status) => {
    const map = {
      APPROVED: 'badge badge-approved',
      PENDING: 'badge badge-pending',
      REJECTED: 'badge badge-rejected',
      DRAFT: 'badge badge-draft',
    };
    return map[status] || 'badge';
  };

  const roleBadge = (role) => {
    const map = {
      OWNER: 'bg-purple-100 text-purple-700 ring-1 ring-purple-600/20',
      EDITOR: 'bg-blue-100 text-blue-700 ring-1 ring-blue-600/20',
      VIEWER: 'bg-slate-100 text-slate-700 ring-1 ring-slate-600/20',
    };
    return map[role] || 'badge';
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading dashboard\u2026</p>
      </div>
    );
  }

  const stats = data || { total: 0, approved: 0, pending: 0, rejected: 0, items: [] };

  const statCards = [
    { label: 'Total Documents', value: stats.total, icon: FileText, color: 'bg-primary-50 text-primary-600' },
    { label: 'Approved', value: stats.approved, icon: CheckCircle2, color: 'bg-green-50 text-green-600' },
    { label: 'Pending', value: stats.pending, icon: Clock, color: 'bg-amber-50 text-amber-600' },
    { label: 'Rejected', value: stats.rejected, icon: XCircle, color: 'bg-red-50 text-red-600' },
  ];

  return (
    <div className="max-w-6xl mx-auto">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Dashboard</h1>
          <p className="page-subtitle">Documents you uploaded or have access to</p>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {statCards.map((card) => (
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

      {/* Documents list */}
      <div className="card overflow-hidden">
        <div className="px-4 py-3 border-b border-slate-100 bg-slate-50/50">
          <h3 className="text-sm font-semibold text-slate-700 flex items-center gap-2">
            <FileText size={16} className="text-primary-600" />
            My Documents
          </h3>
        </div>

        {stats.items.length === 0 ? (
          <div className="p-12 text-center">
            <Folder size={40} className="text-slate-300 mx-auto mb-2" />
            <p className="text-slate-500 text-sm">No documents yet. Upload a document or get shared access.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr className="border-b border-slate-100 bg-slate-50/50">
                  <th className="table-head">Name</th>
                  <th className="table-head">Status</th>
                  <th className="table-head">Your Role</th>
                  <th className="table-head hidden md:table-cell">Updated</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {stats.items.map((doc) => (
                  <tr
                    key={doc.id}
                    onClick={() => navigate(`/documents/${doc.id}`)}
                    className="hover:bg-slate-50 transition-colors cursor-pointer"
                  >
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-3">
                        <FileText size={18} className="text-slate-400 shrink-0" />
                        <span className="text-sm font-medium text-slate-700 truncate max-w-[250px]">{doc.title}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      <span className={statusBadge(doc.status)}>{doc.status}</span>
                    </td>
                    <td className="px-4 py-3.5">
                      {doc.role && <span className={`badge ${roleBadge(doc.role)}`}>{doc.role}</span>}
                    </td>
                    <td className="px-4 py-3.5 hidden md:table-cell">
                      <span className="text-sm text-slate-500">
                        {doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '-'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
