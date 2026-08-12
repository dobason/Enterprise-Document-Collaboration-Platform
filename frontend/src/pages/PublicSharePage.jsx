import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getSharedDocument } from '../api/share.api';
import { FileText, Download, AlertCircle } from 'lucide-react';
import { API_BASE_URL } from '../api/client';

export default function PublicSharePage() {
  const { shareToken } = useParams();
  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const data = await getSharedDocument(shareToken);
        if (!cancelled) {
          setDoc(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Link is invalid or has expired');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }
    load();
    return () => { cancelled = true; };
  }, [shareToken]);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center">
        <div className="spinner mb-4 text-primary-600" />
        <p className="text-slate-500">Loading document\u2026</p>
      </div>
    );
  }

  if (error || !doc) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
        <div className="card max-w-md w-full p-8 text-center">
          <div className="w-16 h-16 rounded-full bg-red-100 text-red-600 flex items-center justify-center mx-auto mb-4">
            <AlertCircle size={32} />
          </div>
          <h2 className="text-xl font-bold text-slate-800 mb-2">Document Unavailable</h2>
          <p className="text-slate-500 mb-6">{error}</p>
          <Link to="/" className="btn btn-primary w-full">Go to Homepage</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Topbar */}
      <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 lg:px-6 shrink-0 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-primary-600 flex items-center justify-center">
            <FileText size={18} className="text-white" />
          </div>
          <span className="font-bold text-lg text-slate-800 hidden sm:block">EDMS</span>
        </div>
        
        <div className="flex items-center gap-3">
          <span className="badge badge-draft">Read Only</span>
          <a
            href={`${API_BASE_URL}/documents/${doc.id}/download`}
            className="btn btn-primary px-4 py-2 text-sm hidden sm:flex"
            target="_blank"
            rel="noopener noreferrer"
            download
          >
            <Download size={16} />
            Download Original
          </a>
        </div>
      </header>

      {/* Main content */}
      <main className="flex-1 overflow-y-auto p-4 lg:p-8">
        <div className="max-w-4xl mx-auto">
          <div className="mb-6 flex justify-between items-start gap-4">
            <div>
              <h1 className="text-2xl font-bold text-slate-800">{doc.title}</h1>
              <p className="text-sm text-slate-500 mt-1">
                Shared on {new Date(doc.createdAt).toLocaleDateString()} &bull; {doc.type}
              </p>
            </div>
            {/* Mobile download button */}
            <a
              href={`${API_BASE_URL}/documents/${doc.id}/download`}
              className="btn btn-primary sm:hidden shrink-0"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Download"
            >
              <Download size={18} />
            </a>
          </div>

          <div className="card p-6 min-h-[500px]">
            {/* Document Content View */}
            <div className="prose prose-slate max-w-none">
              <pre className="whitespace-pre-wrap font-sans text-sm text-slate-700 bg-slate-50 p-6 rounded-xl border border-slate-100">
                {doc.content || 'This document has no content.'}
              </pre>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
