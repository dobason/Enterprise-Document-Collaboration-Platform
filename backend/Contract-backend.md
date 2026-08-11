# Contract — EDMS Java Backend (`backend-java`)
**Thư mục mục tiêu**: `backend`  
**Nguồn thông tin chính (Source of Truth)**: `README.md` & `frontend/API-CONTRACT.md`

---

## 1. Mục tiêu Dự án & Chiến lược Kiến trúc

### 1.1 Mục tiêu Theo Từng Giai đoạn
- **Phase 1 (Chạy Hoàn chỉnh ở Local)**:
  - Triển khai backend 100% bằng **Java 17 / Spring Boot 3** trong thư mục `backend-java`.
  - Hoàn thành đầy đủ **37 REST API Endpoints** theo đúng `API-CONTRACT.md`.
  - Thực thi đúng **100% Business Rules** theo `README.md`.
  - Chạy hoàn toàn trên máy local với **MySQL / H2, Local File Storage, Spring Security + JWT, Java Approval State Machine, Mock OCR, Logging Notification, JPA Audit Logging**.
  - Đảm bảo 100% Unit Tests, Controller Tests (MockMvc), Repository Tests (`@DataJpaTest`) và Integration Tests vượt qua.

- **Phase 2 (Migrate lên AWS Serverless)**:
  - Deploy hệ thống lên AWS sử dụng **Aurora Serverless v2, Amazon S3, AWS Cognito, Amazon DynamoDB, AWS Step Functions, AWS Textract, Amazon SNS, Amazon EventBridge, AWS Secrets Manager, CloudWatch, X-Ray**.
  - **Không viết lại (Zero Rewrite) bất kỳ dòng Business Logic nào**. Chỉ thay đổi triển khai hạ tầng (Infrastructure Adapters) thông qua Spring Profile.

---

## 2. Phân tích & Đối chiếu README.md với API-CONTRACT.md

### Nguyên tắc Xử lý Bất đồng bộ / Khác biệt giữa 2 Tài liệu:
1. **Ưu tiên README.md cho Business Rules**: Quy trình phê duyệt tài liệu (DRAFT -> PENDING -> APPROVED / REJECTED), Phân quyền OWNER/EDITOR/VIEWER, Soft delete tài liệu, Phê duyệt bởi MANAGER, Ghi nhận Audit log, Thống kê phòng ban.
2. **Ưu tiên API-CONTRACT.md cho API Interface**: Endpoint URL path, HTTP Method, Request/Response DTO JSON Schema, Headers (`Authorization: Bearer <token>`), HTTP Status Codes (`200`, `201`, `400`, `401`, `403`, `404`, `500`).

### Bảng Liệt kê Chi tiết Khác biệt & Giải pháp Quy chuẩn:

