import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  FileText,
  LogOut,
  Menu,
  ChevronDown,
  Shield
} from 'lucide-react';

const baseNavItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/documents', label: 'Documents', icon: FileText },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const navItems = [
    ...baseNavItems,
    ...(user?.role === 'ADMIN' ? [{ to: '/admin', label: 'Admin Panel', icon: Shield }] : [])
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userInitials = user?.name
    ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
    : '??';

  const roleBadgeColor = {
    USER: 'bg-blue-100 text-blue-700 ring-1 ring-blue-600/20',
    MANAGER: 'bg-amber-100 text-amber-700 ring-1 ring-amber-600/20',
    ADMIN: 'bg-red-100 text-red-700 ring-1 ring-red-600/20',
  };

  const roleColor = roleBadgeColor[user?.role] || roleBadgeColor.USER;

  return (
    <div className="flex h-screen bg-slate-50">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <button
          className="fixed inset-0 bg-black/30 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
          aria-label="Close menu"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed lg:static inset-y-0 left-0 z-30
          sidebar
          transition-transform duration-200
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        {/* Logo */}
        <div className="flex items-center gap-3 h-16 px-6 border-b border-slate-800">
          <div className="w-8 h-8 rounded-lg bg-primary-600 flex items-center justify-center">
            <FileText size={18} className="text-white" />
          </div>
          <span className="font-bold text-lg text-white">EDMS</span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? 'sidebar-link-active' : ''}`
              }
            >
              <item.icon size={20} aria-hidden="true" />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        {/* Sidebar footer - interactive user menu */}
        <div className="relative p-4 border-t border-slate-800">
          <button
            onClick={() => setUserMenuOpen(!userMenuOpen)}
            className="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <div
              className="w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center text-xs font-bold"
              role="img"
              aria-label={user?.name || 'User avatar'}
            >
              {userInitials}
            </div>
            <div className="flex-1 min-w-0 text-left">
              <p className="text-sm font-medium text-slate-100 truncate">{user?.name}</p>
              <p className="text-xs text-slate-400 truncate">{user?.email}</p>
            </div>
            <span className={`badge ${roleColor}`}>{user?.role}</span>
            <ChevronDown size={14} className="text-slate-400 shrink-0" aria-hidden="true" />
          </button>

          {userMenuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setUserMenuOpen(false)} />
              <div className="absolute bottom-full left-3 mb-1 w-56 bg-white rounded-lg shadow-xl border border-slate-200 py-1 z-20">
                <div className="px-4 py-3 border-b border-slate-100">
                  <p className="text-sm font-medium text-slate-800">{user?.name}</p>
                  <p className="text-xs text-slate-400">{user?.email}</p>
                  <p className="text-xs text-slate-400 mt-0.5">{user?.department}</p>
                  <span className={`badge mt-1 ${roleColor}`}>{user?.role}</span>
                </div>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-2 w-full px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <LogOut size={16} />
                  Logout
                </button>
              </div>
            </>
          )}
        </div>
      </aside>

      {/* Main area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Topbar - chỉ còn nút mở menu mobile */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center gap-4 px-4 lg:px-6 shrink-0">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden p-2 -ml-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg"
            aria-label="Open menu"
          >
            <Menu size={20} />
          </button>
          <div className="flex-1" />
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
