import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchDocuments } from '../api/search.api';
import { Search, FileText } from 'lucide-react';

export default function SearchBar({ compact = false }) {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [loading, setLoading] = useState(false);
  const debounceRef = useRef(null);
  const wrapperRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    const q = query.trim();
    if (!q) {
      setResults([]);
      setShowDropdown(false);
      return;
    }

    if (q.length < 2) return;

    setLoading(true);
    debounceRef.current = setTimeout(async () => {
      try {
        const result = await searchDocuments({ q });
        setResults(result.items.slice(0, 5));
        setShowDropdown(true);
      } catch (err) {
        console.error('Search failed:', err);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [query]);

  const handleSelect = (doc) => {
    setShowDropdown(false);
    setQuery('');
    navigate(`/documents/${doc.id}`);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) {
      setShowDropdown(false);
      navigate(`/search?q=${encodeURIComponent(query.trim())}`);
    }
  };

  return (
    <div className="relative" ref={wrapperRef}>
      <form onSubmit={handleSubmit} role="search">
        <div className="relative">
          <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" aria-hidden="true" />
          <label htmlFor="search-bar-input" className="sr-only">Search documents</label>
          <input
            id="search-bar-input"
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder=""
            autoComplete="off"
            spellCheck={false}
            className={`input pl-10 ${compact ? 'text-sm py-1.5' : ''}`}
          />
          {loading && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2">
              <div className="spinner spinner-sm" />
            </div>
          )}
        </div>
      </form>

      {/* Dropdown results */}
      {showDropdown && results.length > 0 && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-30">
          {results.map((doc) => (
            <button
              key={doc.id}
              onClick={() => handleSelect(doc)}
              className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
            >
              <FileText size={16} className="text-slate-400 shrink-0" />
              <div className="flex-1 text-left min-w-0">
                <span className="truncate block">{doc.title}</span>
              </div>
              <span className={`badge ${doc.status === 'APPROVED' ? 'badge-approved' : doc.status === 'DRAFT' ? 'badge-draft' : 'badge-pending'}`}>
                {doc.status}
              </span>
            </button>
          ))}
          <div className="border-t border-slate-100 mt-1 pt-1">
            <button
              onClick={handleSubmit}
              className="flex items-center gap-2 w-full px-4 py-2 text-xs text-primary-600 hover:bg-primary-50 transition-colors"
            >
              <Search size={14} />
              View all results for "{query}"
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