| STT | Khía cạnh | Nội dung trong README.md | Nội dung trong API-CONTRACT.md | Giải pháp Quy chuẩn Kiến trúc Clean |
|---|---|---|---|---|
| 1 | **ID Định danh** | Dùng `int id PK` kiểu số nguyên tự tăng. | Dùng chuỗi string dạng `"d1"`, `"u1"`, `"f3"`, `"v3"`, `"p1"`, `"t1"`, `"share_123"`. | Entity dùng Primary Key kiểu `String` (UUID / Short ID string), DTO trả về string ID đúng API Contract. |
| 2 | **Cấu trúc Document** | Thuộc tính `fileName`, `fileType`, `s3Key`. | Thuộc tính `title`, `type` (Report/Contract/Policy), `content` (RichText JSON), `currentVersionId`, `folderId`, `ownerId`. | Entity `Document` lưu đầy đủ cả metadata file (`fileName`, `fileType`, `s3Key`) và nội dung RichText JSON (`title`, `type`, `content`). |
| 3 | **Cấu trúc Folder** | Có `departmentId FK` và `ownerId FK`. | Thuộc tính `department` string (ví dụ `"Engineering"`), `name`, `ownerId`. | Domain/Entity lưu quan hệ `department`, DTO ánh xạ chuyển đổi trả về `department` name dạng String. |
| 4 | **Xác thực (Auth)** | AWS Cognito User Pool & Groups. | REST API POST `/auth/login`, `/auth/logout`, GET `/auth/me` trả về Bearer JWT token & User DTO. | Thiết kế Port `AuthenticationService`. Profile `local` dùng `JwtAuthenticationService`, Profile `aws` dùng `CognitoAuthenticationService`. |
| 5 | **Phê duyệt Document** | AWS Step Functions State Machine (`approval.asl.json`). | REST API POST `/approval/submit`, `/approve`, `/reject` và GET `/history`. | Thiết kế Port `WorkflowService`. Profile `local` dùng `LocalApprovalWorkflowService` (Java State Machine + DB), Profile `aws` kích hoạt Step Functions. |
| 6 | **OCR Processing** | Xử lý trích xuất chữ từ S3. | REST GET & POST `/documents/:id/ocr` trả về status & text. | Thiết kế Port `OcrService`. Profile `local` dùng `MockOcrService`, Profile `aws` gọi AWS Textract. |
| 7 | **Audit Log** | DynamoDB table `AuditLog` (`PK=DOC#id`, `SK=LOG#timestamp`). | Ghi log ngầm thao tác hệ thống phía backend. | Thiết kế Port `AuditService`. Profile `local` dùng `JpaAuditService` (MySQL), Profile `aws` dùng `DynamoAuditService`. |

---

## 3. Kiến trúc Clean / Hexagonal Architecture (Ports & Adapters)

### 3.1 Mô hình Phân tầng
```
                       +--------------------------------------------------------+
                       |                       API Layer                        |
                       |  (REST Controllers, DTOs, Mappers, Global Exceptions)   |
                       +---------------------------+----------------------------+
                                                   |
                                                   v
                       +--------------------------------------------------------+
                       |                   Application Layer                    |
                       |  (Use Cases, Business Services, Domain Event Handlers) |
                       +---------------------------+----------------------------+
                                                   |
                                         [ Injects 8 Ports ]
                                                   |
                       +---------------------------+----------------------------+
                       |               Infrastructure Layer (Adapters)          |
                       |  +--------------------------+-----------------------+  |
                       |  |     Profile: "local"     |     Profile: "aws"    |  |
                       |  +--------------------------+-----------------------+  |
                       |  | LocalStorageService      | S3StorageService      |  |
                       |  | JwtAuthService           | CognitoAuthService    |  |
                       |  | JpaAuditService          | DynamoAuditService    |  |
                       |  | LocalApprovalWorkflow    | StepFunctionsWorkflow |  |
                       |  | LoggingNotifService      | SnsNotifService       |  |
                       |  | MockOcrService           | TextractOcrService    |  |
                       |  | LocalEventPublisher      | EventBridgePublisher  |  |
                       |  | EnvironmentSecretProvider| SecretsManagerProvider|  |
                       |  +--------------------------+-----------------------+  |
                       +--------------------------------------------------------+
```

### 3.2 Định nghĩa 8 Core Ports (Interfaces trong Tầng Application)

```java
// 1. StorageService Port
public interface StorageService {
    String generatePresignedUploadUrl(String fileId, String fileName, String contentType);
    void uploadFile(String key, byte[] content, String contentType);
    byte[] downloadFile(String key);
    void deleteFile(String key);
}

// 2. AuthenticationService Port
public interface AuthenticationService {
    AuthResponse login(LoginRequest request);
    UserDto getCurrentUser(String token);
    void logout(String token);
}

// 3. AuditService Port
public interface AuditService {
    void log(String documentId, AuditAction action, String performedBy, String details);
}

// 4. WorkflowService Port
public interface WorkflowService {
    void submitForApproval(String documentId, String submittedBy);
    void approveDocument(String documentId, String approvedBy);
    void rejectDocument(String documentId, String rejectedBy, String reason);
}

// 5. NotificationService Port
public interface NotificationService {
    void sendNotification(String recipientEmail, String subject, String message);
}

// 6. OcrService Port
public interface OcrService {
    OcrResultDto processOcr(String documentId, String s3Key);
    OcrResultDto getOcrResult(String documentId);
}

// 7. EventPublisher Port
public interface EventPublisher {
    void publish(DomainEvent event);
}

// 8. SecretProvider Port
public interface SecretProvider {
    String getSecret(String secretName);
}
```

