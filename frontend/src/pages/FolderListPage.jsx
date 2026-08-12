import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useToast } from '../context/ToastContext';
import { listFolders, createFolder } from '../api/folders.api';
import { Folder, Plus, ChevronRight, X } from 'lucide-react';

export default function FolderListPage() {
  const { addToast } = useToast();
  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [newFolderDept, setNewFolderDept] = useState('');
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const data = await listFolders();
        if (!cancelled) setFolders(data);
      } catch (err) {
        if (!cancelled) addToast('Failed to load folders', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [addToast]);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!newFolderName.trim()) return;

    setCreating(true);
    try {
      const created = await createFolder(newFolderName.trim(), newFolderDept.trim() || 'General');
      setFolders((prev) => [...prev, created]);
      addToast('Folder created', 'success');
      setShowCreateModal(false);
      setNewFolderName('');
      setNewFolderDept('');
    } catch (err) {
      addToast(err.message || 'Failed to create folder', 'error');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Folders</h1>
          <p className="text-sm text-slate-500">Browse all root directories</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="btn btn-primary"
        >
          <Plus size={16} />
          New Folder
        </button>
      </div>

      {loading ? (
        <div className="p-12 flex justify-center">
          <div className="spinner" />
        </div>
      ) : folders.length === 0 ? (
        <div className="card p-12 text-center">
          <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mx-auto mb-4">
            <Folder size={32} className="text-slate-400" />
          </div>
          <h3 className="text-lg font-medium text-slate-800 mb-2">No folders yet</h3>
          <p className="text-slate-500 mb-6">Create the first folder to start organizing documents.</p>
          <button onClick={() => setShowCreateModal(true)} className="btn btn-primary mx-auto">
            <Plus size={16} /> Create Folder
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {folders.map((folder) => (
            <Link
              key={folder.id}
              to={`/folders/${folder.id}`}
              className="card p-4 flex items-center gap-4 hover:border-primary-300 hover:shadow-md transition-all group"
            >
              <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 group-hover:bg-blue-100 transition-colors">
                <Folder size={24} />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-slate-800 truncate group-hover:text-primary-700 transition-colors">
                  {folder.name}
                </h3>
                <p className="text-xs text-slate-500 truncate mt-0.5">
                  {folder.department || 'General'}
                </p>
              </div>
              <div className="text-slate-300 group-hover:text-primary-600 transition-colors">
                <ChevronRight size={20} />
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !creating && setShowCreateModal(false)} />
          <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-bold text-slate-800">Create New Folder</h3>
              <button
                onClick={() => setShowCreateModal(false)}
                disabled={creating}
                className="p-1 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label htmlFor="folderName" className="block text-sm font-medium text-slate-700 mb-1.5">
                  Folder Name <span className="text-red-500">*</span>
                </label>
                <input
                  id="folderName"
                  type="text"
                  value={newFolderName}
                  onChange={(e) => setNewFolderName(e.target.value)}
                  className="input"
                  placeholder="e.g. Legal Documents"
                  autoFocus
                  disabled={creating}
                />
              </div>
              <div>
                <label htmlFor="folderDept" className="block text-sm font-medium text-slate-700 mb-1.5">
                  Department
                </label>
                <input
                  id="folderDept"
                  type="text"
                  value={newFolderDept}
                  onChange={(e) => setNewFolderDept(e.target.value)}
                  className="input"
                  placeholder="e.g. Legal (optional)"
                  disabled={creating}
                />
              </div>
              
              <div className="pt-2 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  disabled={creating}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating || !newFolderName.trim()}
                  className="btn btn-primary"
                >
                  {creating ? (
                    <div className="spinner spinner-sm border-white border-t-transparent" />
                  ) : (
                    'Create'
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
