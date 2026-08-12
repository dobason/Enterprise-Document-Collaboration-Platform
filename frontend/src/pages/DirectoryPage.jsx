import React, { useState, useEffect, useMemo } from 'react';
import { listUsers } from '../api/users.api';
import { useToast } from '../context/ToastContext';
import {
  Users,
  Mail,
  Shield,
  Search,
  Building2,
} from 'lucide-react';

export default function DirectoryPage() {
  const { addToast } = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDept, setSelectedDept] = useState('All');

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const data = await listUsers();
        if (!cancelled) setUsers(data);
      } catch (err) {
        if (!cancelled) addToast('Failed to load directory', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [addToast]);

  const departments = useMemo(() => {
    const counts = { All: users.length };
    users.forEach(u => {
      const dept = u.department || 'Unassigned';
      counts[dept] = (counts[dept] || 0) + 1;
    });
    return Object.entries(counts).map(([name, count]) => ({ name, count }));
  }, [users]);

  const filteredUsers = useMemo(() => {
    return users.filter(u => {
      const matchesDept = selectedDept === 'All' || (u.department || 'Unassigned') === selectedDept;
      const matchesSearch = u.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
                            u.email.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesDept && matchesSearch;
    });
  }, [users, selectedDept, searchQuery]);

  const getRoleBadge = (role) => {
    switch (role) {
      case 'OWNER': return 'bg-purple-100 text-purple-700 ring-1 ring-purple-600/20';
      case 'ADMIN': return 'bg-red-100 text-red-700 ring-1 ring-red-600/20';
      case 'MANAGER': return 'bg-amber-100 text-amber-700 ring-1 ring-amber-600/20';
      case 'EDITOR': return 'bg-blue-100 text-blue-700 ring-1 ring-blue-600/20';
      default: return 'bg-slate-100 text-slate-700 ring-1 ring-slate-600/20';
    }
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading directory…</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
            <Users size={24} className="text-primary-600" />
            Directory
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            {filteredUsers.length} {filteredUsers.length === 1 ? 'member' : 'members'} in {selectedDept}
          </p>
        </div>
        <div className="relative w-full sm:w-72">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search members by name or email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="input pl-9"
          />
        </div>
      </div>

      {/* Department Filters (Horizontal) */}
      <div className="flex items-center gap-2 overflow-x-auto pb-4 mb-2 no-scrollbar">
        {departments.map(dept => (
          <button
            key={dept.name}
            onClick={() => setSelectedDept(dept.name)}
            className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
              selectedDept === dept.name 
                ? 'bg-primary-600 text-white shadow-sm' 
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
            }`}
          >
            {dept.name}
            <span className={`text-xs px-2 py-0.5 rounded-full ${
              selectedDept === dept.name ? 'bg-primary-500/50 text-white' : 'bg-slate-100 text-slate-500'
            }`}>
              {dept.count}
            </span>
          </button>
        ))}
      </div>

      {/* User Table */}
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50/50">
                <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-6 py-4">
                  Member
                </th>
                <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-6 py-4">
                  Department
                </th>
                <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-6 py-4">
                  Role
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredUsers.map(user => (
                <tr key={user.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-100 to-primary-200 text-primary-700 flex items-center justify-center text-sm font-bold shrink-0">
                        {user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                      </div>
                      <div>
                        <p className="text-sm font-medium text-slate-800">{user.name}</p>
                        <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-0.5">
                          <Mail size={12} />
                          {user.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2 text-sm text-slate-600">
                      <Building2 size={16} className="text-slate-400" />
                      {user.department || 'Unassigned'}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <Shield size={14} className="text-slate-400" />
                      <span className={`badge ${getRoleBadge(user.role)}`}>{user.role}</span>
                    </div>
                  </td>
                </tr>
              ))}
              {filteredUsers.length === 0 && (
                <tr>
                  <td colSpan="3" className="px-6 py-12 text-center text-slate-500">
                    No members found matching your criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