---

## 4. Flyway Database Schema Script (`V1__init_schema.sql`)

Triển khai cho **MySQL (Phase Local)** và **Aurora Serverless v2 (Phase AWS)** mà **không đổi Entity/Repository**:

```sql
-- 1. Departments Table
CREATE TABLE departments (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Users Table
CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY,
    cognito_sub VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    department VARCHAR(255),
    department_id VARCHAR(64),
    avatar VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Folders Table
CREATE TABLE folders (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(255),
    department_id VARCHAR(64),
    owner_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_folders_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_folders_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Documents Table
CREATE TABLE documents (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    owner_id VARCHAR(64) NOT NULL,
    folder_id VARCHAR(64),
    department_id VARCHAR(64),
    content LONGTEXT,
    file_name VARCHAR(255),
    file_type VARCHAR(100),
    s3_key VARCHAR(512),
    current_version_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_documents_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_documents_folder FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    CONSTRAINT fk_documents_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Document Versions Table
CREATE TABLE document_versions (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    version_number INT NOT NULL,
    content LONGTEXT,
    s3_key VARCHAR(512),
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_versions_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_versions_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_doc_version UNIQUE(document_id, version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Permissions Table
CREATE TABLE permissions (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_permissions_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_doc_user_permission UNIQUE(document_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Tags Table
CREATE TABLE tags (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Document Tags Table
CREATE TABLE document_tags (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    tag_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctags_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT uq_doc_tag UNIQUE(document_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Shares Table
CREATE TABLE shares (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    shared_by VARCHAR(64) NOT NULL,
    shared_with_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shares_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_shared_by FOREIGN KEY (shared_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Approval Histories Table
CREATE TABLE approval_histories (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL,
    action VARCHAR(50) NOT NULL,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    performed_by VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_user FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. OCR Results Table
CREATE TABLE ocr_results (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'not_found',
    text LONGTEXT,
    extracted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_ocr_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Local Audit Logs Table
CREATE TABLE audit_logs (
    id VARCHAR(64) PRIMARY KEY,
    document_id VARCHAR(64),
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(64) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Database Indexes
CREATE INDEX idx_documents_owner ON documents(owner_id);
CREATE INDEX idx_documents_folder ON documents(folder_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_deleted_at ON documents(deleted_at);
CREATE INDEX idx_versions_document ON document_versions(document_id);
CREATE INDEX idx_permissions_doc_user ON permissions(document_id, user_id);
CREATE INDEX idx_doc_tags_doc ON document_tags(document_id);
CREATE INDEX idx_doc_tags_tag ON document_tags(tag_id);
CREATE INDEX idx_shares_doc ON shares(document_id);
CREATE INDEX idx_shares_token ON shares(token);
CREATE INDEX idx_approval_doc ON approval_histories(document_id);
CREATE INDEX idx_audit_doc ON audit_logs(document_id);
```

---

## 5. Danh sách 37 REST API Endpoints Chuẩn hóa theo API-CONTRACT.md

