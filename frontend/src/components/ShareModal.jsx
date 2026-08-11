import React, { useState } from 'react';
import { shareDocument } from '../api/share.api';
import { useToast } from '../context/ToastContext';
import { Share2, X, Copy, Link, Clock } from 'lucide-react';

const TTL_OPTIONS = [
  { value: 1, label: '1 hour' },
  { value: 24, label: '24 hours' },
  { value: 168, label: '7 days' },
];

export default function ShareModal({ documentId, documentTitle, onClose }) {
  const { addToast } = useToast();
  const [email, setEmail] = useState('');
  const [ttl, setTtl] = useState(24);
  const [shareLink, setShareLink] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!email.trim()) {
      addToast('Please enter an email address', 'error');
      return;
    }

    setLoading(true);
    try {
      const result = await shareDocument(documentId, email.trim(), ttl);
      setShareLink(result.link);
      addToast('Share link generated', 'success');
    } catch (err) {
      addToast('Failed to generate link: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    if (!shareLink) return;
    try {
      await navigator.clipboard.writeText(shareLink);
      addToast('Link copied to clipboard', 'success');
    } catch {
      // Fallback
      const textArea = document.createElement('textarea');
      textArea.value = shareLink;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      addToast('Link copied to clipboard', 'success');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 modal-overlay">
      <div className="fixed inset-0 bg-black/40 modal-overlay" onClick={onClose} />
      <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <Share2 size={20} className="text-primary-600" aria-hidden="true" />
            <div>
              <h2 className="text-lg font-semibold text-slate-800">Share Document</h2>
              <p className="text-xs text-slate-400 truncate max-w-[300px]">{documentTitle}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400" aria-label="Close">
            <X size={20} aria-hidden="true" />
          </button>
        </div>

        {!shareLink ? (
          <form onSubmit={handleGenerate} className="space-y-4">
            <div>
              <label htmlFor="share-email" className="block text-sm font-medium text-slate-700 mb-1.5">
                Share with email
              </label>
              <input
                id="share-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="colleague@company.com"
                autoComplete="email"
                className="input"
                disabled={loading}
                spellCheck={false}
              />
            </div>

            <div>
              <label htmlFor="share-ttl" className="block text-sm font-medium text-slate-700 mb-1.5">
                Link expires after
              </label>
              <select
                id="share-ttl"
                value={ttl}
                onChange={(e) => setTtl(Number(e.target.value))}
                className="input"
                disabled={loading}
              >
                {TTL_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="submit"
              disabled={loading || !email.trim()}
              className="btn btn-primary w-full"
            >
              {loading ? (
                <>
                  <div className="spinner spinner-sm border-white border-t-transparent" />
                  Generating\u2026
                </>
              ) : (
                <>
                  <Link size={16} aria-hidden="true" />
                  Generate Share Link
                </>
              )}
            </button>
          </form>
        ) : (
          <div className="space-y-4">
            <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-sm font-medium text-green-800 mb-1">Share link created</p>
              <p className="text-xs text-green-600">Expires in {TTL_OPTIONS.find(o => o.value === ttl)?.label}</p>
            </div>

            <div className="flex gap-2">
              <input
                type="text"
                value={shareLink}
                readOnly
                className="input flex-1 text-sm bg-slate-50"
                onClick={(e) => e.target.select()}
              />
              <button
                onClick={handleCopy}
                className="btn btn-primary"
                aria-label="Copy to clipboard"
              >
                <Copy size={16} aria-hidden="true" />
              </button>
            </div>

            <button
              onClick={() => { setShareLink(null); setEmail(''); }}
              className="btn btn-secondary w-full"
            >
              Share with another person
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
