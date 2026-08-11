import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { EditorState, convertFromRaw } from 'draft-js';
import { getDocument } from '../api/documents.api';
import { getVersions, rollbackVersion } from '../api/versions.api';
import { useToast } from '../context/ToastContext';
import RichTextEditor from '../components/RichTextEditor';
import {
  ArrowLeft,
  Clock,
  RotateCcw,
  ChevronRight,
  CheckCircle2,
} from 'lucide-react';

export default function VersionHistoryPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

  const [doc, setDoc] = useState(null);
  const [versions, setVersions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedVersion, setSelectedVersion] = useState(null);
  const [selectedContent, setSelectedContent] = useState(null);
  const [rollbacking, setRollbacking] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [document, docVersions] = await Promise.all([
          getDocument(id),
          getVersions(id),
        ]);

        if (cancelled) return;
        setDoc(document);
        setVersions(docVersions);

        // Select most recent version by default
        if (docVersions.length > 0) {
          setSelectedVersion(docVersions[0]);
          parseContent(docVersions[0].content);
        }
      } catch (err) {
        if (!cancelled) {
          addToast('Failed to load version history: ' + err.message, 'error');
          navigate(`/documents/${id}`);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [id, addToast, navigate]);

  const parseContent = (contentStr) => {
    try {
      const raw = JSON.parse(contentStr || '{}');
      if (raw.blocks) {
        const contentState = convertFromRaw(raw);
        setSelectedContent(EditorState.createWithContent(contentState));
      } else {
        setSelectedContent(EditorState.createEmpty());
      }
    } catch {
      setSelectedContent(EditorState.createEmpty());
    }
  };

  const handleSelectVersion = (version) => {
    setSelectedVersion(version);
    parseContent(version.content);
  };

  const handleRollback = async (version) => {
    if (!window.confirm(`Restore version ${version.versionNumber} as current?`)) return;

    setRollbacking(version.id);
    try {
      await rollbackVersion(id, version.id);
      addToast(`Restored to version ${version.versionNumber}`, 'success');

      // Refresh versions
      const updated = await getVersions(id);
      setVersions(updated);
    } catch (err) {
      addToast('Rollback failed: ' + err.message, 'error');
    } finally {
      setRollbacking(null);
    }
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading version history\u2026</p>
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

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate(`/documents/${id}`)}
          className="p-2 rounded-lg hover:bg-slate-100 text-slate-500"
          aria-label="Back to editor"
        >
          <ArrowLeft size={18} aria-hidden="true" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-slate-800">Version History</h1>
          <p className="text-sm text-slate-500">{doc.title}</p>
        </div>
      </div>

      {versions.length === 0 ? (
        <div className="card p-12">
          <div className="empty-state">
            <Clock size={40} className="text-slate-300" />
            <h3 className="empty-state-title">No version history</h3>
            <p className="empty-state-desc">
              This document doesn't have any versions yet.
            </p>
          </div>
        </div>
      ) : (
        <div className="flex gap-6">
          {/* Version timeline (left) */}
          <div className="w-72 shrink-0">
            <div className="space-y-1">
              {versions.map((v, idx) => {
                const isCurrent = idx === 0;
                const isSelected = selectedVersion?.id === v.id;
                return (
                  <div key={v.id} className="relative">
                    {/* Timeline line */}
                    {idx < versions.length - 1 && (
                      <div className="absolute left-[11px] top-6 bottom-0 w-0.5 bg-slate-200" />
                    )}

                    <button
                      onClick={() => handleSelectVersion(v)}
                      className={`w-full text-left p-3 rounded-lg transition-colors ${
                        isSelected
                          ? 'bg-primary-50 ring-1 ring-primary-200'
                          : 'hover:bg-slate-50'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center mt-0.5 shrink-0 ${
                          isCurrent
                            ? 'border-green-500 bg-green-50'
                            : isSelected
                            ? 'border-primary-500 bg-primary-50'
                            : 'border-slate-300 bg-white'
                        }`}>
                          {isCurrent && <CheckCircle2 size={10} className="text-green-600" />}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <span className="text-sm font-semibold text-slate-700">
                              v{v.versionNumber}
                            </span>
                            {isCurrent && (
                              <span className="badge badge-approved text-[10px] px-1.5 py-0">
                                Current
                              </span>
                            )}
                          </div>
                          <p className="text-xs text-slate-400 mt-0.5">
                            {new Date(v.createdAt).toLocaleDateString('en-US', {
                              month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit'
                            })}
                          </p>

                          {!isCurrent && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleRollback(v);
                              }}
                              disabled={rollbacking === v.id}
                              className="mt-1.5 text-xs text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1"
                            >
                              <RotateCcw size={11} />
                              {rollbacking === v.id ? 'Restoring\u2026' : 'Rollback'}
                            </button>
                          )}
                        </div>
                      </div>
                    </button>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Version content (right) */}
          <div className="flex-1 min-w-0">
            <div className="card p-4">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-sm font-semibold text-slate-700">
                  {selectedVersion ? `Version ${selectedVersion.versionNumber}` : 'Select a version'}
                </h2>
                {selectedVersion && (
                  <span className="text-xs text-slate-400">
                    {new Date(selectedVersion.createdAt).toLocaleString()}
                  </span>
                )}
              </div>

              {selectedContent ? (
                <RichTextEditor
                  editorState={selectedContent}
                  onChange={() => {}}
                  readOnly={true}
                />
              ) : (
                <p className="text-sm text-slate-400">Select a version to view its content</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
