import React, { useState } from 'react';
import { getVersions, rollbackVersion } from '../api/versions.api';
import { useToast } from '../context/ToastContext';
import { Clock, RotateCcw, ChevronRight } from 'lucide-react';

function VersionPanel({ documentId, versions: initialVersions, onRollback }) {
  const { addToast } = useToast();
  const [versions, setVersions] = useState(initialVersions || []);
  const [rollbacking, setRollbacking] = useState(null);

  const currentVersion = versions.length > 0 ? versions[0] : null;

  const handleRollback = async (version) => {
    if (!window.confirm(`Set version ${version.versionNumber} as the main version? This will restore its content as the current version.`)) {
      return;
    }

    setRollbacking(version.id);
    try {
      const newVersion = await rollbackVersion(documentId, version.id);
      addToast(`Set version ${version.versionNumber} as main`, 'success');
      onRollback?.(newVersion);

      // Refresh versions
      const updated = await getVersions(documentId);
      setVersions(updated);
    } catch (err) {
      addToast('Failed to set main version: ' + err.message, 'error');
    } finally {
      setRollbacking(null);
    }
  };

  if (versions.length === 0) {
    return (
      <div className="card p-4">
        <h3 className="text-sm font-semibold text-slate-700 mb-3 flex items-center gap-2">
          <Clock size={16} />
          Versions
        </h3>
        <p className="text-xs text-slate-400">No versions yet</p>
      </div>
    );
  }

  return (
    <div className="card p-4">
      <h3 className="text-sm font-semibold text-slate-700 mb-3 flex items-center gap-2">
        <Clock size={16} />
        Versions
      </h3>

      <div className="space-y-2">
        {versions.map((v, idx) => {
          const isCurrent = idx === 0;
          return (
            <div
              key={v.id}
              className={`p-2.5 rounded-lg text-sm transition-colors ${
                isCurrent
                  ? 'bg-primary-50 border border-primary-200'
                  : 'bg-slate-50 hover:bg-slate-100'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="font-medium text-slate-700">
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
                  month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                })}
              </p>

              {!isCurrent && (
                <button
                  onClick={() => handleRollback(v)}
                  disabled={rollbacking === v.id}
                  className="mt-1.5 text-xs text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1 transition-colors"
                >
                  {rollbacking === v.id ? (
                    'Restoring\u2026'
                  ) : (
                    <>
                      <RotateCcw size={12} />
                      Set as Main
                    </>
                  )}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default React.memo(VersionPanel);
