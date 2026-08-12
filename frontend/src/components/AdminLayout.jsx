import React from 'react';
import { Outlet, Navigate, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Building2,
  FolderOpen,
  LogOut,
} from 'lucide-react';

export default function AdminLayout() {
  const { user, logout } = useAuth();

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  const navItems = [
    { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
    { name: 'Departments', path: '/admin/departments', icon: Building2 },
    { name: 'Users', path: '/admin/users', icon: Users },
    { name: 'Folders', path: '/admin/folders', icon: FolderOpen },
  ];

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div className="flex h-screen bg-slate-50">
      {/* Admin Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <h1 className="text-xl font-bold text-white flex items-center gap-2">
            <span className="text-primary-500">EDMS</span> Admin
          </h1>
        </div>

        <nav className="flex-1 overflow-y-auto py-4">
          <ul className="space-y-1 px-3">
            {navItems.map((item) => (
              <li key={item.name}>
                <NavLink
                  to={item.path}
                  className={({ isActive }) =>
                    `sidebar-link ${isActive ? 'sidebar-link-active' : ''}`
                  }
                >
                  <item.icon size={18} />
                  <span className="font-medium text-sm">{item.name}</span>
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <div className="p-4 border-t border-slate-800">
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 w-full text-sm text-slate-400 hover:text-white transition-colors"
          >
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 overflow-y-auto bg-slate-50 p-6 md:p-8">
        <Outlet />
      </main>
    </div>
  );
}
