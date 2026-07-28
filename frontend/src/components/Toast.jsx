import React from 'react';
import { useToast } from '../context/ToastContext';
import { CheckCircle, XCircle, AlertCircle, X } from 'lucide-react';

const typeStyles = {
  success: 'bg-green-600 text-white',
  error: 'bg-red-600 text-white',
  info: 'bg-primary-600 text-white',
  warning: 'bg-amber-600 text-white',
};

const typeIcons = {
  success: CheckCircle,
  error: XCircle,
  info: AlertCircle,
  warning: AlertCircle,
};

function ToastItem({ toast, onRemove }) {
  const Icon = typeIcons[toast.type] || AlertCircle;
  const style = typeStyles[toast.type] || typeStyles.info;

  return (
    <div
      className={`flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg ${style} min-w-[300px] max-w-[420px]`}
      role="alert"
      aria-live="polite"
    >
      <Icon size={18} className="shrink-0" aria-hidden="true" />
      <p className="flex-1 text-sm font-medium">{toast.message}</p>
      <button
        onClick={() => onRemove(toast.id)}
        className="shrink-0 hover:opacity-80 transition-opacity"
        aria-label="Dismiss"
      >
        <X size={16} />
      </button>
    </div>
  );
}

export default function ToastContainer() {
  const { toasts, removeToast } = useToast();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-[9999] flex flex-col gap-2">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onRemove={removeToast} />
      ))}
    </div>
  );
}
