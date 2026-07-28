import React, { useState, useEffect } from 'react';
import { getTags, addTag, removeTag } from '../api/tags.api';
import { useToast } from '../context/ToastContext';
import { Tag, Plus, X } from 'lucide-react';

const TAG_COLORS = [
  'bg-blue-100 text-blue-700',
  'bg-green-100 text-green-700',
  'bg-purple-100 text-purple-700',
  'bg-amber-100 text-amber-700',
  'bg-pink-100 text-pink-700',
  'bg-cyan-100 text-cyan-700',
  'bg-indigo-100 text-indigo-700',
  'bg-rose-100 text-rose-700',
];

function hashString(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function TagManager({ documentId }) {
  const { addToast } = useToast();
  const [tags, setTags] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!documentId) return;

    const load = async () => {
      try {
        const result = await getTags(documentId);
        setTags(result);
      } catch (err) {
        console.error('Failed to load tags:', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [documentId]);

  const handleAdd = async () => {
    const name = inputValue.trim();
    if (!name) return;

    try {
      const tag = await addTag(documentId, name);
      if (tag) {
        setTags((prev) => [...prev, tag]);
        addToast(`Tag "${name}" added`, 'success');
      }
      setInputValue('');
    } catch (err) {
      addToast('Failed to add tag: ' + err.message, 'error');
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAdd();
    }
  };

  const handleRemove = async (tag) => {
    if (!tag.docTagId) return;
    try {
      await removeTag(documentId, tag.docTagId);
      setTags((prev) => prev.filter((t) => t.id !== tag.id));
      addToast(`Tag "${tag.name}" removed`, 'info');
    } catch (err) {
      addToast('Failed to remove tag: ' + err.message, 'error');
    }
  };

  const getTagColor = (name) => {
    const idx = hashString(name) % TAG_COLORS.length;
    return TAG_COLORS[idx];
  };

  if (!documentId) return null;

  return (
    <div className="card p-4">
      <h3 className="text-sm font-semibold text-slate-700 mb-3 flex items-center gap-2">
        <Tag size={16} />
        Tags
      </h3>

      {/* Tag list */}
      <div className="flex flex-wrap gap-1.5 mb-3 min-h-[24px]">
        {loading ? (
          <div className="spinner spinner-sm" />
        ) : tags.length === 0 ? (
          <p className="text-xs text-slate-400">No tags yet</p>
        ) : (
          tags.map((tag) => (
            <span
              key={tag.id}
              className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${getTagColor(tag.name)}`}
            >
              {tag.name}
              <button
                onClick={() => handleRemove(tag)}
                className="hover:opacity-70 transition-opacity"
                aria-label={`Remove tag ${tag.name}`}
              >
                <X size={12} />
              </button>
            </span>
          ))
        )}
      </div>

      {/* Add tag input */}
      <div className="flex gap-2">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Add tag..."
          className="input flex-1 text-xs px-2.5 py-1.5"
        />
        <button
          onClick={handleAdd}
          disabled={!inputValue.trim()}
          className="btn btn-primary !px-2.5 !py-1.5"
          title="Add tag"
        >
          <Plus size={14} />
        </button>
      </div>
    </div>
  );
}

export default React.memo(TagManager);
