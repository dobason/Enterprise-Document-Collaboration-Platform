import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDocument } from '../api/documents.api';
import { getPermissions, grantPermission, removePermission, updatePermission } from '../api/permissions.api';
import { listUsers } from '../api/users.api';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import {
  ArrowLeft,
  Shield,
  User,
  Trash2,
  Plus,
  Info,
} from 'lucide-react';

const ROLES = ['VIEWER', 'EDITOR'];
const OWNER_ROLE = 'OWNER';

export default function PermissionManagerPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { addToast } = useToast();

  const [doc, setDoc] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newEmail, setNewEmail] = useState('');
  const [newRole, setNewRole] = useState('VIEWER');
  const [adding, setAdding] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [userDirectory, setUserDirectory] = useState([]);

  useEffect(() => {
    listUsers().then(setUserDirectory).catch(() => {});
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [document, perms] = await Promise.all([
          getDocument(id),
          getPermissions(id),
        ]);

        if (cancelled) return;
        setDoc(document);
        setPermissions(perms);
      } catch (err) {
        if (!cancelled) {
          addToast('Failed to load: ' + err.message, 'error');
          navigate(-1);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => { cancelled = true; };
  }, [id, addToast, navigate]);

  const handleEmailChange = (e) => {
    const value = e.target.value;
    setNewEmail(value);

    if (value.trim().length > 0) {
      const allUsers = userDirectory;
      const filtered = allUsers.filter(
        (u) =>
          u.email.toLowerCase().includes(value.toLowerCase()) &&
          !permissions.some((p) => p.userId === u.id)
      );
      setSuggestions(filtered);
      setShowSuggestions(filtered.length > 0);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  };

  const handleGrant = async () => {
    if (!newEmail.trim()) return;

    setAdding(true);
    try {
      const targetUser = userDirectory.find(
        (u) => u.email.toLowerCase() === newEmail.toLowerCase()
      );

      if (!targetUser) {
        addToast('User not found in the system', 'error');
        setAdding(false);
        return;
      }

      await grantPermission(id, targetUser.id, newRole);
      addToast(`Permission granted to ${targetUser.email}`, 'success');

      // Refresh
      const perms = await getPermissions(id);
      setPermissions(perms);
      setNewEmail('');
      setShowSuggestions(false);
    } catch (err) {
      addToast('Failed to grant permission: ' + err.message, 'error');
    } finally {
      setAdding(false);
    }
  };

  const handleRemove = async (perm) => {
    if (perm.role === OWNER_ROLE) {
      addToast('Cannot remove the Owner', 'error');
      return;
    }

    if (!window.confirm(`Remove permission for ${perm.userEmail}?`)) return;

    try {
      await removePermission(id, perm.id);
      addToast(`Permission removed for ${perm.userEmail}`, 'info');

      const perms = await getPermissions(id);
      setPermissions(perms);
    } catch (err) {
      addToast('Failed to remove: ' + err.message, 'error');
    }
  };

  const handleRoleChange = async (perm, newRole) => {
    if (perm.role === OWNER_ROLE) return;

    try {
      await updatePermission(id, perm.id, newRole);
      addToast(`Role updated to ${newRole}`, 'success');

      const perms = await getPermissions(id);
      setPermissions(perms);
    } catch (err) {
      addToast('Failed to update role: ' + err.message, 'error');
    }
  };

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner" />
        <p>Loading permissions\u2026</p>
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="empty-state">
        <h3 className="empty-state-title">Document not found</h3>
        <button onClick={() => navigate(-1)} className="btn btn-primary mt-4">Back</button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate(-1)} className="p-2 rounded-lg hover:bg-slate-100 text-slate-500" aria-label="Back">
          <ArrowLeft size={18} aria-hidden="true" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-slate-800">Manage Permissions</h1>
          <p className="text-sm text-slate-500">{doc.title}</p>
        </div>
      </div>

      {/* Add permission form */}
      <div className="card p-6 mb-6">
        <h3 className="text-sm font-semibold text-slate-700 mb-4 flex items-center gap-2">
          <Plus size={16} />
          Add Permission
        </h3>

        <div className="flex gap-3 items-end">
          <div className="flex-1 relative">
            <input
              type="text"
              value={newEmail}
              onChange={handleEmailChange}
              onKeyDown={(e) => e.key === 'Enter' && handleGrant()}
              placeholder="Enter email address..."
              className="input"
            />
            {showSuggestions && (
              <div className="absolute top-full left-0 right-0 mt-1 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-10">
                {suggestions.map((u) => (
                  <button
                    key={u.id}
                    onClick={() => {
                      setNewEmail(u.email);
                      setShowSuggestions(false);
                    }}
                    className="flex items-center gap-3 w-full px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  >
                    <User size={14} className="text-slate-400" />
                    <span>{u.email}</span>
                    <span className="text-xs text-slate-400 ml-auto">{u.department}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          <select
            value={newRole}
            onChange={(e) => setNewRole(e.target.value)}
            className="input w-32"
          >
            {ROLES.map((role) => (
              <option key={role} value={role}>{role}</option>
            ))}
          </select>

          <button
            onClick={handleGrant}
            disabled={adding || !newEmail.trim()}
            className="btn btn-primary"
          >
            {adding ? 'Adding...' : 'Grant'}
          </button>
        </div>
      </div>

      {/* Permission list */}
      <div className="card overflow-hidden">
        <div className="px-4 py-3 border-b border-slate-100 bg-slate-50/50">
          <h3 className="text-sm font-semibold text-slate-700 flex items-center gap-2">
            <Shield size={16} />
            Current Permissions
          </h3>
        </div>

        {permissions.length === 0 ? (
          <div className="p-8 text-center text-sm text-slate-400">No permissions configured</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-slate-100">
                  <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">User</th>
                  <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">Role</th>
                  <th className="text-right text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3 w-20">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {permissions.map((perm) => (
                  <tr key={perm.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-3">
                        <User size={16} className="text-slate-400" />
                        <div>
                          <p className="text-sm font-medium text-slate-700">{perm.userName}</p>
                          <p className="text-xs text-slate-400">{perm.userEmail}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3.5">
                      {perm.role === OWNER_ROLE ? (
                        <span className="badge badge-approved">OWNER</span>
                      ) : (
                        <select
                          value={perm.role}
                          onChange={(e) => handleRoleChange(perm, e.target.value)}
                          className="input text-sm py-1.5 w-28"
                        >
                          {ROLES.map((role) => (
                            <option key={role} value={role}>{role}</option>
                          ))}
                        </select>
                      )}
                    </td>
                    <td className="px-4 py-3.5 text-right">
                      {perm.role !== OWNER_ROLE && (
                        <button
                          onClick={() => handleRemove(perm)}
                          className="p-1.5 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"
                          aria-label="Remove permission"
                        >
                          <Trash2 size={16} aria-hidden="true" />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Info */}
      <div className="mt-4 flex items-start gap-2 p-3 bg-primary-50 text-primary-700 rounded-lg text-sm">
        <Info size={16} className="shrink-0 mt-0.5" />
        <p>The document owner (OWNER) cannot be removed. Only the owner can grant and manage permissions.</p>
      </div>
    </div>
  );
}
