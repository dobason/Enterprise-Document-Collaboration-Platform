import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { EditorState, convertFromRaw, convertToRaw } from 'draft-js';
import { getDocument, updateDocument } from '../api/documents.api';
import { getVersions, createVersion } from '../api/versions.api';
import { getUserRole } from '../api/permissions.api';
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
            contentRef.current = document.content;
            lastSavedContentRef.current = document.content;
          }
        } catch {
          setEditorState(EditorState.createEmpty());
          contentRef.current = '';
          lastSavedContentRef.current = '';
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
    if (!doc || loading) return;

    saveIntervalRef.current = setInterval(() => {
      handleSaveRef.current(true);
    }, 30000);

    return () => {
      if (saveIntervalRef.current) {
        clearInterval(saveIntervalRef.current);
      }
    };
  }, [doc, loading]);

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
        <button onClick={() => navigate('/documents')} className="btn btn-primary mt-4">
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
          onClick={() => navigate('/documents')}
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

          {/* Save button */}
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
          <RichTextEditor
            editorState={editorState}
            onChange={handleEditorChange}
            readOnly={isViewer}
            placeholder="Start writing your document..."
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
            <TagManager documentId={doc.id} />

            {/* View all versions link */}
            <button
              onClick={() => navigate(`/documents/${doc.id}/versions`)}
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
