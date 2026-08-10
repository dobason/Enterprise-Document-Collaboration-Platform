# EDMS — Hướng dẫn Fullstack & Kết nối AWS

> Tài liệu tổng quát **toàn bộ dự án fullstack** (Frontend React + Backend Spring Boot) — viết để **bất kỳ ai vào repo** đều nắm được kiến trúc hiện tại, cách chạy, cách gọi API, và **những gì cần biết khi kết nối các service AWS** (Phase 2).
>
> Tài liệu chi tiết theo từng phần: [`README.md`](./README.md) · [`EDMS-Serverless-Roadmap.md`](./EDMS-Serverless-Roadmap.md) · [`frontend/API-CONTRACT.md`](./frontend/API-CONTRACT.md) · [`backend/Contract-backend.md`](./backend/Contract-backend.md)

---

## 1. Tổng quan kiến trúc hiện tại (Phase 1 — Local)

```
┌──────────────────────────────┐         ┌──────────────────────────────────────┐
│  FRONTEND (React 18, CRA)    │  HTTP   │  BACKEND (Spring Boot 3, Java 17)    │
│  http://localhost:3000       │ ──────► │  http://localhost:8088               │
│  src/api/*.api.js            │  REST   │  Hexagonal: api → application →      │
│  (client.js: JWT Bearer)     │  JSON   │  domain ← infrastructure (adapters)  │
└──────────────────────────────┘         │  Profile "local":                    │
                                         │  • DB: H2 in-memory (Flyway + Seeder)│
                                         │  • Files: backend/uploads/           │
                                         │  • Auth: JWT tự sinh (jjwt)          │
                                         └──────────────────────────────────────┘
```

**Trạng thái hiện tại: đã kết nối frontend ↔ backend thật.** Không còn mock data ở frontend.

| Thành phần | Công nghệ | Ghi chú |
|---|---|---|
| Frontend | React 18 + CRA, Tailwind, Draft.js, lucide-react, mammoth, xlsx | JS thuần (không TS), không UI library |
| Backend | Spring Boot 3.2.5, Java 17, Maven | Hexagonal architecture (Ports & Adapters) |
| Auth | JWT (jjwt) qua `POST /auth/login` | Local: token tự sinh; AWS: Cognito |
| DB | H2 in-memory (`MODE=MySQL`) | ⚠️ **Mất data khi restart** (xem §7) |
| File storage | Thư mục `backend/uploads/` | Local; AWS: S3 |
| CORS | Cho phép mọi origin | Frontend :3000 gọi thẳng backend :8088 |

---

## 2. Chạy local

### Yêu cầu
- **Java 17+** (máy dev hiện tại: JDK 21 — compile target 17 OK)
- **Maven 3.8+** (máy dev hiện tại đã cài tại `C:\Tools\apache-maven-3.9.16`; mở terminal MỚI để `mvn` có trong PATH)
- Node 16+

### Backend — port 8088
```bash
cd backend
mvn spring-boot:run
```
- Mặc định profile `local` (cấu hình trong `application.yml` → `server.port: 8088`)
- Lần đầu chậm (~15-20s): Maven tải dependency + Spring Boot cold start — bình thường
- Swagger UI: `http://localhost:8088/swagger-ui.html`

### Backend — chạy với MySQL local (profile `mysql`)

Khi muốn backend dùng MySQL thay vì H2, chạy profile `mysql` và để MySQL local đang hoạt động trước:

```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"

cd backend
mvn spring-boot:run
``

### Frontend — port 3000
```bash
cd frontend
npm install   # lần đầu
npm start
```

### Backend — chạy với AWS / Aurora MySQL (profile `aws`)

Nếu muốn backend không dùng H2 local mà trỏ sang Aurora MySQL, hãy dùng profile `aws` và set biến môi trường trước khi chạy:

```powershell
$env:AURORA_ENDPOINT="<aurora-writer-endpoint>"
$env:AURORA_PORT="3306"
$env:DB_NAME="edms"
$env:DB_USER="<aurora-username>"
$env:DB_PASS="<aurora-password>"
$env:DB_USE_SSL="true"
$env:SPRING_PROFILES_ACTIVE="aws"

cd backend
mvn spring-boot:run
```

Ghi chú:
- Database `edms` phải tồn tại sẵn trên Aurora.
- `application-aws.yml` đang đọc các biến môi trường ở trên, không còn trỏ ngầm về `localhost`.
- Backend vẫn chạy trên port `8088`, frontend giữ nguyên `http://localhost:3000` nếu API URL chưa đổi.
- Nếu port `8088` đang bị chiếm bởi tiến trình cũ, tắt tiến trình đó rồi chạy lại.
- Nếu bạn test S3 upload trong profile `aws`, hãy dùng `POST /upload/url` rồi `PUT` vào URL backend trả về, không gửi file trực tiếp vào `POST /upload/url`.