| STT | Module | Method | Endpoint Path | Request DTO | Response DTO | Status Code |
|---|---|---|---|---|---|---|
| 1 | Auth | `POST` | `/auth/login` | `LoginRequest` (email, password) | `AuthResponse` (token, user) | `200` / `401` |
| 2 | Auth | `POST` | `/auth/logout` | Header `Authorization` | `{ "message": "Logged out" }` | `200` |
| 3 | Auth | `GET` | `/auth/me` | Header `Authorization` | `UserMeResponse` (user) | `200` / `401` |
| 4 | Documents | `GET` | `/documents` | Query: `page, limit, q, sortBy, sortOrder, folderId, ownerId, status, type` | `PageResponse<DocumentDto>` | `200` |
| 5 | Documents | `GET` | `/documents/{id}` | Path `{id}` | `DocumentDto` | `200` / `404` |
| 6 | Documents | `POST` | `/documents` | `CreateDocumentRequest` (title, type, folderId, content) | `DocumentDto` | `201` / `400` |
| 7 | Documents | `DELETE` | `/documents/{id}` | Path `{id}` | `{ "message": "Deleted" }` | `200` / `403` / `404` |
| 8 | Documents | `PATCH` | `/documents/{id}` | `UpdateDocumentRequest` (title, content, folderId) | `DocumentDto` | `200` / `404` |
| 9 | Versions | `GET` | `/documents/{id}/versions` | Path `{id}` | `VersionListResponse` | `200` |
| 10 | Versions | `POST` | `/documents/{id}/versions` | `CreateVersionRequest` (content, createdBy) | `VersionDto` | `201` |
| 11 | Versions | `POST` | `/documents/{id}/versions/rollback` | `RollbackVersionRequest` (versionId) | `VersionDto` | `200` / `404` |
| 12 | Upload | `POST` | `/upload/url` | `PresignedUrlRequest` (fileName, fileType) | `PresignedUrlResponse` | `200` |
| 13 | Upload | `POST` | `/upload/confirm` | `UploadConfirmRequest` (fileId, fileName, fileType, ownerId) | `DocumentDto` | `201` |
| 14 | Folders | `GET` | `/folders` | None | `FolderListResponse` | `200` |
| 15 | Folders | `GET` | `/folders/{id}` | Path `{id}` | `FolderDto` | `200` / `404` |
| 16 | Folders | `POST` | `/folders` | `CreateFolderRequest` (name, department, ownerId) | `FolderDto` | `201` |
| 17 | Folders | `DELETE` | `/folders/{id}` | Path `{id}` | `{ "message": "Deleted" }` | `200` / `404` |
| 18 | Tags | `GET` | `/documents/{id}/tags` | Path `{id}` | `DocTagListResponse` | `200` |
| 19 | Tags | `POST` | `/documents/{id}/tags` | `AddTagRequest` (name) | `DocTagDto` | `201` |
| 20 | Tags | `DELETE` | `/documents/{id}/tags/{docTagId}` | Path `{id}, {docTagId}` | `{ "message": "Removed" }` | `200` |
| 21 | Tags | `GET` | `/tags` | None | `TagListResponse` | `200` |
| 22 | Search | `GET` | `/search` | Query: `q, tags, types, statuses` | `SearchResponse` | `200` |
| 23 | Permissions | `GET` | `/documents/{id}/permissions` | Path `{id}` | `PermissionListResponse` | `200` |
| 24 | Permissions | `POST` | `/documents/{id}/permissions` | `GrantPermissionRequest` (userId, role) | `PermissionDto` | `201` / `403` |
| 25 | Permissions | `PUT` | `/documents/{id}/permissions/{permissionId}` | `UpdatePermissionRequest` (role) | `PermissionDto` | `200` |
| 26 | Permissions | `DELETE` | `/documents/{id}/permissions/{permissionId}` | Path `{id}, {permissionId}` | `{ "message": "Removed" }` | `200` / `400` |
| 27 | Permissions | `GET` | `/documents/{id}/permissions/role` | Query: `userId` | `UserRoleResponse` | `200` |
| 28 | Approval | `POST` | `/documents/{id}/approval/submit` | Path `{id}` | `ApprovalSubmitResponse` | `200` / `400` |
| 29 | Approval | `POST` | `/documents/{id}/approval/approve` | Path `{id}` | `ApprovalActionResponse` | `200` / `400` |
| 30 | Approval | `POST` | `/documents/{id}/approval/reject` | Path `{id}` | `ApprovalActionResponse` | `200` / `400` |
| 31 | Approval | `GET` | `/documents/{id}/approval/history` | Path `{id}` | `ApprovalHistoryListResponse` | `200` |
| 32 | Share | `POST` | `/documents/{id}/share` | `CreateShareRequest` (email, ttlHours) | `ShareDto` | `201` |
| 33 | Share | `GET` | `/documents/{id}/share` | Path `{id}` | `ShareLinkResponse` | `200` |
| 34 | Share | `GET` | `/documents/{id}/shares` | Path `{id}` | `ShareListResponse` | `200` |
| 35 | Dashboard | `GET` | `/dashboard/stats` | Header `Authorization` | `DashboardStatsResponse` | `200` |
| 36 | OCR | `GET` | `/documents/{id}/ocr` | Path `{id}` | `OcrResultResponse` | `200` |
| 37 | OCR | `POST` | `/documents/{id}/ocr` | Path `{id}` | `OcrActionResponse` | `200` |

