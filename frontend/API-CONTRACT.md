# EDMS API Contract

> Tài liệu này mô tả chi tiết các API mà frontend cần từ backend.
> Backend dev dùng làm spec để implement Lambda + API Gateway.
> Format response mặc định: `application/json`

---

## Mục lục

1. [Auth](#1-auth)
2. [Documents](#2-documents)
3. [Versions](#3-versions)
4. [Upload](#4-upload)
5. [Folders](#5-folders)
6. [Tags](#6-tags)
7. [Search](#7-search)
8. [Permissions](#8-permissions)
9. [Approval](#9-approval)
10. [Share](#10-share)
11. [Dashboard](#11-dashboard)
12. [OCR](#12-ocr)
13. [Common Types](#13-common-types)

---

## 1. Auth

### POST /auth/login

Login với email + password.

**Request Body:**
```json
{
  "email": "owner@edms.vn",
  "password": "any"
}
```

**Success Response (200):**
```json
{
  "token": "mock_jwt_...",
  "user": {
    "id": "u1",
    "email": "owner@edms.vn",
    "name": "Nguyen Van A",
    "role": "OWNER",
    "department": "Engineering",
    "avatar": null
  }
}
```

**Error Response (401):**
```json
{
  "error": "Invalid email or password"
}
```

### POST /auth/logout

**Headers:** `Authorization: Bearer <token>`

**Success (200):** `{ "message": "Logged out" }`

### GET /auth/me

Lấy thông tin user hiện tại từ token.

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "user": { "id": "u1", "email": "owner@edms.vn", "name": "Nguyen Van A", "role": "OWNER", "department": "Engineering" }
}
```

---

## 2. Documents

### GET /documents

List documents với filter, sort, pagination.

**Query Parameters:**

| Param | Type | Example | Description |
|-------|------|---------|-------------|
| `page` | number | `1` | Page number (1-based) |
| `limit` | number | `10` | Items per page |
| `q` | string | `"report"` | Full-text search |
| `sortBy` | string | `"updatedAt"` | Sort field |
| `sortOrder` | string | `"desc"` | `"asc"` or `"desc"` |
| `folderId` | string | `"f3"` | Filter by folder |
| `ownerId` | string | `"u1"` | Filter by owner |
| `status` | string | `"APPROVED"` | Filter by status |
| `type` | string | `"Report"` | Filter by type |

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "items": [
    {
      "id": "d1",
      "title": "Q1 Engineering Report",
      "type": "Report",
      "status": "APPROVED",
      "ownerId": "u1",
      "folderId": "f3",
      "content": "{\"blocks\":[...]}",
      "currentVersionId": "v3",
      "createdAt": "2025-03-15T10:00:00Z",
      "updatedAt": "2025-03-20T10:00:00Z"
    }
  ],
  "total": 10,
  "page": 1,
  "limit": 10,
  "totalPages": 1
}
```

### GET /documents/:id

Lấy chi tiết 1 document.

**Success (200):**
```json
{
  "id": "d1",
  "title": "Q1 Engineering Report",
  "type": "Report",
  "status": "APPROVED",
  "ownerId": "u1",
  "folderId": "f3",
  "content": "{\"blocks\":[...]}",
  "currentVersionId": "v3",
  "createdAt": "2025-03-15T10:00:00Z",
  "updatedAt": "2025-03-20T10:00:00Z"
}
```

**Error (404):** `{ "error": "Document not found" }`

### POST /documents

Tạo document mới.

**Request Body:**
```json
{
  "title": "New Document",
  "type": "Report",
  "folderId": "f3",
  "content": "{\"blocks\":[{\"key\":\"abc\",\"text\":\"Hello\",\"type\":\"unstyled\",\"depth\":0,\"inlineStyleRanges\":[],\"entityRanges\":[],\"data\":{}}],\"entityMap\":{}}"
}
```

**Success (201):** `{ "id": "d11", ...(full document) }`

### DELETE /documents/:id

Xóa document (soft delete).

**Headers:** `Authorization: Bearer <token>`

**Success (200):** `{ "message": "Deleted" }`

**Error (403):** `{ "error": "Forbidden" }` — không phải OWNER

### PATCH /documents/:id

Cập nhật document (title, content, folderId).

**Request Body (partial):**
```json
{
  "title": "Updated Title",
  "content": "{\"blocks\":[...]}"
}
```

**Success (200):** `{ "id": "d1", ...(full document) }`

---

## 3. Versions

### GET /documents/:id/versions

Lấy danh sách version của document.

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "items": [
    {
      "id": "v3",
      "documentId": "d1",
      "versionNumber": 3,
      "content": "{\"blocks\":[...]}",
      "createdBy": "u4",
      "createdAt": "2025-03-20T10:00:00Z"
    }
  ],
  "total": 3
}
```

### POST /documents/:id/versions

Tạo version mới (khi save).

**Request Body:**
```json
{
  "content": "{\"blocks\":[...]}",
  "createdBy": "u1"
}
```

**Success (201):**
```json
{
  "id": "v11",
  "documentId": "d1",
  "versionNumber": 4,
  "content": "...",
  "createdBy": "u1",
  "createdAt": "2025-04-01T12:00:00Z"
}
```

### POST /documents/:id/versions/rollback

Rollback về version cũ (tạo version mới với nội dung cũ).

**Request Body:**
```json
{
  "versionId": "v1"
}
```

**Success (200):** `{ "id": "v12", "versionNumber": 5, ...(new version with old content) }`

---

## 4. Upload

### POST /upload/url

Lấy presigned URL để upload file lên S3.

**Request Body:**
```json
{
  "fileName": "report.pdf",
  "fileType": "application/pdf"
}
```

**Success (200):**
```json
{
  "url": "https://bucket.s3.amazonaws.com/uploads/file_123?X-Amz...",
  "fileId": "file_123",
  "fields": { "key": "uploads/file_123", "bucket": "edms-docs" }
}
```

### POST /upload/confirm

Xác nhận upload hoàn tất → tạo document entry.

**Request Body:**
```json
{
  "fileId": "file_123",
  "fileName": "report.pdf",
  "fileType": "application/pdf",
  "ownerId": "u1"
}
```

**Success (201):**
```json
{
  "id": "d11",
  "title": "report",
  "type": "PDF",
  "status": "DRAFT",
  "ownerId": "u1",
  "createdAt": "2025-04-01T12:00:00Z"
}
```

---

## 5. Folders

### GET /folders

List tất cả folders.

**Success (200):**
```json
{
  "items": [
    { "id": "f1", "name": "Contracts", "department": "Engineering", "ownerId": "u1", "createdAt": "2025-03-01T..." },
    { "id": "f2", "name": "HR Documents", "department": "HR", "ownerId": "u3", "createdAt": "2025-03-05T..." }
  ]
}
```

### GET /folders/:id

Lấy chi tiết folder.

**Success (200):**
```json
{
  "id": "f1",
  "name": "Contracts",
  "department": "Engineering",
  "ownerId": "u1",
  "createdAt": "2025-03-01T10:00:00Z"
}
```

### POST /folders

Tạo folder mới.

**Request Body:**
```json
{
  "name": "New Folder",
  "department": "Engineering",
  "ownerId": "u1"
}
```

**Success (201):** `{ "id": "f4", "name": "New Folder", ... }`

### DELETE /folders/:id

Xóa folder.

**Success (200):** `{ "message": "Deleted" }`

---

## 6. Tags

### GET /documents/:id/tags

Lấy danh sách tag của document.

**Success (200):**
```json
{
  "items": [
    { "id": "t1", "name": "Urgent", "docTagId": "dt1" },
    { "id": "t4", "name": "Final", "docTagId": "dt2" }
  ]
}
```

### POST /documents/:id/tags

Thêm tag vào document. Nếu tag chưa tồn tại trong hệ thống → tạo mới.

**Request Body:**
```json
{
  "name": "Urgent"
}
```

**Success (201):**
```json
{
  "id": "t1",
  "name": "Urgent",
  "docTagId": "dt3"
}
```

### DELETE /documents/:id/tags/:docTagId

Xóa tag khỏi document (không xóa tag khỏi hệ thống).

**Success (200):** `{ "message": "Removed" }`

### GET /tags

Lấy tất cả tags available trong hệ thống.

**Success (200):**
```json
{
  "items": [
    { "id": "t1", "name": "Urgent" },
    { "id": "t2", "name": "Confidential" }
  ]
}
```

---

## 7. Search

### GET /search

Tìm kiếm documents với nhiều tiêu chí.

**Query Parameters:**

| Param | Type | Example | Description |
|-------|------|---------|-------------|
| `q` | string | `"report"` | Keyword search |
| `tags` | string | `"t1,t4"` | Comma-separated tag IDs |
| `types` | string | `"Report,Policy"` | Comma-separated types |
| `statuses` | string | `"DRAFT,PENDING"` | Comma-separated statuses |

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "items": [ ...(documents matching criteria) ],
  "total": 5
}
```

---

## 8. Permissions

### GET /documents/:id/permissions

Lấy danh sách permission của document.

**Success (200):**
```json
{
  "items": [
    { "id": "p1", "documentId": "d1", "userId": "u1", "role": "OWNER", "userName": "Nguyen Van A", "userEmail": "owner@edms.vn" },
    { "id": "p2", "documentId": "d1", "userId": "u2", "role": "EDITOR", "userName": "Tran Thi B", "userEmail": "editor@edms.vn" }
  ]
}
```

### POST /documents/:id/permissions

Grant quyền cho user.

**Request Body:**
```json
{
  "userId": "u3",
  "role": "EDITOR"
}
```

**Note:** `role` gồm `OWNER`, `EDITOR`, `VIEWER`. Chỉ OWNER mới có thể grant.

**Success (201):** `{ "id": "p11", "documentId": "d1", "userId": "u3", "role": "EDITOR" }`

### PUT /documents/:id/permissions/:permissionId

Cập nhật role của permission.

**Request Body:**
```json
{
  "role": "VIEWER"
}
```

**Success (200):** `{ "id": "p2", "role": "VIEWER", ... }`

### DELETE /documents/:id/permissions/:permissionId

Xóa permission. Không thể xóa OWNER.

**Success (200):** `{ "message": "Removed" }`

**Error (400):** `{ "error": "Cannot remove the Owner" }`

### GET /documents/:id/permissions/role?userId=u1

(Không bắt buộc) Kiểm tra role của user trên document.

**Success (200):**
```json
{
  "role": "EDITOR"
}
```

---

## 9. Approval

### POST /documents/:id/approval/submit

Submit document PENDING phê duyệt (chỉ OWNER, status phải DRAFT).

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "id": "d3",
  "status": "PENDING",
  "message": "Submitted for approval"
}
```

**Error (400):** `{ "error": "Only DRAFT documents can be submitted" }`

### POST /documents/:id/approval/approve

Phê duyệt document (chỉ MANAGER, status phải PENDING).

**Success (200):**
```json
{
  "id": "d3",
  "status": "APPROVED"
}
```

### POST /documents/:id/approval/reject

Từ chối document (chỉ MANAGER, status phải PENDING).

**Success (200):**
```json
{
  "id": "d3",
  "status": "REJECTED"
}
```

### GET /documents/:id/approval/history

Lấy lịch sử phê duyệt.

**Success (200):**
```json
{
  "items": [
    {
      "id": "a1",
      "documentId": "d3",
      "action": "SUBMIT",
      "fromStatus": "DRAFT",
      "toStatus": "PENDING",
      "timestamp": "2025-03-22T10:00:00Z"
    }
  ]
}
```

**Action values:** `SUBMIT`, `APPROVE`, `REJECT`

---

## 10. Share

### POST /documents/:id/share

Tạo share link.

**Request Body:**
```json
{
  "email": "partner@company.com",
  "ttlHours": 24
}
```

**Success (201):**
```json
{
  "id": "share_123",
  "documentId": "d1",
  "sharedWithEmail": "partner@company.com",
  "expiresAt": "2025-04-02T12:00:00Z",
  "link": "https://edms.app/share/share_123"
}
```

### GET /documents/:id/share

Lấy share link hiện tại (nếu có).

**Success (200):**
```json
{
  "link": "https://edms.app/share/share_123"
}
```

### GET /documents/:id/shares

Lấy tất cả share links của document.

**Success (200):**
```json
{
  "items": [ ...(share records) ]
}
```

---

## 11. Dashboard

### GET /dashboard/stats

Lấy thống kê cho Dashboard page.

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "totalDocuments": 10,
  "pendingApprovals": 2,
  "approvedThisMonth": 3,
  "totalDepartments": 3,
  "docsByDepartment": [
    { "name": "Engineering", "count": 5 },
    { "name": "HR", "count": 3 },
    { "name": "Management", "count": 2 }
  ],
  "docsByStatus": [
    { "status": "APPROVED", "count": 6 },
    { "status": "PENDING", "count": 2 },
    { "status": "DRAFT", "count": 2 }
  ]
}
```

---

## 12. OCR

### GET /documents/:id/ocr

Lấy kết quả OCR của document.

**Headers:** `Authorization: Bearer <token>`

**Success (200) — đã có OCR:**
```json
{
  "status": "completed",
  "text": "OCR Extracted Text...",
  "extractedAt": "2025-03-20T10:00:00Z"
}
```

**Success (200) — đang xử lý:**
```json
{
  "status": "processing",
  "text": null
}
```

**Success (200) — chưa có OCR:**
```json
{
  "status": "not_found",
  "text": null
}
```

### POST /documents/:id/ocr

Yêu cầu OCR processing cho document.

**Headers:** `Authorization: Bearer <token>`

**Success (200):**
```json
{
  "status": "processing",
  "message": "OCR processing started"
}
```

**Success (200) — đã xong:**
```json
{
  "status": "completed",
  "text": "Extracted text..."
}
```

---

## 13. Common Types

### Document Status

| Value | Description | Badge Color |
|-------|-------------|-------------|
| `DRAFT` | Đang soạn thảo | Amber |
| `PENDING` | Chờ phê duyệt | Cyan |
| `APPROVED` | Đã phê duyệt | Green |
| `REJECTED` | Bị từ chối | Red |

### Permission Roles

| Role | Quyền hạn |
|------|-----------|
| `OWNER` | Full quyền: xem, sửa, xóa, grant permission, submit approval |
| `EDITOR` | Xem, sửa nội dung (không xóa, không grant) |
| `VIEWER` | Chỉ xem (read-only) |
| `MANAGER` | Approve/reject documents |
| `ADMIN` | Toàn quyền hệ thống |

### Document Types

`"Report"`, `"Contract"`, `"Policy"`

### Paginated Response

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "limit": 20,
  "totalPages": 0
}
```

### Error Response

```json
{
  "error": "Human-readable error message"
}
```

HTTP status codes:
- `200` — Success
- `201` — Created
- `400` — Bad request
- `401` — Unauthorized (missing/invalid token)
- `403` — Forbidden (không đủ quyền)
- `404` — Not found
- `500` — Internal server error

---

## Authentication

Tất cả API (trừ login) đều cần header:

```
Authorization: Bearer <token>
```

Token lấy từ response của login, lưu trong localStorage key `edms_token`.
Frontend tự động gửi token qua `apiFetch()` helper.

Khi Cognito được tích hợp, token sẽ là Cognito JWT token thay vì mock string.
