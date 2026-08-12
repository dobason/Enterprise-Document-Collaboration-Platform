import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { listUsers, updateUserRole, createUser, updateUserDepartment } from '../api/users.api';
import { listDepartments } from '../api/departments.api';
import { Users, Plus, X, Edit2 } from 'lucide-react';

export default function AdminUsersPage() {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [departments, setDepartments] = useState([]);
  const [saving, setSaving] = useState(false);

  // Create modal state
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({ name: '', email: '', role: 'USER', departmentId: '' });

  // Edit modal state - pre-populated with the selected user's data
  const [showEditModal, setShowEditModal] = useState(false);
  const [editFormData, setEditFormData] = useState({ userId: '', name: '', role: 'USER', departmentId: '' });

  const loadData = async () => {
    try {
      const [usersData, deptsData] = await Promise.all([
        listUsers(),
        listDepartments().catch(() => [])
      ]);
      setUsers(usersData);
      setDepartments(deptsData);
    } catch (err) {
      addToast('Failed to load users', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openEditModal = (u) => {
    // Find the department ID matching the user's current department name
    const currentDept = departments.find(d => d.name === u.department);
    setEditFormData({
      userId: u.id,
      name: u.name,
      role: u.role,
      departmentId: currentDept?.id || '',
    });
    setShowEditModal(true);
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createUser(formData);
      addToast('User created successfully. Default password is Password123!', 'success');
      setShowModal(false);
      setFormData({ name: '', email: '', role: 'USER', departmentId: '' });
      loadData();
    } catch (err) {
      addToast(err.message || 'Failed to create user', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleEditUser = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      // Update role if changed
      const targetUser = users.find(u => u.id === editFormData.userId);
      if (targetUser && targetUser.role !== editFormData.role) {
        await updateUserRole(editFormData.userId, editFormData.role);
      }
      // Update department
      await updateUserDepartment(editFormData.userId, editFormData.departmentId);

      addToast('User updated successfully', 'success');
      setShowEditModal(false);
      loadData();
    } catch (err) {
      addToast(err.message || 'Failed to update user', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
            <Users size={24} className="text-primary-600" /> User Management
          </h1>
          <p className="text-sm text-slate-500">Manage system users and their roles</p>
        </div>
        <button onClick={() => setShowModal(true)} className="btn btn-primary">
          <Plus size={16} /> New User
        </button>
      </div>

      <div className="card">
        {loading ? (
          <div className="p-8 flex justify-center">
            <div className="spinner" />
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200">
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">User</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Email</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Department</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Role</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/50 transition-colors">
                    <td className="px-6 py-4">
                      <div className="font-medium text-slate-800">{u.name}</div>
                      {user.id === u.id && (
                        <span className="text-xs text-primary-500">You</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm text-slate-500">{u.email}</td>
                    <td className="px-6 py-4 text-sm text-slate-500">{u.department || <span className="text-slate-300 italic">No department</span>}</td>
                    <td className="px-6 py-4">
                      <span className={`badge ${u.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-slate-100 text-slate-600'}`}>
                        {u.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => openEditModal(u)}
                        disabled={user.id === u.id}
                        className="btn btn-secondary btn-sm"
                        title={user.id === u.id ? 'Cannot edit yourself' : 'Edit user'}
                      >
                        <Edit2 size={14} /> Edit
                      </button>
                    </td>
                  </tr>
                ))}
                {users.length === 0 && (
                  <tr>
                    <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                      No users found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create User Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !saving && setShowModal(false)} />
          <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-bold text-slate-800">Create New User</h3>
              <button onClick={() => setShowModal(false)} className="p-1 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg">
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateUser} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Full Name <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="input"
                  required
                  disabled={saving}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Email Address <span className="text-red-500">*</span></label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="input"
                  required
                  disabled={saving}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">System Role</label>
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                  className="input"
                  disabled={saving}
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Department</label>
                <select
                  value={formData.departmentId}
                  onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                  className="input"
                  disabled={saving}
                >
                  <option value="">-- No Department --</option>
                  {departments.map((dept) => (
                    <option key={dept.id} value={dept.id}>{dept.name} ({dept.code})</option>
                  ))}
                </select>
              </div>

              <div className="p-3 bg-blue-50 text-blue-700 text-sm rounded-lg border border-blue-100">
                The new user will be able to log in with the default password: <strong>Password123!</strong>
              </div>

              <div className="pt-2 flex justify-end gap-3">
                <button type="button" onClick={() => setShowModal(false)} disabled={saving} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={saving || !formData.name || !formData.email} className="btn btn-primary">
                  {saving ? <div className="spinner spinner-sm border-white border-t-transparent" /> : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit User Modal */}
      {showEditModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !saving && setShowEditModal(false)} />
          <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="text-lg font-bold text-slate-800">Edit User</h3>
                <p className="text-sm text-slate-500 mt-0.5">{editFormData.name}</p>
              </div>
              <button onClick={() => setShowEditModal(false)} className="p-1 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg">
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleEditUser} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">System Role</label>
                <select
                  value={editFormData.role}
                  onChange={(e) => setEditFormData({ ...editFormData, role: e.target.value })}
                  className="input"
                  disabled={saving}
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Department</label>
                <select
                  value={editFormData.departmentId}
                  onChange={(e) => setEditFormData({ ...editFormData, departmentId: e.target.value })}
                  className="input"
                  disabled={saving}
                >
                  <option value="">-- No Department --</option>
                  {departments.map((dept) => (
                    <option key={dept.id} value={dept.id}>{dept.name} ({dept.code})</option>
                  ))}
                </select>
              </div>

              <div className="pt-2 flex justify-end gap-3">
                <button type="button" onClick={() => setShowEditModal(false)} disabled={saving} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={saving} className="btn btn-primary">
                  {saving ? <div className="spinner spinner-sm border-white border-t-transparent" /> : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
