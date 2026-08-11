import React, { useState, useRef, useEffect } from 'react';
import { Download, FileText, FileType, FileJson, File } from 'lucide-react';

const exportFormats = [
  { id: 'docx', label: 'Microsoft Word (.docx)', icon: FileText, mime: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' },
  { id: 'pdf', label: 'PDF (.pdf)', icon: FileType, mime: 'application/pdf' },
  { id: 'md', label: 'Markdown (.md)', icon: File, mime: 'text/markdown' },
  { id: 'note', label: 'Note (.note)', icon: FileJson, mime: 'text/plain' },
];

function ExportDropdown({ documentTitle }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleExport = (format) => {
    const safeTitle = (documentTitle || 'document').replace(/[^a-zA-Z0-9-_]/g, '_');
    const content = generateMockContent(format, documentTitle);

    const blob = new Blob([content], { type: format.mime });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${safeTitle}.${format.id}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    setOpen(false);
  };

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen(!open)}
        className="btn btn-secondary"
      >
        <Download size={16} />
        Export
      </button>

      {open && (
        <div className="absolute right-0 top-full mt-1 w-56 bg-white rounded-lg shadow-lg border border-slate-200 py-1 z-20">
          {exportFormats.map((format) => (
            <button
              key={format.id}
              onClick={() => handleExport(format)}
              className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
            >
              <format.icon size={16} className="text-slate-400" />
              {format.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default React.memo(ExportDropdown);

function generateMockContent(format, title) {
  const header = title || 'Untitled Document';
  const date = new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });

  switch (format.id) {
    case 'docx':
      return `PK\u0003\u0004DOCX mock - ${header}`;
    case 'pdf':
      return `%PDF-1.4 mock PDF - ${header}`;
    case 'md':
      return `# ${header}\n\n*Exported on ${date}*\n\n## Content\n\nThis is a mock export of "${header}".\n\nThe document was exported from EDMS on ${date}.\n\n---\n\n*EDMS - Enterprise Document Management System*`;
    case 'note':
      return `Note: ${header}\nDate: ${date}\n\nThis is a mock note export.\n\nContent will be available when the real export backend is integrated.`;
    default:
      return `Export - ${header}`;
  }
}
