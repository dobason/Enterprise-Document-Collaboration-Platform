# EDMS Frontend — React SPA

Enterprise Document Management System frontend. Built with **React 18**, **Tailwind CSS**, **Lucide React** icons, and **Draft.js** rich-text editor.

---

## Quick Start

```bash
cd frontend
npm install
npm start
```

Mở `http://localhost:3000`. Login bằng tài khoản demo: `owner@edms.vn` / `editor@edms.vn` / `manager@edms.vn` / `viewer@edms.vn` / `admin@edms.vn` (mật khẩu: `Password123!`).

> Yêu cầu backend Spring Boot đang chạy ở `http://localhost:8088` (xem `backend/`, chạy bằng `mvn spring-boot:run`, profile `local` dùng H2 in-memory).

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
│   │   ├── client.js              # HTTP client chung: JWT Bearer, xử lý lỗi, 204 No Content
│   │   ├── config.js              # API_URL → http://localhost:8088
│   │   ├── documents.api.js
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
│   │   ├── ocr.api.js
│   │   └── users.api.js
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

## Kết nối Backend

Frontend gọi **backend Spring Boot thật** qua REST API tại `http://localhost:8088` (cấu hình trong `src/api/config.js`):

- Mọi request đi qua helper `apiFetch()` trong `src/api/client.js` — tự đính kèm `Authorization: Bearer <token>`, xử lý lỗi chuẩn `{ "error": "..." }` và response `204 No Content`.
- Token JWT lấy từ `POST /auth/login`, lưu trong `localStorage` (`edms_token` / `edms_user`).
- Mỗi module là một file `src/api/*.api.js` export các async function tương ứng với endpoint backend (documents, versions, folders, tags, search, permissions, approval, share, dashboard, ocr, users).

Danh sách endpoint chính:

| File | Endpoint |
|------|----------|
| `auth.api.js` | `POST /auth/login`, `POST /auth/logout` |
| `documents.api.js` | `GET/POST /documents`, `GET/PATCH/DELETE /documents/{id}` |
| `versions.api.js` | `GET/POST /documents/{id}/versions`, `POST /documents/{id}/versions/rollback` |
| `upload.api.js` | `POST /upload/url`, `POST /upload/confirm` |
| `folders.api.js` | `GET/POST /folders`, `GET/DELETE /folders/{id}` |
| `tags.api.js` | `GET/POST /documents/{id}/tags`, `DELETE /documents/{id}/tags/{docTagId}`, `GET /tags` |
| `search.api.js` | `GET /search` |
| `permissions.api.js` | `GET/POST /documents/{id}/permissions`, `PUT/DELETE /documents/{id}/permissions/{permissionId}` |
| `approval.api.js` | `POST /approval/submit|approve|reject`, `GET /approval/history` |
| `share.api.js` | `POST/GET /documents/{id}/share`, `GET /documents/{id}/shares` |
| `dashboard.api.js` | `GET /dashboard/stats` |
| `ocr.api.js` | `GET/POST /documents/{id}/ocr` |
| `users.api.js` | `GET /users` |

### Nguyên tắc

> **Không sửa file UI.** Mọi thay đổi chỉ trong `src/api/` và `src/context/AuthContext.js`.

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
