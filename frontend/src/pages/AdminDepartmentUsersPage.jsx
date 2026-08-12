import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDepartmentUsers } from '../api/departments.api';
import { useToast } from '../context/ToastContext';
import { ArrowLeft, Users, Shield, Building2 } from 'lucide-react';

export default function AdminDepartmentUsersPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();
  
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const data = await getDepartmentUsers(id);
        if (!cancelled) setUsers(data.items || []);
      } catch (err) {
        if (!cancelled) addToast('Failed to load department users', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [id, addToast]);

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate('/admin/departments')} className="p-2 rounded-lg hover:bg-slate-100 text-slate-500">
          <ArrowLeft size={18} />
        </button>
        <div>
          <h1 className="page-title flex items-center gap-2">
            <Building2 size={24} className="text-primary-600" /> Department Users
          </h1>
          <p className="page-subtitle">View users within this department</p>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="flex justify-center p-12"><div className="spinner" /></div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200">
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">User</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Email</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Role</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500">
                          <Users size={14} />
                        </div>
                        <span className="font-medium text-slate-800">{u.name}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-slate-500">{u.email}</td>
                    <td className="px-6 py-4">
                      <span className={`badge ${u.role === 'ADMIN' ? 'bg-red-100 text-red-700' : 'bg-blue-100 text-blue-700'}`}>
                        {u.role === 'ADMIN' && <Shield size={12} className="mr-1" />}
                        {u.role}
                      </span>
                    </td>
                  </tr>
                ))}
                {users.length === 0 && (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-slate-500">
                      No users found in this department.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