---

## 6. Structure Package Chi tiết trong `backend-java`

```
backend-java/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/edms/
    │   │   ├── EdmsApplication.java
    │   │   │
    │   │   ├── domain/                         # Domain layer (Zero External Dependencies)
    │   │   │   ├── model/ (Document, User, Folder, Version, Permission, Tag, Share, ApprovalHistory)
    │   │   │   ├── enums/ (DocumentStatus, PermissionRole, UserRole, ApprovalAction, AuditAction)
    │   │   │   └── events/ (DocumentUploadedEvent, DocumentApprovedEvent, PermissionGrantedEvent)
    │   │   │
    │   │   ├── application/                    # Application Layer (Use Cases & Business Logic)
    │   │   │   ├── ports/                      # 8 Core Interfaces
    │   │   │   │   ├── StorageService.java
    │   │   │   │   ├── AuthenticationService.java
    │   │   │   │   ├── AuditService.java
    │   │   │   │   ├── WorkflowService.java
    │   │   │   │   ├── NotificationService.java
    │   │   │   │   ├── OcrService.java
    │   │   │   │   ├── EventPublisher.java
    │   │   │   │   └── SecretProvider.java
    │   │   │   └── service/                    # Business Service Implementations
    │   │   │       ├── DocumentServiceImpl.java
    │   │   │       ├── FolderServiceImpl.java
    │   │   │       ├── VersionServiceImpl.java
    │   │   │       ├── PermissionServiceImpl.java
    │   │   │       ├── ApprovalServiceImpl.java
    │   │   │       ├── TagServiceImpl.java
    │   │   │       ├── ShareServiceImpl.java
    │   │   │       ├── SearchServiceImpl.java
    │   │   │       ├── DashboardServiceImpl.java
    │   │   │       └── UserServiceImpl.java
    │   │   │
    │   │   ├── infrastructure/                 # Infrastructure Adapters (Local vs AWS)
    │   │   │   ├── config/                     # Security, CORS, Swagger, Profile Config
    │   │   │   │   ├── SecurityConfig.java
    │   │   │   │   ├── CorsConfig.java
    │   │   │   │   ├── SwaggerConfig.java
    │   │   │   │   └── ProfileConfig.java
    │   │   │   ├── persistence/                # JPA Entities & Repositories (MySQL / Aurora)
    │   │   │   │   ├── entity/ (JPA Mappings)
    │   │   │   │   └── repository/ (Spring Data JPA Repositories)
    │   │   │   ├── local/                      # Local Implementation Adapters (@Profile("local"))
    │   │   │   │   ├── LocalStorageService.java
    │   │   │   │   ├── JwtAuthenticationService.java
    │   │   │   │   ├── JpaAuditService.java
    │   │   │   │   ├── LocalApprovalWorkflowService.java
    │   │   │   │   ├── LoggingNotificationService.java
    │   │   │   │   ├── MockOcrService.java
    │   │   │   │   ├── LocalEventPublisher.java
    │   │   │   │   └── EnvironmentSecretProvider.java
    │   │   │   └── aws/                        # AWS Infrastructure Adapters (@Profile("aws")) [Phase 2]
    │   │   │       ├── S3StorageService.java
    │   │   │       ├── CognitoAuthenticationService.java
    │   │   │       ├── DynamoAuditService.java
    │   │   │       ├── StepFunctionsWorkflowService.java
    │   │   │       ├── SnsNotificationService.java
    │   │   │       ├── TextractOcrService.java
    │   │   │       ├── EventBridgePublisher.java
    │   │   │       └── SecretsManagerProvider.java
    │   │   │
    │   │   └── api/                            # API Controller Layer
    │   │       ├── controller/ (37 REST Controllers)
    │   │       ├── dto/ (Request & Response DTOs)
    │   │       ├── mapper/ (DTO <-> Domain / Entity Mappers)
    │   │       └── exception/ (GlobalExceptionHandler)
    │   │
    │   └── resources/
    │       ├── application.yml                 # Default Config
    │       ├── application-local.yml           # Local Profile Config (MySQL, Local Storage, JWT)
    │       ├── application-aws.yml             # AWS Profile Config (Aurora, S3, Cognito, DynamoDB)
    │       ├── db/migration/
    │       │   └── V1__init_schema.sql         # Flyway Schema Script
    │       └── data.sql                        # Sample Seed Data
    │
    └── test/                                   # Unit, Controller & Repository Tests
```

