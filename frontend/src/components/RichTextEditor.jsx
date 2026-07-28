import React, { useRef, useCallback } from 'react';
import { Editor, EditorState, RichUtils, AtomicBlockUtils, getDefaultKeyBinding } from 'draft-js';
import Toolbar from './Toolbar';

function mediaBlockRenderer(block) {
  if (block.getType() === 'atomic') {
    return {
      component: MediaBlock,
      editable: false,
    };
  }
  return null;
}

function MediaBlock({ block, contentState }) {
  const entity = contentState.getEntity(block.getEntityAt(0));
  const { src, alt } = entity.getData();

  return (
    <div className="my-3 flex justify-center">
      <img
        src={src}
        alt={alt || 'Uploaded image'}
        className="max-w-full rounded-lg shadow-sm max-h-96"
        style={{ maxWidth: '100%' }}
      />
    </div>
  );
}

function myKeyBindingFn(e) {
  if (e.key === 'Tab') {
    e.preventDefault();
    return 'editor-tab';
  }
  return getDefaultKeyBinding(e);
}

export default function RichTextEditor({
  editorState,
  onChange,
  readOnly = false,
  placeholder = 'Start typing\u2026',
}) {
  const editorRef = useRef(null);

  const focusEditor = useCallback(() => {
    if (editorRef.current && !readOnly) {
      editorRef.current.focus();
    }
  }, [readOnly]);

  const handleKeyCommand = useCallback(
    (command) => {
      if (readOnly) return 'not-handled';

      if (command === 'editor-tab') {
        const newState = RichUtils.onTab(
          { preventDefault: () => {} },
          editorState,
          4
        );
        if (newState) {
          onChange(newState);
          return 'handled';
        }
        return 'not-handled';
      }

      const newState = RichUtils.handleKeyCommand(editorState, command);
      if (newState) {
        onChange(newState);
        return 'handled';
      }
      return 'not-handled';
    },
    [editorState, onChange, readOnly]
  );

  const handleImageInsert = useCallback(
    (url, alt = '') => {
      if (!url) return;

      const contentState = editorState.getCurrentContent();
      const contentStateWithEntity = contentState.createEntity(
        'IMAGE',
        'IMMUTABLE',
        { src: url, alt }
      );
      const entityKey = contentStateWithEntity.getLastCreatedEntityKey();
      const newEditorState = AtomicBlockUtils.insertAtomicBlock(
        editorState,
        entityKey,
        ' '
      );
      onChange(newEditorState);
    },
    [editorState, onChange]
  );

  const handleImageUpload = useCallback(() => {
    // Create a file input for image upload
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (e) => {
      const file = e.target.files[0];
      if (!file) return;

      // For mock, create an object URL
      const imageUrl = URL.createObjectURL(file);
      handleImageInsert(imageUrl, file.name);
    };
    input.click();
  }, [handleImageInsert]);

  const blockStyleFn = useCallback((contentBlock) => {
    const type = contentBlock.getType();
    switch (type) {
      case 'header-one':
        return 'text-2xl font-bold my-3';
      case 'header-two':
        return 'text-xl font-semibold my-2';
      case 'blockquote':
        return 'border-l-4 border-primary-300 pl-4 italic my-2 text-slate-600';
      case 'code-block':
        return 'bg-slate-100 rounded p-3 font-mono text-sm my-2 overflow-x-auto';
      case 'unordered-list-item':
        return 'list-disc ml-6 my-1';
      case 'ordered-list-item':
        return 'list-decimal ml-6 my-1';
      default:
        return 'my-1';
    }
  }, []);

  return (
    <div
      className={`border border-slate-200 rounded-lg bg-white ${
        readOnly ? 'opacity-80' : ''
      }`}
    >
      {!readOnly && (
        <Toolbar
          editorState={editorState}
          onChange={onChange}
          onImageClick={handleImageUpload}
        />
      )}

      <div
        className="px-4 py-3 min-h-[300px] cursor-text"
        onClick={focusEditor}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') focusEditor();
        }}
      >
        <Editor
          ref={editorRef}
          editorState={editorState}
          onChange={onChange}
          handleKeyCommand={handleKeyCommand}
          keyBindingFn={myKeyBindingFn}
          blockRendererFn={mediaBlockRenderer}
          blockStyleFn={blockStyleFn}
          readOnly={readOnly}
          placeholder={readOnly ? '' : placeholder}
        />
      </div>
    </div>
  );
}
