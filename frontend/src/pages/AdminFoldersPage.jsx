import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { listFolders } from '../api/folders.api';
import { useToast } from '../context/ToastContext';
import { Folder, ChevronRight, LayoutGrid } from 'lucide-react';

export default function AdminFoldersPage() {
  const navigate = useNavigate();
  const { addToast } = useToast();
  
  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      try {
        const data = await listFolders();
        if (!cancelled) setFolders(data);
      } catch (err) {
        if (!cancelled) addToast('Failed to load folders', 'error');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [addToast]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <Folder size={24} className="text-primary-600" /> Folder Management
          </h1>
          <p className="page-subtitle">Manage all folders in the system</p>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center p-12"><div className="spinner" /></div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {folders.map((folder) => (
            <div 
              key={folder.id} 
              className="card p-5 group hover:border-primary-300 transition-colors cursor-pointer"
              onClick={() => navigate(`/admin/folders/${folder.id}`)}
            >
              <div className="flex items-start justify-between mb-4">
                <div className="w-12 h-12 rounded-xl bg-primary-50 text-primary-600 flex items-center justify-center group-hover:scale-110 transition-transform">
                  <Folder size={24} className="fill-primary-100" />
                </div>
              </div>
              
              <h3 className="font-semibold text-slate-800 mb-1 line-clamp-1">{folder.name}</h3>
              <p className="text-xs text-slate-500 mb-4 flex items-center gap-1">
                <LayoutGrid size={12} /> {folder.department || 'General'}
              </p>
              
              <div className="pt-4 border-t border-slate-100 flex items-center justify-between text-sm">
                <span className="text-slate-400 text-xs">
                  {new Date(folder.createdAt).toLocaleDateString()}
                </span>
                <span className="text-primary-600 font-medium flex items-center gap-1 group-hover:translate-x-1 transition-transform">
                  View <ChevronRight size={16} />
                </span>
              </div>
            </div>
          ))}
          {folders.length === 0 && (
            <div className="col-span-full p-12 text-center border-2 border-dashed border-slate-200 rounded-xl">
              <Folder size={48} className="mx-auto text-slate-300 mb-4" />
              <h3 className="text-lg font-medium text-slate-800">No Folders</h3>
              <p className="text-slate-500">The system has no folders yet.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
