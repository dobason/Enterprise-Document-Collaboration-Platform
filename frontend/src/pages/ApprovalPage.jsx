import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDocument } from '../api/documents.api';
import { submitForApproval, approveDocument, rejectDocument, getApprovalHistory } from '../api/approval.api';
import { getUserRole } from '../api/permissions.api';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import {
  ArrowLeft,
  Send,
  CheckCircle,
  XCircle,
  Clock,
  History,
} from 'lucide-react';

const STATUS_CONFIG = {
  DRAFT: { color: 'badge-draft', icon: Clock },
  PENDING: { color: 'badge-pending', icon: Clock },
  APPROVED: { color: 'badge-approved', icon: CheckCircle },
  REJECTED: { color: 'badge-rejected', icon: XCircle },
};

export default function ApprovalPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [doc, setDoc] = useState(null);
  const [userRole, setUserRole] = useState(null);
  const [approvalHistory, setApprovalHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [document, role, history] = await Promise.all([
          getDocument(id),
          getUserRole(id, user?.id),
          getApprovalHistory(id),
        ]);

        if (cancelled) return;
        setDoc(document);
        setUserRole(role);
        setApprovalHistory(history);
      } catch (err) {
        if (!cancelled) {
          addToast('Failed to load: ' + err.message, 'error');
          navigate(`/documents/${id}`);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [id, user?.id, addToast, navigate]);

  const handleAction = async (action) => {
    setActionLoading(action);
    try {
      let updated;
      switch (action) {
        case 'submit':
          updated = await submitForApproval(id);
          addToast('Document submitted for approval', 'success');
          break;
        case 'approve':
          updated = await approveDocument(id);
          addToast('Document approved', 'success');
          break;
        case 'reject':
          if (!rejectReason.trim()) {
            addToast('Reject reason is required', 'error');
            setActionLoading(null);
            return;
          }
          updated = await rejectDocument(id, rejectReason.trim());
          addToast('Document rejected', 'info');
          setShowRejectModal(false);
          setRejectReason('');
          break;
        default:
          return;
      }

      setDoc(updated);

      // Refresh history
      const history = await getApprovalHistory(id);
      setApprovalHistory(history);
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading approval info\u2026</p>
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="empty-state">
        <h3 className="empty-state-title">Document not found</h3>
        <button onClick={() => navigate('/documents')} className="btn btn-primary mt-4">Back</button>
      </div>
    );
  }

  const StatusIcon = STATUS_CONFIG[doc.status]?.icon || Clock;
  const isAdmin = user?.role === 'ADMIN';
  const isOwnerOrEditor = userRole === 'OWNER' || userRole === 'EDITOR';
  const canSubmit = doc.status === 'DRAFT' && (isAdmin || isOwnerOrEditor);
  const canApprove = (isAdmin || userRole === 'MANAGER') && doc.status === 'PENDING';
  const canReject = (isAdmin || userRole === 'MANAGER') && doc.status === 'PENDING';

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate(`/documents/${id}`)} className="p-2 rounded-lg hover:bg-slate-100 text-slate-500" aria-label="Back">
          <ArrowLeft size={18} aria-hidden="true" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-slate-800">Approval</h1>
          <p className="text-sm text-slate-500">{doc.title}</p>
        </div>
      </div>

      {/* Current status */}
      <div className="card p-6 mb-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-slate-500 mb-1">Current Status</p>
            <div className="flex items-center gap-3">
              <StatusIcon size={32} aria-hidden="true" className={
                doc.status === 'APPROVED' ? 'text-green-600' :
                doc.status === 'REJECTED' ? 'text-red-600' :
                doc.status === 'PENDING' ? 'text-cyan-600' : 'text-amber-600'
              } />
              <span className={`badge !text-base !px-4 !py-1.5 ${STATUS_CONFIG[doc.status]?.color || 'badge'}`}>
                {doc.status}
              </span>
            </div>
          </div>

          {/* Action buttons */}
          <div className="flex gap-3">
            {canSubmit && (
              <button
                onClick={() => handleAction('submit')}
                disabled={actionLoading === 'submit'}
                className="btn btn-primary"
              >
                {actionLoading === 'submit' ? (
                  <div className="spinner spinner-sm border-white border-t-transparent" />
                ) : <Send size={16} />}
                Submit for Approval
              </button>
            )}
            {canApprove && (
              <button
                onClick={() => handleAction('approve')}
                disabled={actionLoading === 'approve'}
                className="btn bg-green-600 text-white hover:bg-green-700 inline-flex items-center justify-center gap-2 px-4 py-2 rounded-lg font-medium text-sm"
              >
                {actionLoading === 'approve' ? (
                  <div className="spinner spinner-sm border-white border-t-transparent" />
                ) : <CheckCircle size={16} />}
                Approve
              </button>
            )}
            {canReject && (
              <button
                onClick={() => setShowRejectModal(true)}
                disabled={actionLoading === 'reject'}
                className="btn btn-danger"
              >
                <XCircle size={16} />
                Reject
              </button>
            )}
          </div>
        </div>

        {!canApprove && !canReject && doc.status === 'PENDING' && (
          <p className="text-sm text-slate-400 mt-4">Only admin or document owner can approve/reject.</p>
        )}
        {doc.status === 'APPROVED' && (
          <p className="text-sm text-green-600 mt-4">This document has been approved and is publicly available.</p>
        )}
        {doc.status === 'REJECTED' && (
          <p className="text-sm text-red-600 mt-4">This document has been rejected. Contact the owner for more information.</p>
        )}
      </div>

      {/* Approval history */}
      <div className="card p-6">
        <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
          <History size={16} />
          Approval History
        </h3>

        {approvalHistory.length === 0 ? (
          <p className="text-sm text-slate-400">No approval history yet</p>
        ) : (
          <div className="space-y-3">
            {approvalHistory.map((entry) => (
              <div key={entry.id} className="flex items-start gap-3 p-3 bg-slate-50 rounded-lg">
                <div className="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center shrink-0">
                  {entry.action === 'SUBMIT' ? <Send size={14} className="text-amber-600" /> :
                   entry.action === 'APPROVE' ? <CheckCircle size={14} className="text-green-600" /> :
                   entry.action === 'REJECT' ? <XCircle size={14} className="text-red-600" /> :
                   <Clock size={14} className="text-slate-400" />}
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium text-slate-700">
                      {entry.action === 'SUBMIT' ? 'Submitted for approval' :
                       entry.action === 'APPROVE' ? 'Approved' :
                       entry.action === 'REJECT' ? 'Rejected' : entry.action}
                    </span>
                    <span className="text-xs text-slate-400">
                      {entry.fromStatus} → {entry.toStatus}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5">
                    {new Date(entry.timestamp).toLocaleString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
      {/* Reject Modal */}
      {showRejectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !actionLoading && setShowRejectModal(false)} />
          <div className="relative bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
              <XCircle size={20} className="text-red-600" />
              Reject Document
            </h3>
            
            <div className="space-y-4">
              <div>
                <label htmlFor="rejectReason" className="block text-sm font-medium text-slate-700 mb-1.5">
                  Reason for rejection <span className="text-red-500">*</span>
                </label>
                <textarea
                  id="rejectReason"
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  className="input min-h-[100px] py-2"
                  placeholder="Please specify why this document is being rejected..."
                  disabled={actionLoading === 'reject'}
                  autoFocus
                />
              </div>
              
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowRejectModal(false)}
                  disabled={actionLoading === 'reject'}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  onClick={() => handleAction('reject')}
                  disabled={actionLoading === 'reject' || !rejectReason.trim()}
                  className="btn btn-danger"
                >
                  {actionLoading === 'reject' ? (
                    <div className="spinner spinner-sm border-white border-t-transparent" />
                  ) : 'Confirm Rejection'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
