import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getFolder } from '../api/folders.api';
import { listDocuments, createDocument } from '../api/documents.api';
import { useToast } from '../context/ToastContext';
import { ArrowLeft, FileText, Share2, Plus, Upload, Check, X as XIcon } from 'lucide-react';
import { apiFetch } from '../api/client';
import UploadModal from '../components/UploadModal';

export default function AdminFolderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [folder, setFolder] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [creating, setCreating] = useState(false);

  const loadData = async () => {
    try {
      const [folderData, docsData] = await Promise.all([
        getFolder(id),
        listDocuments({ folderId: id })
      ]);
      setFolder(folderData);
      setDocuments(docsData.items || []);
    } catch (err) {
      addToast('Failed to load folder details', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (e, docId) => {
    e.stopPropagation();
    try {
      await apiFetch('/approval/approve', {
        method: 'POST',
        body: { documentId: docId }
      });
      addToast('Document approved', 'success');
      loadData();
    } catch (err) {
      addToast('Failed to approve: ' + err.message, 'error');
    }
  };

  const handleReject = async (e, docId) => {
    e.stopPropagation();
    const reason = window.prompt("Enter rejection reason:");
    if (reason === null) return;
    try {
      await apiFetch('/approval/reject', {
        method: 'POST',
        body: { documentId: docId, reason: reason || 'No reason specified' }
      });
      addToast('Document rejected', 'success');
      loadData();
    } catch (err) {
      addToast('Failed to reject: ' + err.message, 'error');
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const handleCreateEmpty = async () => {
    const title = prompt('Enter document title:');
    if (!title) return;

    setCreating(true);
    try {
      const doc = await createDocument({ title, type: 'TEXT', folderId: id });
      addToast('Document created', 'success');
      navigate(`/admin/documents/${doc.id}`); // This goes to the editor which Admin has full access to
    } catch (err) {
      addToast(err.message || 'Failed to create document', 'error');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/admin/folders')} className="p-2 rounded-lg hover:bg-slate-100 text-slate-500">
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-slate-800">
              {folder ? folder.name : 'Loading...'}
            </h1>
            <p className="text-sm text-slate-500">Folder Documents</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={() => setShowUploadModal(true)} className="btn btn-secondary">
            <Upload size={16} /> Upload File
          </button>
          <button onClick={handleCreateEmpty} disabled={creating} className="btn btn-primary">
            {creating ? <div className="spinner spinner-sm" /> : <Plus size={16} />}
            Create Empty Doc
          </button>
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
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Document</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase">Status</th>
                  <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {documents.map((doc) => (
                  <tr key={doc.id} onClick={() => navigate(`/admin/documents/${doc.id}`)} className="hover:bg-slate-50 hover:cursor transition-colors">
                    <td className="px-6 py-4 cursor-pointer" >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded bg-primary-50 text-primary-600 flex items-center justify-center">
                          <FileText size={16} />
                        </div>
                        <span className="font-medium text-slate-800 hover:text-primary-600">{doc.title}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`badge ${doc.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                        doc.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                          doc.status === 'PENDING' ? 'bg-amber-100 text-amber-700' :
                            'bg-slate-100 text-slate-700'
                        }`}>
                        {doc.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right flex justify-end gap-2">
                      {doc.status === 'PENDING' && (
                        <>
                          <button
                            onClick={(e) => handleApprove(e, doc.id)}
                            className="btn btn-sm bg-green-50 text-green-600 hover:bg-green-100 border-transparent"
                            title="Approve Document"
                          >
                            <Check size={14} /> Approve
                          </button>
                          <button
                            onClick={(e) => handleReject(e, doc.id)}
                            className="btn btn-sm bg-red-50 text-red-600 hover:bg-red-100 border-transparent"
                            title="Reject Document"
                          >
                            <XIcon size={14} /> Reject
                          </button>
                        </>
                      )}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/admin/documents/${doc.id}/permissions`);
                        }}
                        className="btn btn-secondary btn-sm"
                        title="Share / Manage Permissions"
                      >
                        <Share2 size={14} /> Share
                      </button>
                    </td>
                  </tr>
                ))}
                {documents.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-6 py-8 text-center text-slate-500">
                      No documents in this folder.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showUploadModal && (
        <UploadModal
          onClose={() => setShowUploadModal(false)}
          onSuccess={() => {
            setShowUploadModal(false);
            loadData();
          }}
          folderId={id}
        />
      )}
    </div>
  );
}
