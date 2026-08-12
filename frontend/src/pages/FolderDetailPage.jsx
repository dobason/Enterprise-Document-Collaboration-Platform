import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getFolder } from '../api/folders.api';
import * as documentsApi from '../api/documents.api';
import { listDocuments } from '../api/documents.api';
import { useToast } from '../context/ToastContext';
import UploadModal from '../components/UploadModal';
import {
  ArrowLeft,
  Folder,
  FileText,
  Plus,
  Upload,
  ChevronRight,
  Home,
  Trash2,
  MoreHorizontal,
  Share2,
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
  const [loading, setLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);
  const [creating, setCreating] = useState(false);
  const [actionMenu, setActionMenu] = useState(null);
  const [menuAnchor, setMenuAnchor] = useState(null);

  const load = async () => {
    try {
      const [folderData, allDocuments] = await Promise.all([
        getFolder(id),
        listDocuments({ folderId: id }),
      ]);
      setFolder(folderData);
      setDocuments(allDocuments.items);
    } catch (err) {
      addToast('Failed to load folder: ' + err.message, 'error');
      navigate('/documents');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    load();
  }, [id]);

  const handleDelete = async (doc) => {
    if (!window.confirm(`Delete "${doc.title}"? This action cannot be undone.`)) return;
    try {
      await documentsApi.deleteDocument(doc.id);
      addToast(`"${doc.title}" deleted`, 'success');
      setActionMenu(null);
      setMenuAnchor(null);
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const handleUploadComplete = () => {
    setShowUpload(false);
    load();
  };

  const handleCreateEmpty = async () => {
    const title = window.prompt('Enter document title:');
    if (!title) return;

    setCreating(true);
    try {
      const doc = await documentsApi.createDocument({ title, type: 'TEXT', folderId: id });
      addToast('Document created', 'success');
      navigate(`/documents/${doc.id}`);
    } catch (err) {
      addToast(err.message || 'Failed to create document', 'error');
    } finally {
      setCreating(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
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
      <div className="page-header">
        <div className="flex items-center gap-3">
          <Folder size={28} className="text-primary-600" />
          <div>
            <h1 className="page-title">{folder.name}</h1>
            <p className="page-subtitle">
              {folder.department} &middot; Created {formatDate(folder.createdAt)}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <button
            onClick={handleCreateEmpty}
            disabled={creating}
            className="btn btn-secondary"
          >
            {creating ? <div className="spinner spinner-sm" /> : <Plus size={16} />}
            Create Empty Doc
          </button>
          <button onClick={() => setShowUpload(true)} className="btn btn-primary">
            <Upload size={16} />
            Upload
          </button>
        </div>
      </div>

      {/* Documents in folder - giống giao diện DocumentListPage */}
      <div className="card overflow-visible">
        <div className={`overflow-x-auto ${actionMenu ? 'overflow-y-visible' : ''}`}>
          <table className="table">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50/50">
                <th className="table-head">
                  Name
                </th>
                <th className="table-head">
                  Owner
                </th>
                <th className="table-head">
                  Status
                </th>
                <th className="table-head hidden md:table-cell">
                  Updated
                </th>
                <th className="table-head text-right w-16">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {documents.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-12 text-center">
                    <FileText size={40} className="text-slate-300 mx-auto mb-2" />
                    <p className="text-slate-500 text-sm">No documents in this folder</p>
                    <button onClick={() => setShowUpload(true)} className="btn btn-primary mt-4">
                      <Upload size={16} />
                      Upload Document
                    </button>
                  </td>
                </tr>
              ) : (
                documents.map((doc) => (
                  <tr
                    key={doc.id}
                    onClick={() => navigate(`/documents/${doc.id}`)}
                    className="hover:bg-slate-50 transition-colors cursor-pointer"
                  >
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-3">
                        <FileText size={18} className="text-slate-400 shrink-0" />
                        <span className="text-sm font-medium text-slate-700 truncate max-w-[250px]">
                          {doc.title}
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      <span className="text-sm text-slate-600 truncate max-w-[180px] block">
                        {doc.ownerName || doc.ownerId || '-'}
                      </span>
                    </td>
                    <td className="px-4 py-3.5">
                      <span className={STATUS_BADGE[doc.status] || 'badge'}>
                        {doc.status}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 hidden md:table-cell">
                      <span className="text-sm text-slate-500">{formatDate(doc.updatedAt)}</span>
                    </td>
                    <td className="px-4 py-3.5 text-right">
                      <div className="relative inline-block">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            const rect = e.currentTarget.getBoundingClientRect();
                            setMenuAnchor({ top: rect.bottom + 4, left: rect.right - 160 });
                            setActionMenu(actionMenu === doc.id ? null : doc.id);
                          }}
                          className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
                          aria-label="Actions"
                        >
                          <MoreHorizontal size={18} />
                        </button>

                        {actionMenu === doc.id && menuAnchor && (
                          <>
                            <div
                              className="fixed inset-0 z-10"
                              onClick={(e) => {
                                e.stopPropagation();
                                setActionMenu(null);
                                setMenuAnchor(null);
                              }}
                            />
                            <div
                              className="fixed z-20 w-40 bg-white rounded-lg shadow-lg border border-slate-200 py-1"
                              style={{ top: menuAnchor.top, left: menuAnchor.left }}
                            >
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setActionMenu(null);
                                  setMenuAnchor(null);
                                  navigate(`/documents/${doc.id}`);
                                }}
                                className="flex items-center gap-2 w-full px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                              >
                                <FileText size={14} />
                                Open
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setActionMenu(null);
                                  setMenuAnchor(null);
                                  navigate(`/documents/${doc.id}/permissions`);
                                }}
                                className="flex items-center gap-2 w-full px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                              >
                                <Share2 size={14} />
                                Share
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDelete(doc);
                                }}
                                className="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                              >
                                <Trash2 size={14} />
                                Delete
                              </button>
                            </div>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showUpload && (
        <UploadModal
          folderId={id}
          onClose={() => setShowUpload(false)}
          onUploaded={handleUploadComplete}
        />
      )}
    </div>
  );
}
