import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { EditorState, convertFromRaw, convertToRaw, ContentState, convertFromHTML } from 'draft-js';
import { getDocument, updateDocument } from '../api/documents.api';
import { getVersions, createVersion } from '../api/versions.api';
import { approveDocument, rejectDocument, submitForApproval } from '../api/approval.api';
import { getUserRole } from '../api/permissions.api';
import { getToken } from '../api/client';
import { CONFIG } from '../api/config';
import mammoth from 'mammoth/mammoth.browser';
import * as XLSX from 'xlsx';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import RichTextEditor from '../components/RichTextEditor';
import VersionPanel from '../components/VersionPanel';
import TagManager from '../components/TagManager';
import ExportDropdown from '../components/ExportDropdown';
import {
  ArrowLeft,
  Save,
  Clock,
  CheckCircle2,
  Loader2,
  PanelRightOpen,
  PanelRightClose,
  File,
  Download,
  ThumbsUp,
  ThumbsDown,
  Send,
  History,
} from 'lucide-react';

export default function DocumentEditorPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [doc, setDoc] = useState(null);
  const [editorState, setEditorState] = useState(EditorState.createEmpty());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveStatus, setSaveStatus] = useState({ text: '', type: '' });
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [versions, setVersions] = useState([]);
  const [isViewer, setIsViewer] = useState(false);
  const [isEditor, setIsEditor] = useState(false);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [fileUrl, setFileUrl] = useState(null);
  const [fileHtml, setFileHtml] = useState(null);
  const [previewKind, setPreviewKind] = useState(null);
  const [fileLoadFailed, setFileLoadFailed] = useState(false);
  const [saveText, setSaveText] = useState('');
  const [saveType, setSaveType] = useState('');
  const contentRef = useRef(null);
  const lastSavedContentRef = useRef(null);
  const saveIntervalRef = useRef(null);
  const titleRef = useRef(null);

  // Load document
  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const document = await getDocument(id);
        if (cancelled) return;
        setDoc(document);

        if (document.fileName) {
          setUploadedFile({
            id: document.id,
            name: document.fileName,
            type: document.fileType || 'application/octet-stream',
          });
          renderFile(document.id, document.fileName, document.fileType);
        }

        // Check user permission
        if (user) {
          const role = await getUserRole(id, user.id);
          setIsViewer(role === 'VIEWER');
          setIsEditor(role === 'EDITOR' || role === 'OWNER' || role === 'MANAGER' || role === 'ADMIN');
          if (!role) {
            // Default: owner by document's ownerId
            setIsViewer(document.ownerId !== user.id);
            setIsEditor(document.ownerId === user.id);
          }
        }

        // Parse content JSON into EditorState
        try {
          const raw = JSON.parse(document.content || '{}');
          if (raw.blocks && raw.blocks.length > 0) {
            const contentState = convertFromRaw(raw);
            const state = EditorState.createWithContent(contentState);
            setEditorState(state);
            const saved = JSON.stringify(convertToRaw(state.getCurrentContent()));
            contentRef.current = saved;
            lastSavedContentRef.current = saved;
          } else {
            throw new Error('Empty JSON blocks');
          }
        } catch {
          // Fallback to HTML parser if JSON parse fails
          const blocksFromHTML = convertFromHTML(document.content || '');
          if (blocksFromHTML.contentBlocks && blocksFromHTML.contentBlocks.length > 0) {
            const contentState = ContentState.createFromBlockArray(
              blocksFromHTML.contentBlocks,
              blocksFromHTML.entityMap
            );
            const state = EditorState.createWithContent(contentState);
            setEditorState(state);
            const saved = JSON.stringify(convertToRaw(state.getCurrentContent()));
            contentRef.current = saved;
            lastSavedContentRef.current = saved;
          } else {
            // Fallback to plain text stripping HTML completely
            const stripHtml = (html) => {
              if (!html) return '';
              let withNewlines = html.replace(/<\/(p|div|h[1-6]|li)>/ig, '\n');
              withNewlines = withNewlines.replace(/<br\s*[\/]?>/ig, '\n');
              const tmp = window.document.createElement('DIV');
              tmp.innerHTML = withNewlines;
              return (tmp.textContent || tmp.innerText || '').trim();
            };
            const cleanText = stripHtml(document.content || '');
            const plainTextContent = ContentState.createFromText(cleanText);
            const state = EditorState.createWithContent(plainTextContent);
            setEditorState(state);
            const saved = JSON.stringify(convertToRaw(state.getCurrentContent()));
            contentRef.current = saved;
            lastSavedContentRef.current = saved;
          }
        }

        // Load versions
        const docVersions = await getVersions(id);
        if (!cancelled) {
          setVersions(docVersions);
        }
      } catch (err) {
        if (!cancelled) {
          addToast('Failed to load document: ' + err.message, 'error');
          navigate('/documents');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [id, addToast, navigate]);

  const renderFile = useCallback(async (docId, fileName, fileType) => {
    try {
      const res = await fetch(`${CONFIG.API_URL}/documents/${docId}/download`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      if (!res.ok) {
        setFileLoadFailed(true);
        return;
      }
      const blob = await res.blob();
      const ext = (fileName.split('.').pop() || '').toLowerCase();
      const mime = (fileType || '').toLowerCase();

      if (
        mime.startsWith('image/') ||
        ['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp', 'bmp'].includes(ext)
      ) {
        setPreviewKind('image');
        setFileUrl(URL.createObjectURL(blob));
      } else if (mime === 'application/pdf' || ext === 'pdf') {
        setPreviewKind('iframe');
        setFileUrl(URL.createObjectURL(blob));
      } else if (
        mime.startsWith('text/') ||
        ['txt', 'md', 'csv', 'log', 'json', 'xml', 'js', 'css', 'html'].includes(ext)
      ) {
        setPreviewKind('iframe');
        setFileUrl(URL.createObjectURL(blob));
      } else if (ext === 'docx') {
        const arrayBuffer = await blob.arrayBuffer();
        const { value } = await mammoth.convertToHtml({ arrayBuffer });
        setPreviewKind('html');
        setFileHtml(value);
      } else if (ext === 'xlsx' || ext === 'xls') {
        const arrayBuffer = await blob.arrayBuffer();
        const workbook = XLSX.read(arrayBuffer, { type: 'array' });
        const sheet = workbook.Sheets[workbook.SheetNames[0]];
        setPreviewKind('html');
        setFileHtml(XLSX.utils.sheet_to_html(sheet));
      } else {
        setFileLoadFailed(true);
      }
    } catch {
      setFileLoadFailed(true);
    }
  }, []);

  useEffect(() => {
    return () => {
      if (fileUrl) URL.revokeObjectURL(fileUrl);
    };
  }, [fileUrl]);

  const handleDownload = useCallback(async () => {
    const targetId = uploadedFile?.id || doc?.id;
    if (!targetId) return;

    // Nếu doc có file upload .docx/.doc -> export nội dung editor thành text
    if (uploadedFile?.name?.endsWith('.docx') || uploadedFile?.name?.endsWith('.doc')) {
      const text = editorState.getCurrentContent().getPlainText('\n');
      const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = uploadedFile.name.replace(/\.docx?$/, '.txt');
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      return;
    }

    try {
      const res = await fetch(`${CONFIG.API_URL}/documents/${targetId}/download`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      if (!res.ok) throw new Error(`Download failed: HTTP ${res.status}`);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = uploadedFile?.name || `${doc?.title || 'document'}.txt`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      addToast('Download failed: ' + err.message, 'error');
    }
  }, [uploadedFile, doc, editorState, addToast]);

  const getContentJSON = useCallback(() => {
    const contentState = contentRef.current
      ? convertFromRaw(JSON.parse(contentRef.current))
      : editorState.getCurrentContent();
    const raw = convertToRaw(contentState);
    return JSON.stringify(raw);
  }, [editorState]);

  // Keep contentRef in sync with editorState
  useEffect(() => {
    try {
      const raw = convertToRaw(editorState.getCurrentContent());
      contentRef.current = JSON.stringify(raw);
    } catch {}
  }, [editorState]);

  const handleSave = useCallback(async (auto = false) => {
    if (!doc || saving) return;

    const currentContent = contentRef.current || getContentJSON();
    if (currentContent === lastSavedContentRef.current) {
      if (auto) return;
      setSaveText('No changes to save');
      setSaveType('info');
      setTimeout(() => { setSaveText(''); setSaveType(''); }, 2000);
      return;
    }

    setSaving(true);
    if (!auto) { setSaveText('Saving\u2026'); setSaveType('saving'); }

    try {
      const version = await createVersion(
        doc.id,
        currentContent,
        user?.id
      );

      await updateDocument(doc.id, {
        content: currentContent,
        currentVersionId: version.id,
      });

      lastSavedContentRef.current = currentContent;

      const docVersions = await getVersions(doc.id);
      setVersions(docVersions);

      const vNum = version.versionNumber;
      setSaveText(`Saved v${vNum}`);
      setSaveType('saved');
      setTimeout(() => { setSaveText(''); setSaveType(''); }, 3000);
      if (!auto) addToast(`Version ${vNum} saved`, 'success');
    } catch (err) {
      setSaveText('Save failed');
      setSaveType('error');
      addToast('Save failed: ' + err.message, 'error');
    } finally {
      setSaving(false);
    }
  }, [doc, saving, user, addToast, getContentJSON]);

  // Auto-save every 30s
  const handleSaveRef = useRef(handleSave);

  useEffect(() => {
    handleSaveRef.current = handleSave;
  }, [handleSave]);

  useEffect(() => {
    if (!doc || loading || uploadedFile) return;

    saveIntervalRef.current = setInterval(() => {
      handleSaveRef.current(true);
    }, 30000);

    return () => {
      if (saveIntervalRef.current) {
        clearInterval(saveIntervalRef.current);
      }
    };
  }, [doc, loading, uploadedFile]);

  const handleEditorChange = (state) => {
    setEditorState(state);
  };

  const handleTitleChange = async (e) => {
    const newTitle = e.target.value;
    setDoc((prev) => ({ ...prev, title: newTitle }));
  };

  const handleTitleBlur = async () => {
    if (doc) {
      await updateDocument(doc.id, { title: doc.title });
    }
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading document\u2026</p>
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="empty-state">
        <h3 className="empty-state-title">Document not found</h3>
        <button onClick={() => navigate(-1)} className="btn btn-primary mt-4">
          Back to Documents
        </button>
      </div>
    );
  }


  const saveStatusColor = {
    saving: 'text-primary-600',
    saved: 'text-green-600',
    error: 'text-red-600',
    info: 'text-slate-400',
  };

  return (
    <div className="flex flex-col h-full">
      {/* Toolbar */}
      <div className="flex items-center gap-3 mb-4 shrink-0">
        <button
          onClick={() => navigate(-1)}
          className="p-2 rounded-lg hover:bg-slate-100 text-slate-500"
          aria-label="Back to documents"
        >
          <ArrowLeft size={18} aria-hidden="true" />
        </button>

        <label htmlFor="doc-title-input" className="sr-only">Document title</label>
        <input
          id="doc-title-input"
          ref={titleRef}
          type="text"
          value={doc.title}
          onChange={handleTitleChange}
          onBlur={handleTitleBlur}
          className="flex-1 text-lg font-semibold bg-transparent border-none outline-none text-slate-800 px-2 py-1 rounded hover:bg-slate-100 focus:bg-white focus:ring-2 focus:ring-primary-200"
          readOnly={isViewer}
        />

        <div className="flex items-center gap-2">
          {/* Save status */}
          {saveText && (
            <span className={`text-xs font-medium flex items-center gap-1 ${saveStatusColor[saveType] || 'text-slate-400'}`}>
              {saveType === 'saving' && <Loader2 size={12} className="animate-spin" aria-hidden="true" />}
              {saveType === 'saved' && <CheckCircle2 size={12} aria-hidden="true" />}
              {saveType === 'error' && <Clock size={12} aria-hidden="true" />}
              {saveText}
            </span>
          )}

          {doc.status && (
            <span className={`badge ml-2 ${
              doc.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
              doc.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
              doc.status === 'PENDING' ? 'bg-amber-100 text-amber-700' :
              'bg-slate-100 text-slate-700'
            }`}>
              {doc.status}
            </span>
          )}

          {doc.status === 'DRAFT' && (user?.role === 'ADMIN' || !isViewer) && (
            <button
              onClick={async () => {
                try {
                  const updatedDoc = await submitForApproval(doc.id);
                  setDoc(updatedDoc);
                  addToast('Document submitted for approval', 'success');
                } catch (e) {
                  addToast(e.message || 'Submit failed', 'error');
                }
              }}
              className="btn btn-primary"
            >
              <Send size={16} /> Submit for Approval
            </button>
          )}

          {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && doc.status === 'PENDING' && (
            <>
              <button
                onClick={async () => {
                  try {
                    const updatedDoc = await approveDocument(doc.id);
                    setDoc(updatedDoc);
                    addToast('Document approved', 'success');
                  } catch (e) {
                    addToast(e.message || 'Approval failed', 'error');
                  }
                }}
                className="btn btn-secondary text-green-600 hover:bg-green-50"
              >
                <ThumbsUp size={16} /> Approve
              </button>
              <button
                onClick={async () => {
                  const reason = prompt('Reason for rejection:');
                  if (!reason) return;
                  try {
                    const updatedDoc = await rejectDocument(doc.id, reason);
                    setDoc(updatedDoc);
                    addToast('Document rejected', 'success');
                  } catch (e) {
                    addToast(e.message || 'Rejection failed', 'error');
                  }
                }}
                className="btn btn-secondary text-red-600 hover:bg-red-50"
              >
                <ThumbsDown size={16} /> Reject
              </button>
            </>
          )}

          {/* Download button - mọi loại doc */}
          <button
            onClick={handleDownload}
            className="btn btn-secondary"
            title="Download document"
          >
            <Download size={16} />
            Download
          </button>

          {/* Save button - hiện khi user có quyền sửa (không phải viewer) */}
          {!isViewer && (
            <button
              onClick={() => handleSave(false)}
              disabled={saving}
              className="btn btn-primary"
            >
              <Save size={16} />
              Save
            </button>
          )}

          {/* Toggle sidebar */}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 rounded-lg hover:bg-slate-100 text-slate-500"
            aria-label={sidebarOpen ? 'Close sidebar' : 'Open sidebar'}
          >
            {sidebarOpen ? <PanelRightClose size={18} aria-hidden="true" /> : <PanelRightOpen size={18} aria-hidden="true" />}
          </button>
        </div>
      </div>

      {/* Editor + Sidebar */}
      <div className="flex gap-4 flex-1 min-h-0">
        {/* Main editor */}
        <div className="flex-1 overflow-y-auto">
          {uploadedFile && (
            <div className="card p-6 mb-6">
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-xl bg-primary-50 flex items-center justify-center shrink-0">
                  <File size={28} className="text-primary-600" aria-hidden="true" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="text-lg font-semibold text-slate-800 truncate">{uploadedFile.name}</h3>
                  <p className="text-sm text-slate-400">{uploadedFile.type}</p>
                </div>
              </div>

              {fileUrl && previewKind === 'image' ? (
                <div className="mt-6">
                  <img
                    src={fileUrl}
                    alt={uploadedFile.name}
                    className="max-h-[60vh] rounded-lg border border-slate-200"
                  />
                </div>
              ) : fileUrl && previewKind === 'iframe' ? (
                <div className="mt-6">
                  <iframe
                    src={fileUrl}
                    title={uploadedFile.name}
                    className="w-full h-[65vh] rounded-lg border border-slate-200 bg-white"
                  />
                </div>
              ) : fileHtml && previewKind === 'html' ? (
                <div
                  className="mt-6 overflow-auto rounded-lg border border-slate-200 bg-white p-6 max-h-[65vh]"
                  dangerouslySetInnerHTML={{ __html: fileHtml }}
                />
              ) : fileLoadFailed ? (
                <div className="mt-6 p-10 text-center text-sm text-slate-400">
                  Cannot preview this file type. Use Download to view it.
                </div>
              ) : (
                <div className="mt-6 p-10 text-center text-sm text-slate-400">Loading preview...</div>
              )}
            </div>
          )}

          <RichTextEditor
            editorState={editorState}
            onChange={handleEditorChange}
            readOnly={isViewer}
            placeholder={uploadedFile ? "Notes or extracted text..." : "Start writing your document..."}
          />
        </div>

        {/* Right sidebar */}
        {sidebarOpen && (
          <div className="w-72 shrink-0 space-y-4 overflow-y-auto">
            <VersionPanel
              documentId={doc.id}
              versions={versions}
              onRollback={(newVersion) => {
                // Reload editor content on rollback
                if (newVersion) {
                  try {
                    const raw = JSON.parse(newVersion.content || '{}');
                    if (raw.blocks) {
                      const contentState = convertFromRaw(raw);
                      setEditorState(EditorState.createWithContent(contentState));
                    }
                  } catch {}
                }
              }}
            />
            {/* <TagManager documentId={doc.id} /> */}

            {/* View all versions link */}
            <button
              onClick={() => {
                const basePath = window.location.pathname.startsWith('/admin') ? '/admin/documents' : '/documents';
                navigate(`${basePath}/${doc.id}/versions`);
              }}
              className="w-full text-xs text-primary-600 hover:text-primary-700 font-medium py-2 text-center"
            >
              View full version history
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
