# EDMS Frontend — React SPA

Enterprise Document Management System frontend. Built with **React 18**, **Tailwind CSS**, **Lucide React** icons, and **Draft.js** rich-text editor.

---

## Quick Start

```bash
cd frontend
npm install
npm start
```

Mở `http://localhost:3000`. Login với bất kỳ email + password nào (không cần backend).

### Build

```bash
npm run build
```

Output: `build/` — static files ready to deploy (S3, CloudFront, etc.)

---

## Kiến trúc

```
frontend/
├── public/index.html              # HTML shell
├── src/
│   ├── index.js                   # Entry point
│   ├── index.css                  # Tailwind directives + component classes
│   ├── App.js                     # Router (10 routes)
│   ├── api/
│   │   ├── mock/                  # ← Mock data layer (swap khi có backend)
│   │   │   ├── seed.js            # Seed data: users, documents, versions...
│   │   │   ├── engine.js          # Mock engine: CRUD, search, paginate, delay
│   │   │   └── data.js            # Singleton data store
│   │   ├── documents.api.js       # ← Swap file này để gọi API thật
│   │   ├── auth.api.js
│   │   ├── versions.api.js
│   │   ├── upload.api.js
│   │   ├── folders.api.js
│   │   ├── tags.api.js
│   │   ├── search.api.js
│   │   ├── permissions.api.js
│   │   ├── approval.api.js
│   │   ├── share.api.js
│   │   ├── dashboard.api.js
│   │   └── ocr.api.js
│   ├── context/
│   │   ├── AuthContext.js         # Auth state (swap Cognito sau này)
│   │   └── ToastContext.js        # Toast notifications
│   ├── components/                # 14 components (Layout, Editor, Upload...)
│   └── pages/                     # 9 pages (Login, Dashboard, Editor...)
├── tailwind.config.js
├── postcss.config.js
└── package.json
```

### Component Tree

```
App (ErrorBoundary)
└── AuthProvider (user, token, login/logout)
    └── ToastProvider
        └── BrowserRouter
            ├── /login → LoginPage
            └── ProtectedRoute (check auth)
                └── Layout (Sidebar + TopBar)
                    └── Routes
                        ├── / → DashboardPage
                        ├── /documents → DocumentListPage
                        ├── /documents/:id → DocumentEditorPage (lazy)
                        ├── /documents/:id/versions → VersionHistoryPage
                        ├── /documents/:id/permissions → PermissionManagerPage
                        ├── /documents/:id/approval → ApprovalPage
                        ├── /search → SearchPage
                        └── /folders/:id → FolderDetailPage
```

---

## Mock Data Layer

Hiện tại **chưa có backend** — toàn bộ dữ liệu dùng mock in-memory:

| File | Vai trò |
|------|---------|
| `src/api/mock/seed.js` | 5 users, 10 documents (rich content), 3 folders, tags, versions, permissions |
| `src/api/mock/engine.js` | CRUD + search + paginate + delay 200-500ms + 5% fail rate (configurable) |
| `src/api/mock/data.js` | Singleton data store, khởi tạo từ seed |

Cơ chế: mỗi file `src/api/*.api.js` export các async function gọi vào mock engine:

```js
// documents.api.js — hiện tại (mock)
export async function listDocuments(params) {
  return mockEngine.query('documents', params);
}
```

Khi có API thật, chỉ cần sửa dòng gọi:

```js
// documents.api.js — sau khi có backend
export async function listDocuments(params) {
  return apiFetch('/documents', { params: new URLSearchParams(params) });
}
```

---

## Hướng dẫn gắn API thật cho Backend Dev

### Bước 1: Cập nhật config

Mở `src/api/config.js`:

```js
export const CONFIG = {
  API_URL: "https://<api-id>.execute-api.ap-southeast-1.amazonaws.com/dev",
  USER_POOL_ID: "<từ Cognito>",
  USER_POOL_CLIENT_ID: "<từ Cognito>",
  REGION: "ap-southeast-1",
};
```

