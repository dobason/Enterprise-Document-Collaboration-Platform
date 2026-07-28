import React, { useCallback } from 'react';
import { RichUtils } from 'draft-js';
import {
  Bold,
  Italic,
  Underline,
  Code,
  Highlighter,
  Heading1,
  Heading2,
  AlignLeft,
  AlignCenter,
  AlignRight,
  Image,
  List,
  ListOrdered,
} from 'lucide-react';

const BLOCK_TYPES = [
  { label: 'H1', icon: Heading1, style: 'header-one', shortcut: '##' },
  { label: 'H2', icon: Heading2, style: 'header-two', shortcut: '#' },
  { label: 'UL', icon: List, style: 'unordered-list-item', shortcut: '-' },
  { label: 'OL', icon: ListOrdered, style: 'ordered-list-item', shortcut: '1.' },
];

const INLINE_STYLES = [
  { label: 'Bold', icon: Bold, style: 'BOLD' },
  { label: 'Italic', icon: Italic, style: 'ITALIC' },
  { label: 'Underline', icon: Underline, style: 'UNDERLINE' },
  { label: 'Code', icon: Code, style: 'CODE' },
  { label: 'Highlight', icon: Highlighter, style: 'HIGHLIGHT' },
];

function ToolbarButton({ active, onToggle, children, title, disabled }) {
  return (
    <button
      onMouseDown={(e) => {
        e.preventDefault();
        if (!disabled) onToggle();
      }}
      title={title}
      disabled={disabled}
      className={`p-1.5 rounded transition-colors ${
        active
          ? 'bg-primary-100 text-primary-700'
          : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
      } disabled:opacity-30 disabled:cursor-not-allowed`}
    >
      {children}
    </button>
  );
}

export default function Toolbar({ editorState, onChange, onImageClick }) {
  const selection = editorState.getSelection();
  const blockType = editorState
    .getCurrentContent()
    .getBlockForKey(selection.getStartKey())
    ?.getType();

  const currentStyle = editorState.getCurrentInlineStyle();

  const toggleBlockType = useCallback(
    (blockStyle) => {
      onChange(RichUtils.toggleBlockType(editorState, blockStyle));
    },
    [editorState, onChange]
  );

  const toggleInlineStyle = useCallback(
    (inlineStyle) => {
      onChange(RichUtils.toggleInlineStyle(editorState, inlineStyle));
    },
    [editorState, onChange]
  );

  const handleImage = useCallback(() => {
    onImageClick?.();
  }, [onImageClick]);

  return (
    <div className="flex items-center flex-wrap gap-0.5 px-3 py-2 border-b border-slate-200 bg-white rounded-t-lg">
      {/* Block types */}
      {BLOCK_TYPES.map((type) => (
        <ToolbarButton
          key={type.style}
          active={blockType === type.style}
          onToggle={() => toggleBlockType(type.style)}
          title={type.label}
        >
          <type.icon size={16} />
        </ToolbarButton>
      ))}

      <div className="w-px h-5 bg-slate-200 mx-1" />

      {/* Inline styles */}
      {INLINE_STYLES.map((style) => (
        <ToolbarButton
          key={style.style}
          active={currentStyle.has(style.style)}
          onToggle={() => toggleInlineStyle(style.style)}
          title={style.label}
        >
          <style.icon size={16} />
        </ToolbarButton>
      ))}

      <div className="w-px h-5 bg-slate-200 mx-1" />

      {/* Image insert */}
      <ToolbarButton
        active={false}
        onToggle={handleImage}
        title="Insert Image"
      >
        <Image size={16} />
      </ToolbarButton>
    </div>
  );
}