### Tài khoản demo (seed bởi DataSeeder, mật khẩu chung: `Password123!`)
| Email | Role |
|---|---|
| owner@edms.vn | OWNER (tạo doc, grant quyền, submit duyệt) |
| editor@edms.vn | EDITOR |
| manager@edms.vn | MANAGER (approve/reject) |
| viewer@edms.vn | VIEWER (chỉ xem) |
| admin@edms.vn | ADMIN |

> ⚠️ Nếu `mvn` không chạy trong terminal: biến PATH mới chỉ hiệu lực với terminal mở SAU khi cài Maven, hoặc chạy `$env:Path += ";C:\Tools\apache-maven-3.9.16\bin"`.

---

## 3. Kiến trúc kết nối Frontend ↔ Backend

### Quy tắc bất biến
> **UI KHÔNG gọi API trực tiếp** — mọi request đi qua `frontend/src/api/*.api.js` → helper `apiFetch()` trong `src/api/client.js`.

### Luồng chuẩn
1. **Login**: `POST /auth/login` → `{token, user}` → lưu `localStorage` (`edms_token`, `edms_user`)
2. **Mọi request sau**: `client.js` tự đính `Authorization: Bearer <token>`
3. **Token hết hạn / 401**: `client.js` tự xóa session → user phải login lại
4. **DELETE trả 204 (body rỗng)**: `client.js` xử lý trả `null` (không crash)

### File cấu hình quan trọng
| File | Vai trò |
|---|---|
| `frontend/src/api/config.js` | `API_URL = "http://localhost:8088"` — **đổi chỗ này khi trỏ lên AWS** |
| `frontend/src/api/client.js` | `apiFetch()` + `getToken()` + `getStoredUser()` |
| `frontend/src/context/AuthContext.js` | state auth (login/logout/isAuthenticated) — giữ nguyên contract, không đổi khi chuyển AWS |

---

## 4. Danh sách REST API (implementation thực tế)

> ⚠️ **Khác biệt so với `frontend/API-CONTRACT.md` cũ — đây là sự thật đang chạy**, frontend đã code theo đúng bảng này.

### Auth & Users
| Method | Endpoint | Body / Query | Trả về |
|---|---|---|---|
| POST | `/auth/login` | `{email, password}` | `{token, user}` — **public, không cần token** |
| POST | `/auth/logout` | header token | `{message}` |
| GET | `/auth/me` | — | `{user}` |
| GET | `/users` | — | `{items: [UserDto]}` — danh bạ user (cho trang quyền) |

### Documents
| Method | Endpoint | Body / Query | Trả về |
|---|---|---|---|
| GET | `/documents` | `page, limit, sortBy, sortOrder, folderId` | `PageResponse<DocumentDto>` |
| GET | `/documents/{id}` | — | `DocumentDto` (có `fileName/fileType/s3Key`) |
| POST | `/documents` | `{title, type, folderId, content}` | 201 `DocumentDto` |
| PATCH | `/documents/{id}` | `{title, content, folderId}` | `DocumentDto` |
| DELETE | `/documents/{id}` | — | **204** |
| GET | `/documents/{id}/download` | — | bytes file gốc + `Content-Disposition` |

### Versions / Upload
| Method | Endpoint | Body | Trả về |
|---|---|---|---|
| GET | `/documents/{id}/versions` | — | `{items: [VersionDto]}` |
| POST | `/documents/{id}/versions` | `{content}` | `VersionDto` |
| POST | `/documents/{id}/versions/rollback` | `{versionId}` | `VersionDto` |
| POST | `/upload/url` | `{fileName, fileType}` | `{url, fileId, fields}` |
| PUT | `/upload/mock-put/{fileId}?fileName=` | **raw bytes** (file) | 200 — endpoint local thay cho S3 PUT |
| POST | `/upload/confirm` | `{fileId, fileName, fileType, ownerId}` | 201 `DocumentDto` |

### Folders / Tags
| Method | Endpoint | Body | Trả về |
|---|---|---|---|
| GET | `/folders` | — | `{items}` |
| GET | `/folders/{id}` | — | `FolderDto` |
| POST | `/folders` | `{name, department}` | 201 `FolderDto` |
| DELETE | `/folders/{id}` | — | 204 |
| GET | `/documents/{id}/tags` | — | `{items}` (có `docTagId`) |
| POST | `/documents/{id}/tags` | `{name}` | `DocTagDto` |
| DELETE | `/documents/{id}/tags/{docTagId}` | — | 204 |
| GET | `/tags` | — | `{items}` |

