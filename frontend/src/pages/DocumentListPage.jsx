import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import * as documentsApi from '../api/documents.api';
import { useToast } from '../context/ToastContext';
import UploadModal from '../components/UploadModal';
import SearchBar from '../components/SearchBar';
import {
  FileText,
  Plus,
  Trash2,
  MoreHorizontal,
  Inbox,
} from 'lucide-react';

const STATUS_BADGE = {
  DRAFT: 'badge badge-draft',
  PENDING: 'badge badge-pending',
  APPROVED: 'badge badge-approved',
  REJECTED: 'badge badge-rejected',
};

export default function DocumentListPage() {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [total, setTotal] = useState(0);
  const [showUpload, setShowUpload] = useState(false);
  const [selectedDoc, setSelectedDoc] = useState(null);
  const [actionMenu, setActionMenu] = useState(null);
  const limit = 10;

  const fetchDocuments = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, limit, sortBy: 'updatedAt', sortOrder: 'desc' };
      const result = await documentsApi.listDocuments(params);
      setDocuments(result.items);
      setTotalPages(result.totalPages);
      setTotal(result.total);
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }, [page, addToast]);

  useEffect(() => {
    fetchDocuments();
  }, [fetchDocuments]);

  const handleDelete = async (doc) => {
    if (!window.confirm(`Delete "${doc.title}"? This action cannot be undone.`)) return;
    try {
      await documentsApi.deleteDocument(doc.id);
      addToast(`"${doc.title}" deleted`, 'success');
      fetchDocuments();
    } catch (err) {
      addToast(err.message, 'error');
    }
    setActionMenu(null);
  };

  const handleUploadComplete = () => {
    setShowUpload(false);
    fetchDocuments();
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Documents</h1>
          <p className="text-sm text-slate-500 mt-1">
            {total > 0 ? `${total} document${total !== 1 ? 's' : ''} total` : 'Manage your documents'}
          </p>
        </div>
        <button onClick={() => setShowUpload(true)} className="btn btn-primary">
          <Plus size={16} />
          Upload
        </button>
      </div>

      {/* Search bar */}
      <div className="mb-4">
        <SearchBar compact />
      </div>

      {/* Content */}
      {loading ? (
        <div className="loading-center">
          <div className="spinner" />
          <p>Loading documents\u2026</p>
        </div>
      ) : documents.length === 0 ? (
        <div className="card p-12">
          <div className="empty-state">
            <Inbox size={48} className="text-slate-300 mb-2" />
            <h3 className="empty-state-title">No documents yet</h3>
            <p className="empty-state-desc">
              Upload your first document to get started.
            </p>
            <button onClick={() => setShowUpload(true)} className="btn btn-primary mt-4">
              <Plus size={16} />
              Upload Document
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* Table */}
          <div className="card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50/50">
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">
                      Name
                    </th>
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3 hidden sm:table-cell">
                      Type
                    </th>
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">
                      Status
                    </th>
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3 hidden md:table-cell">
                      Updated
                    </th>
                    <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3 w-16">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {documents.map((doc) => (
                    <tr
                      key={doc.id}
                      className="hover:bg-slate-50 transition-colors cursor-pointer"
                      onClick={() => navigate(`/documents/${doc.id}`)}
                    >
                      <td className="px-4 py-3.5">
                        <div className="flex items-center gap-3">
                          <FileText size={18} className="text-slate-400 shrink-0" />
                          <span className="text-sm font-medium text-slate-700 truncate max-w-[250px]">
                            {doc.title}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3.5 hidden sm:table-cell">
                        <span className="text-sm text-slate-500">{doc.type}</span>
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
                        <div className="relative">
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setActionMenu(actionMenu === doc.id ? null : doc.id);
                              setSelectedDoc(doc);
                            }}
                            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
                            aria-label="Actions"
                          >
                            <MoreHorizontal size={18} />
                          </button>

                          {actionMenu === doc.id && (
                            <>
                              <div className="fixed inset-0 z-10" onClick={() => setActionMenu(null)} />
                              <div className="absolute right-0 top-full mt-1 w-40 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-20">
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    navigate(`/documents/${doc.id}`);
                                  }}
                                  className="flex items-center gap-2 w-full px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                                >
                                  <FileText size={14} />
                                  Open
                                </button>
                                <button
                                  onClick={() => handleDelete(doc)}
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
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-4 text-sm">
              <p className="text-slate-500">
                Page {page} of {totalPages}
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page <= 1}
                  className="btn btn-secondary btn-sm"
                >
                  Previous
                </button>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                  disabled={page >= totalPages}
                  className="btn btn-secondary btn-sm"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Upload Modal */}
      {showUpload && (
        <UploadModal
          onClose={() => setShowUpload(false)}
          onUploaded={handleUploadComplete}
        />
      )}
    </div>
  );
}
