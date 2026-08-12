import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../context/ToastContext';
import { listDepartments, createDepartment, updateDepartment } from '../api/departments.api';
import { Building2, Plus, Edit2, Users, X } from 'lucide-react';

export default function AdminDepartmentsPage() {
  const navigate = useNavigate();
  const { addToast } = useToast();
  
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingDept, setEditingDept] = useState(null);
  const [formData, setFormData] = useState({ code: '', name: '' });
  const [saving, setSaving] = useState(false);

  const loadData = async () => {
    try {
      const data = await listDepartments();
      setDepartments(data);
    } catch (err) {
      addToast('Failed to load departments', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.code.trim() || !formData.name.trim()) return;

    setSaving(true);
    try {
      if (editingDept) {
        await updateDepartment(editingDept.id, formData);
        addToast('Department updated', 'success');
      } else {
        await createDepartment(formData);
        addToast('Department created', 'success');
      }
      setShowModal(false);
      loadData();
    } catch (err) {
      addToast(err.message || 'Operation failed', 'error');
    } finally {
      setSaving(false);
    }
  };

  const openModal = (dept = null) => {
    setEditingDept(dept);
    setFormData(dept ? { code: dept.code, name: dept.name } : { code: '', name: '' });
    setShowModal(true);
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Departments</h1>
          <p className="page-subtitle">Manage organizational departments</p>
        </div>
        <button onClick={() => openModal()} className="btn btn-primary">
          <Plus size={16} /> New Department
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center p-12"><div className="spinner" /></div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {departments.map((dept) => (
            <div key={dept.id} className="card p-5 group hover:border-primary-300 transition-colors">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-primary-50 text-primary-600 flex items-center justify-center">
                    <Building2 size={20} />
                  </div>
                  <div>
                    <h3 className="font-semibold text-slate-800">{dept.name}</h3>
                    <p className="text-xs text-slate-500 font-mono">{dept.code}</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100 mt-4">
                <button
                  onClick={() => openModal(dept)}
                  className="btn btn-secondary btn-sm"
                  title="Edit Department"
                >
                  <Edit2 size={14} /> Edit
                </button>
                <button
                  onClick={() => navigate(`/admin/departments/${dept.id}/users`)}
                  className="btn btn-primary btn-sm"
                  title="View Users"
                >
                  <Users size={14} /> Users
                </button>
              </div>
            </div>
          ))}
          {departments.length === 0 && (
            <div className="col-span-full p-8 text-center text-slate-500">
              No departments found.
            </div>
          )}
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !saving && setShowModal(false)} />
          <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-bold text-slate-800">
                {editingDept ? 'Edit Department' : 'Create Department'}
              </h3>
              <button onClick={() => setShowModal(false)} className="p-1 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg">
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Department Code <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                  className="input uppercase"
                  placeholder="e.g. IT, HR"
                  required
                  disabled={saving}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Department Name <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="input"
                  placeholder="e.g. Information Technology"
                  required
                  disabled={saving}
                />
              </div>
              <div className="pt-2 flex justify-end gap-3">
                <button type="button" onClick={() => setShowModal(false)} disabled={saving} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={saving || !formData.code || !formData.name} className="btn btn-primary">
                  {saving ? <div className="spinner spinner-sm border-white border-t-transparent" /> : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
