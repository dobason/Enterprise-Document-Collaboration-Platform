import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getAllTags } from '../api/tags.api';
import { searchDocuments } from '../api/search.api';
import { useToast } from '../context/ToastContext';
import {
  Search,
  FileText,
  X,
  RotateCcw,
  SlidersHorizontal,
} from 'lucide-react';

const STATUS_BADGE = {
  PENDING: 'badge badge-pending',
  APPROVED: 'badge badge-approved',
  REJECTED: 'badge badge-rejected',
};

const FILE_TYPES = ['Report', 'Contract', 'Policy'];
const STATUSES = ['PENDING', 'APPROVED', 'REJECTED'];

export default function SearchPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { addToast } = useToast();

  const [keyword, setKeyword] = useState(searchParams.get('q') || '');
  const [selectedTags, setSelectedTags] = useState([]);
  const [selectedType, setSelectedType] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [allTags, setAllTags] = useState([]);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [showFilters, setShowFilters] = useState(false);

  useEffect(() => {
    getAllTags().then(setAllTags).catch(() => {});
  }, []);

  // Auto-search from URL params (initial load only)
  useEffect(() => {
    if (searchParams.get('q')) {
      // Will execute search on first render
      setLoading(true);
      setSearched(true);
      searchDocuments({ q: searchParams.get('q') })
        .then((result) => setResults(result.items))
        .catch(() => {})
        .finally(() => setLoading(false));
    }
  }, []);

  const handleSearch = useCallback(async () => {
    setLoading(true);
    setSearched(true);
    try {
      const result = await searchDocuments({
        q: keyword,
        tag: selectedTags.length > 0 ? selectedTags : undefined,
        type: selectedType || undefined,
        status: selectedStatus || undefined,
      });
      setResults(result.items);
    } catch (err) {
      addToast('Search failed: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  }, [keyword, selectedTags, selectedType, selectedStatus, addToast]);

  const handleClear = () => {
    setKeyword('');
    setSelectedTags([]);
    setSelectedType('');
    setSelectedStatus('');
    setResults([]);
    setSearched(false);
  };

  const toggleTag = (tagId) => {
    setSelectedTags((prev) =>
      prev.includes(tagId)
        ? prev.filter((id) => id !== tagId)
        : [...prev, tagId]
    );
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  };

  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-2xl font-bold text-slate-800 mb-6">Search Documents</h1>

      {/* Search form */}
      <div className="card p-6 mb-6">
        <div className="flex gap-3">
          <div className="flex-1 relative">
            <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" aria-hidden="true" />
            <label htmlFor="search-keyword" className="sr-only">Search keyword</label>
            <input
              id="search-keyword"
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Search by keyword\u2026"
              autoComplete="off"
              spellCheck={false}
              className="input pl-10"
            />
          </div>
          <button onClick={handleSearch} disabled={loading} className="btn btn-primary">
            {loading ? <div className="spinner spinner-sm border-white border-t-transparent" /> : <Search size={16} aria-hidden="true" />}
            Search
          </button>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`btn btn-secondary ${showFilters ? 'bg-primary-50 border-primary-200' : ''}`}
            aria-label={showFilters ? 'Hide filters' : 'Show filters'}
          >
            <SlidersHorizontal size={16} aria-hidden="true" />
          </button>
          {searched && (
            <button onClick={handleClear} className="btn btn-ghost" aria-label="Clear filters">
              <RotateCcw size={16} aria-hidden="true" />
            </button>
          )}
        </div>

        {/* Filters */}
        {showFilters && (
          <div className="mt-4 pt-4 border-t border-slate-100">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Tags */}
              <div>
                <span className="block text-xs font-medium text-slate-500 mb-1.5">Tags</span>
                <div className="flex flex-wrap gap-1.5" role="group" aria-label="Filter by tags">
                  {allTags.map((tag) => {
                    const active = selectedTags.includes(tag.id);
                    return (
                      <button
                        key={tag.id}
                        onClick={() => toggleTag(tag.id)}
                        className={`px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${
                          active
                            ? 'bg-primary-100 text-primary-700 ring-1 ring-primary-300'
                            : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                        }`}
                        aria-pressed={active}
                      >
                        {tag.name}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* File type */}
              <div>
                <label htmlFor="filter-type" className="block text-xs font-medium text-slate-500 mb-1.5">File Type</label>
                <select
                  id="filter-type"
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value)}
                  className="input text-sm"
                >
                  <option value="">All types</option>
                  {FILE_TYPES.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>

              {/* Status */}
              <div>
                <label htmlFor="filter-status" className="block text-xs font-medium text-slate-500 mb-1.5">Status</label>
                <select
                  id="filter-status"
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                  className="input text-sm"
                >
                  <option value="">All statuses</option>
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Results */}
      {loading ? (
        <div className="loading-center">
          <div className="spinner" />
          <p>Searching\u2026</p>
        </div>
      ) : !searched ? (
        <div className="card p-12">
          <div className="empty-state">
            <Search size={40} className="text-slate-300" />
            <h3 className="empty-state-title">Search your documents</h3>
            <p className="empty-state-desc">
              Enter keywords or use filters to find documents.
            </p>
          </div>
        </div>
      ) : results.length === 0 ? (
        <div className="card p-12">
          <div className="empty-state">
            <FileText size={40} className="text-slate-300" />
            <h3 className="empty-state-title">No results found</h3>
            <p className="empty-state-desc">
              No documents match your search criteria. Try different keywords or fewer filters.
            </p>
          </div>
        </div>
      ) : (
        <>
          <p className="text-sm text-slate-500 mb-4">{results.length} result{results.length !== 1 ? 's' : ''} found</p>

          <div className="card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50/50">
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">Name</th>
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3">Status</th>
                    <th className="text-left text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 py-3 hidden md:table-cell">Updated</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {results.map((doc) => (
                    <tr
                      key={doc.id}
                      onClick={() => navigate(`/documents/${doc.id}`)}
                      className="hover:bg-slate-50 transition-colors cursor-pointer"
                    >
                      <td className="px-4 py-3.5">
                        <div className="flex items-center gap-3">
                          <FileText size={18} className="text-slate-400 shrink-0" />
                          <span className="text-sm font-medium text-slate-700 truncate max-w-[300px]">{doc.title}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3.5">
                        <span className={STATUS_BADGE[doc.status] || 'badge'}>{doc.status}</span>
                      </td>
                      <td className="px-4 py-3.5 hidden md:table-cell">
                        <span className="text-sm text-slate-500">{formatDate(doc.updatedAt)}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
