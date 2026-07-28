import React, { useState, useRef } from 'react';
import { createDocument } from '../api/documents.api';
import { useAuth } from '../context/AuthContext';
import { Upload, File, X, CheckCircle } from 'lucide-react';

export default function UploadModal({ onClose, onUploaded }) {
  const { user } = useAuth();
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [done, setDone] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      setProgress(0);
      setDone(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) {
      setSelectedFile(file);
      setProgress(0);
      setDone(false);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    setProgress(0);

    // Fake progress simulation
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 90) {
          clearInterval(interval);
          return 90;
        }
        return prev + Math.random() * 15;
      });
    }, 200);

    try {
      // Simulate upload delay
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // Create document in mock data
      await createDocument({
        title: selectedFile.name.replace(/\.[^/.]+$/, ''),
        type: selectedFile.name.split('.').pop().toUpperCase(),
        fileName: selectedFile.name,
        fileSize: selectedFile.size,
        ownerId: user?.id || 'u1',
        status: 'DRAFT',
      });

      clearInterval(interval);
      setProgress(100);
      setDone(true);

      setTimeout(() => {
        onUploaded?.();
      }, 800);
    } catch (err) {
      clearInterval(interval);
      setUploading(false);
      setProgress(0);
      alert('Upload failed: ' + err.message);
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' bytes';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 modal-overlay">
      <div className="fixed inset-0 bg-black/40 modal-overlay" onClick={onClose} />
      <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-lg font-semibold text-slate-800">Upload Document</h2>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400" aria-label="Close">
            <X size={20} aria-hidden="true" />
          </button>
        </div>

        {/* Drop zone */}
        {!selectedFile ? (
          <div
            role="button"
            tabIndex={0}
            aria-label="Select a file to upload"
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); fileInputRef.current?.click(); }}}
            className="border-2 border-dashed border-slate-200 rounded-xl p-10 text-center cursor-pointer hover:border-primary-300 hover:bg-primary-50/30 transition-colors"
          >
            <Upload size={40} className="mx-auto text-slate-300 mb-3" aria-hidden="true" />
            <p className="text-sm font-medium text-slate-600">
              Drop a file here, or click to browse
            </p>
            <p className="text-xs text-slate-400 mt-1">
              Any file type supported
            </p>
            <input
              ref={fileInputRef}
              type="file"
              onChange={handleFileSelect}
              className="hidden"
            />
          </div>
        ) : (
          <div className="space-y-4">
            {/* Selected file info */}
            <div className="flex items-center gap-3 p-3 bg-slate-50 rounded-lg">
              <File size={24} className="text-primary-600" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-slate-700 truncate">
                  {selectedFile.name}
                </p>
                <p className="text-xs text-slate-400">
                  {formatFileSize(selectedFile.size)}
                </p>
              </div>
              {!uploading && !done && (
                <button
                  onClick={() => setSelectedFile(null)}
                  className="p-1 rounded hover:bg-slate-200 text-slate-400"
                  aria-label="Remove file"
                >
                  <X size={16} aria-hidden="true" />
                </button>
              )}
              {done && (
                <CheckCircle size={20} className="text-green-500" aria-hidden="true" />
              )}
            </div>

            {/* Progress bar */}
            {uploading && (
              <div className="space-y-1">
                <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                  <div
                    className="bg-primary-600 h-full rounded-full transition-[width] duration-300"
                    style={{ width: `${Math.min(100, progress)}%` }}
                  />
                </div>
                <p className="text-xs text-slate-400 text-right">
                  {Math.round(progress)}%
                </p>
              </div>
            )}

            {/* Actions */}
            {!uploading && !done && (
              <button onClick={handleUpload} className="btn btn-primary w-full">
                <Upload size={16} />
                Upload
              </button>
            )}

            {done && (
              <button onClick={onClose} className="btn btn-primary w-full">
                Done
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