### Bước 2: Swap từng file `.api.js`

Mỗi file trong `src/api/` đều có cấu trúc giống nhau:

```js
// === Mock version (current) ===
// Comment 2 dòng dưới khi có API thật
import { mockEngine } from './mock/engine';
export async function getDocument(id) {
  return mockEngine.get('documents', id);
}

// === Real API version (swap khi có backend) ===
// Bỏ comment 4 dòng dưới, comment 2 dòng trên
// import { apiFetch } from './client';
// export async function getDocument(id) {
//   return apiFetch(`/documents/${id}`);
// }
```

Danh sách file cần swap:

| File | Endpoint | Method |
|------|----------|--------|
| `auth.api.js` | `/auth/login`, `/auth/logout` | POST |
| `documents.api.js` | `/documents` | GET, POST, DELETE |
| `versions.api.js` | `/documents/:id/versions` | GET, POST |
| `upload.api.js` | `/upload/url`, `/upload/confirm` | POST |
| `folders.api.js` | `/folders` | GET, POST |
| `tags.api.js` | `/documents/:id/tags` | GET, POST, DELETE |
| `search.api.js` | `/search` | GET |
| `permissions.api.js` | `/documents/:id/permissions` | GET, POST, PUT, DELETE |
| `approval.api.js` | `/documents/:id/approval` | POST |
| `share.api.js` | `/documents/:id/share` | POST, GET |
| `dashboard.api.js` | `/dashboard/stats` | GET |
| `ocr.api.js` | `/documents/:id/ocr` | GET, POST |

### Bước 3: Auth swap (khi Cognito ready)

Sửa `src/context/AuthContext.js`:
- Import `amazon-cognito-identity-js` (đã có sẵn trong `package.json`)
- `login()` gọi `CognitoUser.authenticateUser()` thay vì mock
- `logout()` gọi `CognitoUser.signOut()`

### Nguyên tắc

> **Không sửa file UI.** Mọi thay đổi chỉ trong `src/api/` và `src/context/AuthContext.js`.

### API Contract

Backend API cần trả về format:

```json
{
  "items": [{ "id": "d1", "title": "...", "type": "Report", "status": "APPROVED", ... }],
  "total": 10,
  "page": 1,
  "limit": 20,
  "totalPages": 1
}
```

Document fields:

| Field | Type | Example |
|-------|------|---------|
| `id` | string | `"d1"` |
| `title` | string | `"Q1 Engineering Report"` |
| `type` | string | `"Report"`, `"Contract"`, `"Policy"` |
| `status` | string | `"DRAFT"`, `"PENDING"`, `"APPROVED"`, `"REJECTED"` |
| `ownerId` | string | `"u1"` |
| `folderId` | string | `"f3"` |
| `content` | string | JSON.stringify(RawDraftContentState) |
| `createdAt` | string (ISO) | `"2025-03-15T..."` |
| `updatedAt` | string (ISO) | `"2025-03-20T..."` |

---

## Công nghệ

| Package | Version |用途 |
|---------|---------|-----|
| React | ^18.2.0 | UI framework |
| react-router-dom | ^6.23.0 | Client-side routing |
| Draft.js | ^0.11.7 | Rich-text editor |
| Tailwind CSS | ^3.4.19 | Utility-first CSS |
| Lucide React | ^1.27.0 | Icons (no emoji) |
| amazon-cognito-identity-js | ^6.3.7 | Cognito auth (chưa dùng) |

## Must-Not-Have

- ❌ KHÔNG gọi API backend thật trực tiếp từ UI — qua `src/api/*.api.js`
- ❌ KHÔNG dùng TypeScript — JSX + JSDoc
- ❌ KHÔNG dùng Redux/Zustand — React Context đủ
- ❌ KHÔNG dùng UI library (MUI/Chakra/Ant)
- ❌ KHÔNG dùng emoji làm icon — Lucide React
