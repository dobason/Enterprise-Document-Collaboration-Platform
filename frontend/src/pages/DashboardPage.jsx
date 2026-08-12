import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getDashboardStats } from '../api/dashboard.api';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import {
  FileText,
  Clock,
  CheckCircle2,
  Building2,
  BarChart3,
  Folder,
  Upload,
  TrendingUp,
  AlertCircle,
  ArrowRight,
} from 'lucide-react';

function AnimatedNumber({ value, duration = 1000 }) {
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    let start = null;
    const from = 0;
    const step = (timestamp) => {
      if (!start) start = timestamp;
      const progress = Math.min((timestamp - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplay(Math.round(from + (value - from) * eased));
      if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }, [value, duration]);

  return <span>{display.toLocaleString()}</span>;
}

function DonutSegment({ pct, color, offset }) {
  const r = 36;
  const circ = 2 * Math.PI * r;
  const dash = (pct / 100) * circ;
  return (
    <circle
      cx="44" cy="44" r={r}
      fill="none" stroke={color} strokeWidth="14"
      strokeDasharray={`${dash} ${circ - dash}`}
      strokeDashoffset={-offset * circ / 100}
      strokeLinecap="butt"
      style={{ transition: 'stroke-dasharray 0.8s ease' }}
    />
  );
}

const DEPT_COLORS = [
  'bg-primary-500', 'bg-violet-500', 'bg-cyan-500',
  'bg-emerald-500', 'bg-amber-500', 'bg-rose-500',
];

const STATUS_CONFIG = {
  APPROVED: { color: '#22c55e', label: 'Approved', badgeClass: 'badge-approved' },
  PENDING:  { color: '#06b6d4', label: 'Pending',  badgeClass: 'badge-pending' },
  DRAFT:    { color: '#f59e0b', label: 'Draft',    badgeClass: 'badge-draft' },
  REJECTED: { color: '#ef4444', label: 'Rejected', badgeClass: 'badge-rejected' },
};

function getGreeting(hour) {
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function DashboardPage() {
  const { addToast } = useToast();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const hour = new Date().getHours();
  const greeting = getGreeting(hour);
  const firstName = user?.name ? user.name.split(' ')[0] : 'there';

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
        <p>Loading dashboard…</p>
      </div>
    );
  }

  const maxDeptCount = stats?.docsByDepartment?.length > 0
    ? stats.docsByDepartment.reduce((max, d) => (d.count > max ? d.count : max), 0)
    : 1;

  const totalDocs = stats?.totalDocuments || 1;
  let donutOffset = 0;
  const donutSegments = (stats?.docsByStatus || []).map((item) => {
    const cfg = STATUS_CONFIG[item.status] || {};
    const pct = Math.round((item.count / totalDocs) * 100);
    const seg = { ...item, pct, color: cfg.color || '#94a3b8', offset: donutOffset };
    donutOffset += pct;
    return seg;
  });

  const statCards = [
    {
      icon: FileText, label: 'Total Files', value: stats?.totalDocuments || 0,
      gradient: 'from-primary-500 to-primary-700',
      sub: 'Documents managed',
    },
    {
      icon: Clock, label: 'Pending Review', value: stats?.pendingApprovals || 0,
      gradient: 'from-amber-400 to-orange-500',
      sub: 'Awaiting approval',
    },
    {
      icon: CheckCircle2, label: 'Approved This Month', value: stats?.approvedThisMonth || 0,
      gradient: 'from-emerald-400 to-teal-600',
      sub: 'Approved recently',
    },
    {
      icon: Building2, label: 'Departments', value: stats?.totalDepartments || 0,
      gradient: 'from-violet-500 to-purple-700',
      sub: 'Active departments',
    },
  ];

  const quickActions = [
    { icon: Upload,  label: 'Upload File',    desc: 'Add a new document',     onClick: () => navigate('/folders/f1'), color: 'text-primary-600 bg-primary-50' },
    { icon: Folder,  label: 'Browse Folders', desc: 'View all folders',        onClick: () => navigate('/folders/f1'), color: 'text-violet-600 bg-violet-50' },
    { icon: Clock,   label: 'Pending Items',  desc: 'Review approvals',        onClick: () => navigate('/documents'),   color: 'text-amber-600 bg-amber-50' },
    { icon: TrendingUp, label: 'Analytics',   desc: 'View insights',           onClick: () => {},                       color: 'text-teal-600 bg-teal-50' },
  ];

  return (
    <div className="max-w-7xl mx-auto">

      {/* Greeting Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-primary-600 via-primary-700 to-violet-700 p-6 mb-8 text-white shadow-lg">
        <div className="absolute -top-8 -right-8 w-40 h-40 rounded-full bg-white/5" />
        <div className="absolute top-6 right-12 w-24 h-24 rounded-full bg-white/5" />
        <div className="relative z-10">
          <p className="text-sm font-medium text-primary-200 mb-1">{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
          <h1 className="text-2xl font-bold mb-1">{greeting}, {firstName}! 👋</h1>
          <p className="text-primary-200 text-sm">Here's your document management overview for today.</p>
        </div>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {statCards.map((card) => (
          <div key={card.label} className={`relative overflow-hidden rounded-2xl bg-gradient-to-br ${card.gradient} p-5 text-white shadow-md`}>
            <div className="absolute -bottom-4 -right-4 w-20 h-20 rounded-full bg-white/10" />
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center">
                <card.icon size={20} />
              </div>
              <ArrowRight size={16} className="text-white/40" />
            </div>
            <p className="text-3xl font-bold mb-0.5">
              <AnimatedNumber value={card.value} />
            </p>
            <p className="text-sm font-semibold text-white/90">{card.label}</p>
            <p className="text-xs text-white/60 mt-0.5">{card.sub}</p>
          </div>
        ))}
      </div>

      {/* Charts + Quick Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">

        {/* Documents by Department */}
        <div className="lg:col-span-2 card p-6">
          <h3 className="text-sm font-semibold text-slate-700 mb-5 flex items-center gap-2">
            <BarChart3 size={16} className="text-primary-500" />
            Documents by Department
          </h3>
          {stats?.docsByDepartment?.length > 0 ? (
            <div className="space-y-4">
              {stats.docsByDepartment.map((dept, i) => {
                const pct = Math.round((dept.count / maxDeptCount) * 100);
                const barClass = DEPT_COLORS[i % DEPT_COLORS.length];
                return (
                  <div key={dept.name}>
                    <div className="flex items-center justify-between text-sm mb-1.5">
                      <span className="text-slate-600 font-medium">{dept.name}</span>
                      <span className="text-slate-800 font-bold">{dept.count}</span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                      <div
                        className={`${barClass} h-full rounded-full transition-[width] duration-700`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-32 text-slate-400">
              <AlertCircle size={32} className="mb-2 text-slate-200" />
              <p className="text-sm">No data available</p>
            </div>
          )}
        </div>

        {/* Status Distribution Donut */}
        <div className="card p-6 flex flex-col">
          <h3 className="text-sm font-semibold text-slate-700 mb-5 flex items-center gap-2">
            <FileText size={16} className="text-primary-500" />
            Status Distribution
          </h3>
          {donutSegments.length > 0 ? (
            <>
              <div className="flex items-center justify-center mb-5">
                <div className="relative">
                  <svg width="88" height="88" viewBox="0 0 88 88" style={{ transform: 'rotate(-90deg)' }}>
                    <circle cx="44" cy="44" r="36" fill="none" stroke="#f1f5f9" strokeWidth="14" />
                    {donutSegments.map((seg) => (
                      <DonutSegment key={seg.status} pct={seg.pct} color={seg.color} offset={seg.offset} />
                    ))}
                  </svg>
                  <div className="absolute inset-0 flex flex-col items-center justify-center">
                    <span className="text-lg font-bold text-slate-800">{stats?.totalDocuments || 0}</span>
                    <span className="text-[10px] text-slate-400">Total</span>
                  </div>
                </div>
              </div>
              <div className="space-y-2 flex-1">
                {donutSegments.map((seg) => {
                  const cfg = STATUS_CONFIG[seg.status] || {};
                  return (
                    <div key={seg.status} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: seg.color }} />
                        <span className="text-xs text-slate-600">{cfg.label || seg.status}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-800">{seg.count}</span>
                        <span className="text-xs text-slate-400">{seg.pct}%</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center justify-center flex-1 text-slate-400">
              <AlertCircle size={32} className="mb-2 text-slate-200" />
              <p className="text-sm">No data available</p>
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card p-6">
        <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
          <TrendingUp size={16} className="text-primary-500" />
          Quick Actions
        </h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {quickActions.map((action) => (
            <button
              key={action.label}
              onClick={action.onClick}
              className="flex flex-col items-center gap-2 p-4 rounded-xl hover:bg-slate-50 border border-slate-100 hover:border-slate-200 hover:shadow-sm transition-all group text-center"
            >
              <div className={`w-11 h-11 rounded-xl flex items-center justify-center ${action.color} group-hover:scale-110 transition-transform`}>
                <action.icon size={20} />
              </div>
              <div>
                <p className="text-sm font-semibold text-slate-700">{action.label}</p>
                <p className="text-xs text-slate-400">{action.desc}</p>
              </div>
            </button>
          ))}
        </div>
      </div>

    </div>
  );
}