---

## 7. AWS Migration Mapping Table

| Component | Local Implementation (`@Profile("local")`) | AWS Implementation (`@Profile("aws")`) | Mô tả Chuyển đổi Phase 2 |
|---|---|---|---|
| **Storage** | `LocalStorageService` | `S3StorageService` | Lưu file local `uploads/` -> Upload trực tiếp Amazon S3 Presigned URL. |
| **Authentication** | `JwtAuthenticationService` | `CognitoAuthenticationService` | DB Users JWT Token -> AWS Cognito User Pool JWT Authentication. |
| **Audit Logging** | `JpaAuditService` | `DynamoAuditService` | Ghi log vào MySQL `audit_logs` -> Ghi log append-only Amazon DynamoDB. |
| **Approval Workflow** | `LocalApprovalWorkflowService` | `StepFunctionsWorkflowService` | Java State Machine -> AWS Step Functions Execution. |
| **Notification** | `LoggingNotificationService` | `SnsNotificationService` | Ghi SLF4J Log -> Gửi Email/SMS thông qua Amazon SNS Topic. |
| **OCR Service** | `MockOcrService` | `TextractOcrService` | Trích xuất Mock Text -> Tự động trích xuất chữ qua AWS Textract. |
| **Domain Events** | `LocalEventPublisher` | `EventBridgePublisher` | Xử lý nội bộ JVM -> Đẩy Event Bus Amazon EventBridge. |
| **Secrets Manager** | `EnvironmentSecretProvider` | `SecretsManagerProvider` | Đọc `application.yml` -> Lấy credentials AWS Secrets Manager. |
| **Metadata DB** | MySQL Local / Docker | Aurora Serverless v2 (MySQL Engine) | **Giữ nguyên 100% Flyway Migration, JPA Entities và Repositories**. |

---

## 8. Lộ trình Triển khai theo Tuần/Module (Module Roadmap)

### Phase 1: Triển khai Backend Local Hoàn chỉnh (100% APIs & Business Rules)

- [ ] **Bước 1: Khởi tạo Project & Infrastructure Base**
  - Tạo project Maven trong `backend-java`.
  - Khai báo 8 Core Ports trong `application/ports/`.
  - Triển khai Local Adapters (`LocalStorageService`, `JwtAuthenticationService`...) trong `infrastructure/local/`.
  - Cấu hình Spring Security, JWT, `application-local.yml`, Flyway `V1__init_schema.sql`, và `data.sql`.
