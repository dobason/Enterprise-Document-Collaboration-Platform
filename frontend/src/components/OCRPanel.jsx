import React, { useState, useEffect } from 'react';
import { getOCRResult, requestOCR } from '../api/ocr.api';
import { useToast } from '../context/ToastContext';
import { FileText, Copy, RefreshCw, Loader2, CheckCircle2 } from 'lucide-react';

export default function OCRPanel({ documentId }) {
  const { addToast } = useToast();
  const [ocrState, setOcrState] = useState({ loading: true, data: null, error: null });

  useEffect(() => {
    if (!documentId) return;
    loadOCR();
  }, [documentId]);

  const loadOCR = async () => {
    setOcrState({ loading: true, data: null, error: null });
    try {
      const result = await getOCRResult(documentId);
      setOcrState({ loading: false, data: result, error: null });
    } catch (err) {
      setOcrState({ loading: false, data: null, error: err.message });
    }
  };

  const handleRequestOCR = async () => {
    setOcrState((prev) => ({ ...prev, loading: true }));
    try {
      const result = await requestOCR(documentId);
      setOcrState({ loading: false, data: result, error: null });
      addToast('OCR processing complete', 'success');
    } catch (err) {
      setOcrState((prev) => ({ ...prev, loading: false }));
      addToast('OCR failed: ' + err.message, 'error');
    }
  };

  const handleCopy = async () => {
    if (!ocrState.data?.text) return;
    try {
      await navigator.clipboard.writeText(ocrState.data.text);
      addToast('Copied to clipboard', 'success');
    } catch {
      const textArea = document.createElement('textarea');
      textArea.value = ocrState.data.text;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      addToast('Copied to clipboard', 'success');
    }
  };

  if (!documentId) return null;

  return (
    <div className="card p-4">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-slate-700 flex items-center gap-2">
          <FileText size={16} />
          OCR Extracted Text
        </h3>
        {ocrState.data?.status === 'completed' && (
          <button
            onClick={handleCopy}
            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
            aria-label="Copy OCR text"
          >
            <Copy size={14} aria-hidden="true" />
          </button>
        )}
      </div>

      {ocrState.loading ? (
        <div className="flex items-center gap-2 py-4 text-sm text-slate-400">
          <Loader2 size={14} className="animate-spin" />
          {ocrState.data?.status === 'processing' ? 'Processing OCR\u2026' : 'Loading OCR\u2026'}
        </div>
      ) : ocrState.error ? (
        <div className="text-sm text-red-500 py-2">{ocrState.error}</div>
      ) : ocrState.data?.status === 'processing' ? (
        <div className="space-y-3 py-2">
          <div className="flex items-center gap-2 text-sm text-amber-600">
            <Loader2 size={14} className="animate-spin" />
            OCR is being processed
          </div>
          <button onClick={handleRequestOCR} className="btn btn-secondary text-xs">
            <RefreshCw size={12} />
            Check result
          </button>
        </div>
      ) : ocrState.data?.status === 'completed' ? (
        <div>
          <div className="flex items-center gap-1.5 text-xs text-green-600 mb-2">
            <CheckCircle2 size={12} />
            Extracted
          </div>
          <div className="bg-slate-50 rounded-lg p-3 max-h-32 overflow-y-auto">
            <pre className="text-xs text-slate-600 whitespace-pre-wrap font-sans leading-relaxed">
              {ocrState.data.text}
            </pre>
          </div>
        </div>
      ) : (
        <div className="py-2">
          <p className="text-sm text-slate-400 mb-2">No OCR data available</p>
          <button onClick={handleRequestOCR} className="btn btn-secondary text-xs">
            <RefreshCw size={12} />
            Request OCR
          </button>
        </div>
      )}
    </div>
  );
}