### Search / Permissions
| Method | Endpoint | Body / Query | Trả về |
|---|---|---|---|
| GET | `/search` | `q, type, status` — **KHÔNG có tags** | `{items, total}` |
| GET | `/documents/{id}/permissions` | — | `{items}` (có userName/userEmail) |
| POST | `/documents/{id}/permissions` | `{userId, role}` | 201 `PermissionDto` |
| PUT | `/documents/{id}/permissions/{permissionId}` | `{role}` | `PermissionDto` |
| DELETE | `/documents/{id}/permissions/{permissionId}` | — | 204 |
| GET | `/documents/{id}/permissions/role` | — | `{role}` của user hiện tại |

### Approval ⚠️ (khác contract cũ)
| Method | Endpoint | Body | Trả về |
|---|---|---|---|
| POST | `/approval/submit` | `{documentId}` | `{id, status, message}` |
| POST | `/approval/approve` | `{documentId}` | `{id, status}` |
| POST | `/approval/reject` | `{documentId, reason}` | `{id, status}` |
| GET | `/approval/history?documentId=` | — | `{items}` |

### Share / Dashboard / OCR
| Method | Endpoint | Body | Trả về |
|---|---|---|---|
| POST | `/documents/{id}/share` | `{email, ttlHours}` | `{link}` |
| GET | `/documents/{id}/share` | — | `ShareDto` |
| GET | `/documents/{id}/shares` | — | `{items}` |
| GET | `/dashboard/stats` | — | `DashboardStatsResponse` |
| GET | `/documents/{id}/ocr` | — | `{status, text, extractedAt}` |
| POST | `/documents/{id}/ocr` | — | `{status, text}` |

---

## 5. Luồng Upload / Download / Xem file (đã hoàn thiện)

```
Upload (frontend):
  POST /upload/url ──► {url, fileId}
  PUT  <url> (raw file bytes + Bearer token) ──► backend lưu vào backend/uploads/
  POST /upload/confirm ──► tạo Document (fileName/fileType/s3Key)

Xem file (frontend, click vào doc trong danh sách):
  GET /documents/{id}/download ──► bytes
  Render inline theo loại file:
    • Ảnh (png/jpg/gif/svg/webp/bmp) → <img>
    • PDF, text (txt/md/csv/json/...) → <iframe> (trình duyệt render)
    • DOCX → mammoth.js parse → HTML
    • XLSX/XLS → SheetJS parse → bảng HTML
    • PPTX & loại khác → thông báo "không xem trước được" + nút Download
```

- File vật lý lưu tại **`backend/uploads/`** (profile local)
- Tên file được sanitize (`Paths.get().getFileName()`) để chống path traversal
- File không tồn tại trên disk → `LocalStorageService.downloadFile()` trả chuỗi mock để không 500

---

## 6. Bảng map sang AWS (Phase 2) — KHÔNG đổi business logic

Backend dùng **Spring Profile** để đổi adapter mà không sửa tầng business:

```bash
SPRING_PROFILES_ACTIVE=aws mvn spring-boot:run   # sau khi implement adapter aws
```

| Port (interface) | Adapter Local (đang chạy) | Adapter AWS (cần implement — **chưa có**) |
|---|---|---|
| `StorageService` | `LocalStorageService` → `backend/uploads/` | `S3StorageService` → S3 + presigned URL thật |
| `AuthenticationService` | `LocalAuthenticationService` → JWT tự sinh | `CognitoAuthenticationService` → Cognito User Pool |
| `AuditService` | `JpaAuditService` → H2 | `DynamoAuditService` → DynamoDB |
| `WorkflowService` | `LocalWorkflowService` → Java state machine | `StepFunctionsWorkflowService` |
| `NotificationService` | `LoggingNotificationService` | `SnsNotificationService` |
| `OcrService` | `LocalOcrService` → mock text | `TextractOcrService` |
| `EventPublisher` | `LocalEventPublisher` | `EventBridgePublisher` |
| `SecretProvider` | `EnvironmentSecretProvider` | `SecretsManagerProvider` |
| Metadata DB | H2 / MySQL | Aurora Serverless v2 (MySQL engine) |

> Chi tiết: `backend/Contract-backend.md` §7. Toàn bộ `infrastructure/aws/` chưa tồn tại — đây là việc chính của Phase 2.

