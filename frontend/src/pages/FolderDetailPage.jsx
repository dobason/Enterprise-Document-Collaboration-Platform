import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getFolder, listFolders, createFolder } from '../api/folders.api';
import { listDocuments } from '../api/documents.api';
import { useToast } from '../context/ToastContext';
import {
  ArrowLeft,
  Folder,
  FileText,
  Plus,
  Upload,
  ChevronRight,
  Home,
} from 'lucide-react';

const STATUS_BADGE = {
  DRAFT: 'badge badge-draft',
  PENDING: 'badge badge-pending',
  APPROVED: 'badge badge-approved',
  REJECTED: 'badge badge-rejected',
};

export default function FolderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [folder, setFolder] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showNewFolder, setShowNewFolder] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [folderData, allDocuments, allFolders] = await Promise.all([
          getFolder(id),
          listDocuments({ folderId: id }),
          listFolders(),
        ]);

        if (cancelled) return;
        setFolder(folderData);
        setDocuments(allDocuments.items);
        setFolders(allFolders);
      } catch (err) {
        if (!cancelled) {
          addToast('Failed to load folder: ' + err.message, 'error');
          navigate('/documents');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [id, addToast, navigate]);

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) return;
    try {
      await createFolder(newFolderName.trim());
      addToast(`Folder "${newFolderName}" created`, 'success');
      setNewFolderName('');
      setShowNewFolder(false);

      const allFolders = await listFolders();
      setFolders(allFolders);
    } catch (err) {
      addToast('Failed to create folder: ' + err.message, 'error');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading folder\u2026</p>
      </div>
    );
  }

  if (!folder) {
    return (
      <div className="empty-state">
        <h3 className="empty-state-title">Folder not found</h3>
        <button onClick={() => navigate('/documents')} className="btn btn-primary mt-4">Back to Documents</button>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm text-slate-400 mb-4">
        <button onClick={() => navigate('/documents')} className="hover:text-slate-600 transition-colors">
          <Home size={14} />
        </button>
        <ChevronRight size={14} />
        <span className="text-slate-700 font-medium">{folder.name}</span>
      </div>

      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Folder size={28} className="text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-slate-800">{folder.name}</h1>
            <p className="text-sm text-slate-500">
              {folder.department} &middot; Created {formatDate(folder.createdAt)}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setShowNewFolder(!showNewFolder)}
            className="btn btn-secondary"
          >
            <Plus size={16} />
            New Folder
          </button>
        </div>
      </div>

      {/* New folder form */}
      {showNewFolder && (
        <div className="card p-4 mb-6">
          <div className="flex gap-3">
            <input
              type="text"
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCreateFolder()}
              placeholder="Folder name"
              className="input flex-1"
              autoFocus
            />
            <button onClick={handleCreateFolder} disabled={!newFolderName.trim()} className="btn btn-primary">
              Create
            </button>
            <button onClick={() => { setShowNewFolder(false); setNewFolderName(''); }} className="btn btn-ghost">
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* Documents in folder */}
      <div className="card overflow-hidden">
        <div className="px-4 py-3 border-b border-slate-100 bg-slate-50/50">
          <h3 className="text-sm font-semibold text-slate-700">Documents in this folder</h3>
        </div>

        {documents.length === 0 ? (
          <div className="p-12">
            <div className="empty-state">
              <FileText size={40} className="text-slate-300" />
              <h3 className="empty-state-title">No documents in this folder</h3>
              <p className="empty-state-desc">Upload documents to this folder to organize them.</p>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-slate-100">
                  <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">Name</th>
                  <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">Status</th>
                  <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3 hidden md:table-cell">Updated</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {documents.map((doc) => (
                  <tr
                    key={doc.id}
                    onClick={() => navigate(`/documents/${doc.id}`)}
                    className="hover:bg-slate-50 transition-colors cursor-pointer"
                  >
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-3">
                        <FileText size={18} className="text-slate-400 shrink-0" />
                        <span className="text-sm font-medium text-slate-700 truncate max-w-[300px]">{doc.title}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      <span className={STATUS_BADGE[doc.status] || 'badge'}>{doc.status}</span>
                    </td>
                    <td className="px-4 py-3.5 hidden md:table-cell">
                      <span className="text-sm text-slate-500">{formatDate(doc.updatedAt)}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Other folders */}
      <div className="mt-8">
        <h3 className="text-sm font-semibold text-slate-700 mb-3">All Folders</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {folders.map((f) => (
            <button
              key={f.id}
              onClick={() => navigate(`/folders/${f.id}`)}
              className={`card p-4 text-left hover:shadow-md transition-shadow ${
                f.id === id ? 'ring-2 ring-primary-200 bg-primary-50/30' : ''
              }`}
            >
              <div className="flex items-center gap-3">
                <Folder size={20} className={f.id === id ? 'text-primary-600' : 'text-slate-400'} />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-700 truncate">{f.name}</p>
                  <p className="text-xs text-slate-400">{f.department}</p>
                </div>
              </div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