- [ ] **Bước 2: Module Auth & Users** (`/auth/login`, `/auth/logout`, `/auth/me`)
- [ ] **Bước 3: Module Folders** (`GET /folders`, `GET /folders/{id}`, `POST /folders`, `DELETE /folders/{id}`)
- [ ] **Bước 4: Module Upload** (`POST /upload/url`, `POST /upload/confirm`)
- [ ] **Bước 5: Module Documents** (`GET /documents`, `GET /documents/{id}`, `POST /documents`, `DELETE /documents/{id}`, `PATCH /documents/{id}`)
- [ ] **Bước 6: Module Document Versions** (`GET /documents/{id}/versions`, `POST /documents/{id}/versions`, `POST /documents/{id}/versions/rollback`)
- [ ] **Bước 7: Module Tags** (`GET /documents/{id}/tags`, `POST /documents/{id}/tags`, `DELETE /documents/{id}/tags/{docTagId}`, `GET /tags`)
- [ ] **Bước 8: Module Search** (`GET /search`)
- [ ] **Bước 9: Module Permissions** (`GET /documents/{id}/permissions`, `POST /documents/{id}/permissions`, `PUT /documents/{id}/permissions/{permissionId}`, `DELETE /documents/{id}/permissions/{permissionId}`, `GET /documents/{id}/permissions/role`)
- [ ] **Bước 10: Module Approval Workflow** (`POST /approval/submit`, `/approve`, `/reject`, `GET /approval/history`)
- [ ] **Bước 11: Module Share** (`POST /share`, `GET /share`, `GET /shares`)
- [ ] **Bước 12: Module Dashboard & OCR Mock** (`GET /dashboard/stats`, `GET /ocr`, `POST /ocr`)
- [ ] **Bước 13: Audit Log & Automated Tests**
  - Đảm bảo 100% Unit Tests, Controller Tests (MockMvc), Repository Tests (`@DataJpaTest`) pass.

### Phase 2: Chuyển đổi sang Hạ tầng AWS (Zero Code Change in Business Logic)

- [ ] Kích hoạt Profile AWS `spring.profiles.active=aws`.
- [ ] Triển khai các AWS Adapters trong `infrastructure/aws/` (`S3StorageService`, `DynamoAuditService`, `StepFunctionsWorkflowService`...).
- [ ] Kiểm thử E2E trên hạ tầng AWS Cloud.

---

## 9. Lý do Chuyên môn: Tại sao Kiến trúc này Giúp Migrate sang AWS với 0 Thay đổi Business Logic?

1. **Tuân thủ Tuyệt đối Nguyên lý Dependency Inversion (DIP)**:
   - Tầng nghiệp vụ (`DocumentServiceImpl`, `ApprovalServiceImpl`...) chỉ phụ thuộc vào các **Ports (Interfaces)**. Không hề import hay biết tới các SDK cụ thể như AWS S3 SDK hay DynamoDB SDK.
2. **Chuyển đổi Linh hoạt bằng Dynamic Spring `@Profile`**:
   - Chuyển đổi giữa `local` và `aws` chỉ bằng 1 biến môi trường `SPRING_PROFILES_ACTIVE`. Spring IoC Container tự động inject đúng implementation phù hợp mà không cần sửa hay build lại code nghiệp vụ.
3. **Cơ chế Event-Driven Domain Events**:
   - Khi có sự kiện xảy ra (ví dụ tài liệu được phê duyệt), Business Service chỉ gọi `eventPublisher.publish(event)`. Việc xử lý sự kiện bằng log local hay gửi lên Amazon EventBridge được cách ly hoàn toàn ở tầng Infrastructure.
4. **Tính nhất quán giữa Local MySQL và AWS Aurora Serverless v2**:
   - Vì Aurora Serverless v2 dùng tương thích 100% MySQL Engine, toàn bộ DDL Flyway Migration (`V1__init_schema.sql`), JPA Mappings, Entity Classes và Repositories được tái sử dụng 100% trên AWS Cloud.