---

## 7. NHỮNG ĐIỀU PHẢI BIẾT khi kết nối AWS

### A. Backend
1. **Implement 8 adapter AWS** trong `backend/src/main/java/com/edms/infrastructure/aws/` (theo 8 port ở §6), đánh dấu `@Profile("aws")`
2. **Cấu hình** `application-aws.yml` (đã có khung) + `SPRING_PROFILES_ACTIVE=aws`
3. **DB**: Aurora Serverless v2 (MySQL engine) — dùng chung 100% Flyway migration + JPA entities (H2 hiện chạy `MODE=MySQL` nên schema tương thích)
4. **Upload**: presigned URL local (`/upload/mock-put`) sẽ thay bằng **S3 presigned URL thật** — frontend giữ nguyên flow (GET url → PUT → confirm), chỉ đổi URL trả về
5. **Đối chiếu endpoint**: bảng API ở §4 là nguồn chân lý hiện tại; nếu API Gateway cấu hình khác (vd approval path), phải sửa **frontend `src/api/*.api.js`** tương ứng — đừng đổi theo `API-CONTRACT.md` cũ

### B. Frontend
1. **`src/api/config.js`**: đổi `API_URL` → URL của API Gateway (vd `https://<api-id>.execute-api.ap-southeast-1.amazonaws.com/dev`)
2. **Cognito**: `amazon-cognito-identity-js` đã có trong `package.json` (chưa dùng). Sửa `src/context/AuthContext.js` + `src/api/auth.api.js`: login qua `CognitoUser.authenticateUser()`, token = Cognito JWT. **Giữ nguyên localStorage keys** `edms_token` / `edms_user` để phần còn lại không đổi
3. **CORS**: API Gateway + CloudFront phải cho phép origin của frontend

### C. Những giới hạn / lưu ý hiện tại
| Vấn đề | Chi tiết |
|---|---|
| **Data H2 in-memory** | Restart backend → metadata mất (file trong `backend/uploads/` vẫn còn nhưng thành "mồ côi"). Muốn giữ: đổi `jdbc:h2:file:./data/edmsdb` hoặc dùng MySQL profile |
| **Search không filter theo tag** | Backend `/search` chỉ hỗ trợ `q/type/status` — UI bỏ qua tag khi search |
| **PPTX chưa preview được** | Chỉ ảnh/PDF/text/DOCX/XLSX preview inline |
| **Approval không kiểm tra role** | `LocalWorkflowService` chỉ kiểm tra status DRAFT/PENDING — frontend tự chặn nút theo role |
| **`backend/target/` commit trước đó** | Đã thêm `.gitignore`; file jar 58MB cũ vẫn nằm trong git history — dọn bằng `git rm -r --cached backend/target` nếu muốn |

### D. Các bug backend đã fix (khỏi fix lại)
- `countByStatusAndDeletedAtIsNull`: query sai `SELECT d` trả về `long` → đã sửa `SELECT COUNT(d)` (fix Dashboard 500)
- `SearchApplicationService`: `lower()` trên cột CLOB bị Hibernate 6.4 reject → đã sửa (lower chỉ trên title)
- `GET /documents` bổ sung filter `folderId`; thêm `GET /users` (danh bạ cho PermissionManager)

---

## 8. Chạy test & verify

```bash
# Backend (unit test MockMvc — 14 tests hiện tại)
cd backend && mvn test

# Frontend (build kiểm tra compile + import)
cd frontend && npm run build

# Smoke test E2E nhanh (backend đang chạy)
# login → upload → confirm → download → đối chiếu bytes
```

---

## 9. Tài liệu liên quan

| File | Nội dung |
|---|---|
| [`README.md`](./README.md) | Kiến trúc v2, mô hình dữ liệu, ERD, AWS services |
| [`EDMS-Serverless-Roadmap.md`](./EDMS-Serverless-Roadmap.md) | Kiến trúc chi tiết, lý do chọn dịch vụ, clean-up chi phí |
| [`EDMS-Master-Checklist.md`](./EDMS-Master-Checklist.md) | Phân vai trò, lộ trình tuần, checklist |
| [`backend/Contract-backend.md`](./backend/Contract-backend.md) | Contract backend, hexagonal architecture, mapping AWS |
| [`frontend/API-CONTRACT.md`](./frontend/API-CONTRACT.md) | Contract API gốc (⚠️ một số endpoint đã lệch — xem §4) |
| [`frontend/README.md`](./frontend/README.md) | Hướng dẫn frontend |
