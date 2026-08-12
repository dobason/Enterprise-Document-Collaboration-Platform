import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getFolder, listFolders, createFolder, deleteFolder } from '../api/folders.api';
import { listDocuments, deleteDocument, updateDocument } from '../api/documents.api';
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
  MoreVertical,
  ArrowUp,
  Image,
  Shield,
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
  const [showUpload, setShowUpload] = useState(false);
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

  const handleDeleteFolder = async (folderId, folderName, e) => {
    e.stopPropagation();
    if (!window.confirm(`Delete folder "${folderName}"? This action cannot be undone.`)) return;
    try {
      await deleteFolder(folderId);
      addToast(`Folder "${folderName}" deleted`, 'success');
      if (folderId === id) {
        navigate('/documents');
      } else {
        const allFolders = await listFolders();
        setFolders(allFolders);
      }
    } catch (err) {
      addToast('Failed to delete folder: ' + err.message, 'error');
    }
  };

  const handleDeleteDocument = async (docId, docTitle, e) => {
    e.stopPropagation();
    if (!window.confirm(`Delete "${docTitle}"? This action cannot be undone.`)) return;
    try {
      await deleteDocument(docId);
      addToast(`"${docTitle}" deleted`, 'success');
      const allDocuments = await listDocuments({ folderId: id });
      setDocuments(allDocuments.items);
    } catch (err) {
      addToast('Failed to delete document: ' + err.message, 'error');
    }
  };

  const toggleFolderMenu = (folderId, e) => {
    e.stopPropagation();
    setFolders(prev => prev.map(f => f.id === folderId ? { ...f, showMenu: !f.showMenu } : { ...f, showMenu: false }));
  };

  const toggleDocumentMenu = (docId, e) => {
    e.stopPropagation();
    setDocuments(prev => prev.map(d => d.id === docId ? { ...d, showMenu: !d.showMenu } : { ...d, showMenu: false }));
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  };

  const getFileIcon = (doc) => {
    const title = (doc.title || '').toLowerCase();
    const type = (doc.type || '').toLowerCase();
    if (type.includes('budget') || title.includes('budget') || title.includes('xlsx') || title.includes('csv')) {
      return { icon: FileText, color: 'text-emerald-600', bg: 'bg-emerald-50' };
    }
    if (type.includes('architecture') || title.includes('architecture') || title.includes('specification') || title.includes('doc')) {
      return { icon: FileText, color: 'text-blue-600', bg: 'bg-blue-50' };
    }
    if (title.includes('drawio') || title.includes('diagram')) {
      return { icon: FileText, color: 'text-orange-600', bg: 'bg-orange-50' };
    }
    if (title.includes('png') || title.includes('jpg') || title.includes('jpeg') || title.includes('image')) {
      return { icon: Image, color: 'text-red-600', bg: 'bg-red-50' };
    }
    if (title.includes('trắc nghiệm') || title.includes('form') || title.includes('survey')) {
      return { icon: FileText, color: 'text-purple-600', bg: 'bg-purple-50' };
    }
    return { icon: FileText, color: 'text-slate-500', bg: 'bg-slate-50' };
  };

  const renderFilePreview = (doc) => {
    const title = (doc.title || '').toLowerCase();
    const type = (doc.type || '').toLowerCase();
    if (type.includes('budget') || title.includes('budget') || title.includes('xlsx') || title.includes('csv')) {
      return (
        <div className="w-full h-full flex flex-col gap-1.5 p-2 bg-white border border-slate-100 rounded shadow-sm">
          <div className="h-3 bg-emerald-50 rounded w-2/3" />
          <div className="grid grid-cols-3 gap-1">
            <div className="h-2 bg-slate-100 rounded" />
            <div className="h-2 bg-slate-100 rounded" />
            <div className="h-2 bg-slate-100 rounded" />
          </div>
          <div className="grid grid-cols-3 gap-1">
            <div className="h-2 bg-slate-50 rounded" />
            <div className="h-2 bg-slate-50 rounded" />
            <div className="h-2 bg-slate-50 rounded" />
          </div>
          <div className="grid grid-cols-3 gap-1">
            <div className="h-2 bg-slate-50 rounded" />
            <div className="h-2 bg-slate-50 rounded" />
            <div className="h-2 bg-slate-50 rounded" />
          </div>
        </div>
      );
    }
    if (type.includes('architecture') || title.includes('architecture') || title.includes('specification') || title.includes('doc')) {
      return (
        <div className="w-full h-full flex flex-col gap-2 p-2 bg-white border border-slate-100 rounded shadow-sm">
          <div className="h-3 bg-blue-50 rounded w-1/2" />
          <div className="h-2 bg-slate-100 rounded w-full" />
          <div className="h-2 bg-slate-100 rounded w-5/6" />
          <div className="h-2 bg-slate-100 rounded w-4/5" />
        </div>
      );
    }
    if (title.includes('drawio') || title.includes('diagram')) {
      return (
        <div className="w-full h-full flex items-center justify-center gap-2 p-2 bg-white border border-slate-100 rounded shadow-sm">
          <div className="w-8 h-8 rounded border-2 border-orange-200 bg-orange-50/50 flex items-center justify-center" />
          <div className="w-4 h-0.5 bg-slate-300" />
          <div className="w-8 h-8 rounded-full border-2 border-orange-200 bg-orange-50/50 flex items-center justify-center" />
        </div>
      );
    }
    if (title.includes('png') || title.includes('jpg') || title.includes('jpeg') || title.includes('image')) {
      return (
        <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-red-50 to-orange-50 border border-slate-100 rounded shadow-sm relative overflow-hidden">
          <div className="absolute inset-0 opacity-20 bg-[radial-gradient(#f97316_1px,transparent_1px)] [background-size:8px_8px]" />
          <Image size={24} className="text-red-400" />
        </div>
      );
    }
    if (title.includes('trắc nghiệm') || title.includes('form') || title.includes('survey')) {
      return (
        <div className="w-full h-full flex flex-col gap-2 p-2 bg-white border border-slate-100 rounded shadow-sm">
          <div className="h-3 bg-purple-50 rounded w-3/4" />
          <div className="flex items-center gap-1">
            <div className="w-2.5 h-2.5 rounded-full border border-slate-300" />
            <div className="h-2 bg-slate-100 rounded w-1/2" />
          </div>
          <div className="flex items-center gap-1">
            <div className="w-2.5 h-2.5 rounded-full border border-slate-300" />
            <div className="h-2 bg-slate-100 rounded w-1/3" />
          </div>
        </div>
      );
    }
    return (
      <div className="w-full h-full flex flex-col gap-2 p-2 bg-white border border-slate-100 rounded shadow-sm">
        <div className="h-3 bg-slate-100 rounded w-1/2" />
        <div className="h-2 bg-slate-50 rounded w-full" />
        <div className="h-2 bg-slate-50 rounded w-5/6" />
      </div>
    );
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading folder…</p>
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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      {/* Breadcrumb */}
      {/* <div className="flex items-center gap-2 text-sm text-slate-400 mb-4">
        <button onClick={() => navigate('/documents')} className="hover:text-slate-600 transition-colors">
          <Home size={14} />
        </button>
        <ChevronRight size={14} />
        <span className="text-slate-700 font-medium">{folder.name}</span>
      </div> */}

      {/* Header */}
       <div className="flex items-center justify-between mb-6">
        {/* <div className="flex items-center gap-3">
          <Folder size={28} className="text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-slate-800">{folder.name}</h1>
            <p className="text-sm text-slate-500">
              {folder.department} &middot; Created {formatDate(folder.createdAt)}
            </p>
          </div>
        </div>  */}

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


      {/* Folders Section (Image 2 style) */}
      <div className="mb-8">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-semibold text-slate-700">Folders</h3>
          <div className="flex gap-2">
            <button
              onClick={() => setShowUpload(true)}
              className="btn btn-primary"
            >
              <Upload size={16} />
              Upload
            </button>
            <button
              onClick={() => setShowNewFolder(!showNewFolder)}
              className="btn btn-secondary"
            >
              <Plus size={16} />
              New Folder
            </button>
          </div>
        </div>
        {showUpload && (
          <UploadModal
            onClose={() => setShowUpload(false)}
            onUploaded={async (doc) => {
              if (doc && doc.id) {
                try {
                  await updateDocument(doc.id, { folderId: id });
                  const allDocuments = await listDocuments({ folderId: id });
                  setDocuments(allDocuments.items);
                } catch (err) {
                  addToast('Failed to move document to folder: ' + err.message, 'error');
                }
              }
              setShowUpload(false);
            }}
          />
        )}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          {folders.map((f) => (
            <div
              key={f.id}
              onClick={() => navigate(`/folders/${f.id}`)}
              className={`flex items-center justify-between p-3 bg-slate-50 hover:bg-slate-100 border rounded-xl transition-all cursor-pointer relative group ${
                f.id === id ? 'border-primary-500 ring-2 ring-primary-100 bg-primary-50/30' : 'border-slate-200'
              }`}
            >
              <div className="flex items-center gap-3 min-w-0">
                <Folder size={20} className={f.id === id ? 'text-primary-600' : 'text-slate-600'} />
                <span className="text-sm font-medium text-slate-700 truncate">{f.name}</span>
              </div>
              <div className="relative">
                <button
                  onClick={(e) => toggleFolderMenu(f.id, e)}
                  className="p-1 rounded-lg hover:bg-slate-200 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  <MoreVertical size={16} />
                </button>
                {f.showMenu && (
                  <>
                    <div className="fixed inset-0 z-10" onClick={(e) => { e.stopPropagation(); toggleFolderMenu(f.id, e); }} />
                    <div className="absolute right-0 top-full mt-1 w-32 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-20">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/folders/${f.id}`);
                        }}
                        className="flex items-center gap-2 w-full px-3 py-1.5 text-xs text-slate-700 hover:bg-slate-50"
                      >
                        Open
                      </button>
                      <button
                        onClick={(e) => handleDeleteFolder(f.id, f.name, e)}
                        className="flex items-center gap-2 w-full px-3 py-1.5 text-xs text-red-600 hover:bg-red-50"
                      >
                        Delete
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Files Section (Image 2 style) */}
      <div>
        <h3 className="text-sm font-semibold text-slate-700 mb-3">Files</h3>
        {documents.length === 0 ? (
          <div className="card p-12 text-center">
            <FileText size={40} className="text-slate-300 mx-auto mb-2" />
            <h3 className="empty-state-title">No documents in this folder</h3>
            <p className="empty-state-desc">Upload documents to this folder to organize them.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
            {documents.map((doc) => {
              const fileConfig = getFileIcon(doc);
              const IconComponent = fileConfig.icon;
              return (
                <div
                  key={doc.id}
                  onClick={() => navigate(`/documents/${doc.id}`)}
                  className="flex flex-col bg-white border border-slate-200 rounded-xl overflow-hidden hover:shadow-md transition-all cursor-pointer group"
                >
                  {/* Header */}
                  <div className="flex items-center justify-between p-3 border-b border-slate-100 bg-slate-50/50">
                    <div className="flex items-center gap-2 min-w-0">
                      <IconComponent size={18} className={`${fileConfig.color} shrink-0`} />
                      <span className="text-sm font-medium text-slate-700 truncate" title={doc.title}>
                        {doc.title}
                      </span>
                    </div>
                    <div className="relative">
                      <button
                        onClick={(e) => toggleDocumentMenu(doc.id, e)}
                        className="p-1 rounded-lg hover:bg-slate-200 text-slate-400 hover:text-slate-600 transition-colors"
                      >
                        <MoreVertical size={16} />
                      </button>
                      {doc.showMenu && (
                        <>
                          <div className="fixed inset-0 z-10" onClick={(e) => { e.stopPropagation(); toggleDocumentMenu(doc.id, e); }} />
                          <div className="absolute right-0 top-full mt-1 w-32 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-20">
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/documents/${doc.id}`);
                              }}
                              className="flex items-center gap-2 w-full px-3 py-1.5 text-xs text-slate-700 hover:bg-slate-50"
                            >
                              Open
                            </button>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/documents/${doc.id}/permissions`);
                              }}
                              className="flex items-center gap-2 w-full px-3 py-1.5 text-xs text-slate-700 hover:bg-slate-50"
                            >
                              <Shield size={14} />
                              Permissions
                            </button>
                            <button
                              onClick={(e) => handleDeleteDocument(doc.id, doc.title, e)}
                              className="flex items-center gap-2 w-full px-3 py-1.5 text-xs text-red-600 hover:bg-red-50"
                            >
                              Delete
                            </button>
                          </div>
                        </>
                      )}
                    </div>
                  </div>

                  {/* Preview Body */}
                  <div className="flex-1 h-32 bg-slate-50 flex items-center justify-center p-4 relative overflow-hidden">
                    {renderFilePreview(doc)}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
