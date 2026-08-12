import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { listFolders, createFolder } from '../api/folders.api';
import { useToast } from '../context/ToastContext';
import SearchBar from '../components/SearchBar';
import {
  Folder,
  ChevronRight,
  Plus,
  Inbox,
} from 'lucide-react';

export default function DocumentListPage() {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showNewFolder, setShowNewFolder] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');

  const fetchFolders = useCallback(async () => {
    setLoading(true);
    try {
      const folderList = await listFolders();
      setFolders(folderList);
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }, [addToast]);

  useEffect(() => {
    fetchFolders();
  }, [fetchFolders]);

  const handleCreateFolder = async (e) => {
    e.preventDefault();
    if (!newFolderName.trim()) return;
    try {
      await createFolder(newFolderName.trim());
      addToast(`Folder "${newFolderName.trim()}" created`, 'success');
      setNewFolderName('');
      setShowNewFolder(false);
      fetchFolders();
    } catch (err) {
      addToast(err.message || 'Failed to create folder', 'error');
    }
  };

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Documents</h1>
          <p className="page-subtitle">
            {folders.length > 0 ? `${folders.length} folder${folders.length !== 1 ? 's' : ''} total` : 'Browse folders to manage documents'}
          </p>
        </div>
        <button onClick={() => setShowNewFolder(!showNewFolder)} className="btn btn-primary">
          <Plus size={16} />
          New Folder
        </button>
      </div>

      {/* New folder form */}
      {showNewFolder && (
        <form onSubmit={handleCreateFolder} className="card p-4 mb-6">
          <div className="flex gap-3">
            <input
              type="text"
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
              placeholder="Folder name"
              className="input flex-1"
              autoFocus
            />
            <button type="submit" disabled={!newFolderName.trim()} className="btn btn-primary">
              Create
            </button>
            <button type="button" onClick={() => { setShowNewFolder(false); setNewFolderName(''); }} className="btn btn-ghost">
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* Search bar */}
      <div className="mb-4">
        <SearchBar compact />
      </div>

      {/* Content */}
      {loading ? (
        <div className="loading-center">
          <div className="spinner" />
          <p>Loading folders\u2026</p>
        </div>
      ) : folders.length === 0 ? (
        <div className="card p-12">
          <div className="empty-state">
            <Inbox size={48} className="text-slate-300 mb-2" />
            <h3 className="empty-state-title">No folders yet</h3>
            <p className="empty-state-desc">
              Create a folder to start organizing documents.
            </p>
            <button onClick={() => setShowNewFolder(true)} className="btn btn-primary mt-4">
              <Plus size={16} />
              New Folder
            </button>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {folders.map((f) => (
            <button
              key={f.id}
              onClick={() => navigate(`/folders/${f.id}`)}
              className="card p-4 text-left hover:shadow-md hover:border-primary-300 transition-all group"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-primary-50 text-primary-600 flex items-center justify-center shrink-0 group-hover:bg-primary-100 transition-colors">
                  <Folder size={20} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-700 truncate">{f.name}</p>
                  <p className="text-xs text-slate-400 truncate">{f.department}</p>
                </div>
                <ChevronRight size={16} className="text-slate-300 shrink-0 group-hover:text-primary-500 transition-colors" />
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
