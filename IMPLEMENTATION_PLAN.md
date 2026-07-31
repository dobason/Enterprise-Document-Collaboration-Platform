# Kế Hoạch Triển Khai & Hướng Dẫn Phát Triển (EDMS Backend)

Tài liệu này đóng vai trò là kim chỉ nam cho các thành viên trong team, mô tả chi tiết kiến trúc hệ thống, những gì đã được triển khai, và hướng dẫn cách thiết lập môi trường để tiếp tục phát triển dự án Enterprise Document Management System (EDMS).

---

## 1. Tổng Quan Dự Án & Công Nghệ

**EDMS Backend** được xây dựng dựa trên nguyên tắc **Kiến Trúc Lục Giác (Hexagonal Architecture / Ports and Adapters)**, giúp tách biệt hoàn toàn Logic Nghiệp Vụ (Domain/Application) khỏi các yếu tố bên ngoài (Database, API, AWS Services).

**Công nghệ cốt lõi:**
*   **Ngôn ngữ & Framework:** Java 17, Spring Boot 3.2.5
*   **Database:** H2 (cho môi trường test/local nhanh) và MySQL 8.x (cho môi trường local/staging/production).
*   **Migration:** Flyway (quản lý version DB tự động).
*   **Security:** Spring Security + JWT (JSON Web Token), Stateless authentication.
*   **Document API:** Swagger UI (OpenAPI 3.0).

---

## 2. Cấu Trúc Dự Án (Hexagonal Architecture)

Dự án nằm trong thư mục `backend-java`, được tổ chức thành 4 tầng chính:

```text
backend-java/src/main/java/com/edms/
├── domain/               # Tầng trong cùng: Chứa các cấu trúc dữ liệu cốt lõi
│   ├── enums/            # Các enum nghiệp vụ (UserRole, DocumentStatus, v.v.)
│   └── events/           # Domain events (nếu có sử dụng Event-driven)
│
├── application/          # Tầng Logic Nghiệp Vụ (Business Logic)
│   ├── ports/            # KHIẾP ĐIỂM CỦA HEXAGONAL: Các Interface (StorageService, OcrService...)
│   └── service/          # Các Service thực thi logic, chỉ gọi qua Port (DocumentService, AuthService...)
│
├── infrastructure/       # Tầng Giao Tiếp Bên Ngoài (Adapters cho Ports)
│   ├── adapters/
│   │   ├── local/        # Hiện thực Ports cho môi trường Local (Lưu file xuống đĩa, Mock OCR...)
│   │   └── aws/          # (Sẽ làm) Hiện thực Ports cho AWS (S3, Textract, Cognito...)
│   ├── config/           # Cấu hình Spring (Swagger, DataSeeder...)
│   ├── persistence/      # Giao tiếp Database (JPA Entities, Repositories)
│   └── security/         # Cấu hình Spring Security, JWT Filter, Token Provider
│
└── api/                  # Tầng Giao Tiếp Người Dùng (Driving Adapters)
    ├── controller/       # Các REST APIs (37 endpoints)
    ├── dto/              # Các đối tượng Request/Response
    └── exception/        # Xử lý lỗi tập trung (GlobalExceptionHandler)
```

---

## 3. Kế Hoạch Triển Khai (Implementation Plan)

### Pha 1: Local Development (✅ ĐÃ HOÀN THÀNH)
*   **API & DTO:** Hoàn thành 100% (37 RESTful APIs) theo đúng `API-CONTRACT.md`.
*   **Business Logic:** Đã triển khai đầy đủ logic tạo/sửa/xóa tài liệu, quản lý version, phân quyền (RBAC), quy trình duyệt (Workflow), chia sẻ link, và search.
*   **Database & Migration:** Đã có `V1__init_schema.sql` (Flyway) tạo 12 bảng và các ràng buộc.
*   **Adapters:** Đã triển khai xong `local` profile (Local Storage, Mock OCR, Local Auth).
*   **Test:** Đạt 100% Test Pass (`mvn test`). Tích hợp `DataSeeder` tự động sinh dữ liệu mẫu để test.

### Pha 2: AWS Integration (🚧 BƯỚC TIẾP THEO CHO TEAM)
Nhiệm vụ của team trong giai đoạn tới là hiện thực các AWS Adapters (thay thế cho Local Adapters):
1.  **`AwsS3StorageService`**: Implement `StorageService` để upload/download file lên Amazon S3, tạo Presigned URL thực.
2.  **`CognitoAuthenticationService`**: Implement xác thực qua Amazon Cognito thay vì Local JWT.
3.  **`AwsTextractOcrService`**: Gọi Amazon Textract để OCR tài liệu thực.
4.  **`AwsRdsConfiguration`**: Đảm bảo kết nối ổn định tới AWS RDS (Aurora MySQL).

*Lưu ý: Nhờ kiến trúc Hexagonal, team chỉ cần viết các class mới implement các interface trong `application/ports` và gán `@Profile("aws")`. KHÔNG CẦN sửa bất kỳ code nào ở tầng `application/service` hay `api/controller`!*

---

## 4. Hướng Dẫn Setup & Chạy Dự Án

### Yêu Cầu Hệ Thống
*   Java 17 (JDK 17)
*   Maven 3.8+ (hoặc dùng Maven Wrapper)
*   MySQL 8 (Nếu muốn chạy profile MySQL)

### Lựa Chọn 1: Chạy Siêu Tốc với H2 Database (Khuyên Dùng Để Test Nhanh)
Database H2 chạy hoàn toàn trong RAM. Khi tắt app, dữ liệu sẽ mất. Khi bật lại, `DataSeeder` sẽ tự động đổ lại dữ liệu mẫu.

1.  Mở terminal tại thư mục `backend-java`.
2.  Chạy lệnh:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    ```
    *(Nếu PowerShell không nhận `mvn`, hãy dùng file wrapper: `.\mvnw spring-boot:run ...`)*

### Lựa Chọn 2: Chạy với MySQL Local (Dùng để phát triển thực tế)
1.  Mở MySQL, tạo database `edms` bằng cách chạy script đã chuẩn bị sẵn:
    ```sql
    SOURCE src/main/resources/db/mysql-setup.sql;
    ```
    *(Hoặc đơn giản là chạy câu lệnh: `CREATE DATABASE edms;`)*
2.  Mở file `src/main/resources/application-mysql.yml`.
3.  Tìm dòng `password: root` và đổi thành mật khẩu MySQL thực tế trên máy bạn.
4.  Khởi chạy app với profile MySQL:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=mysql
    ```
    *(Dữ liệu mẫu cũng sẽ được `DataSeeder` đổ vào MySQL trong lần chạy đầu tiên).*

---

## 5. Hướng Dẫn Test API (Swagger UI)

Khi ứng dụng đã chạy thành công (ở port 8080):
1.  Truy cập: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2.  Vào API **POST `/auth/login`**, bấm **Try it out**.
3.  Chọn 1 trong các tài khoản mẫu từ dropdown (ví dụ Owner Account: `owner@edms.vn` / `Password123!`), bấm **Execute**.
4.  Copy chuỗi token nhận được.
5.  Cuộn lên trên cùng trang Swagger, bấm nút **Authorize 🔓**, dán token vào và bấm **Authorize**.
6.  Giờ bạn có thể test bất kỳ API nào (Tạo tài liệu, Duyệt tài liệu, v.v.). Dữ liệu mẫu đã có sẵn (Tài liệu ID: `d1`, `d2`, Thư mục: `f1`, `f2`).

---

## 6. Quy Ước Code Dành Cho Team
*   **Không vượt rào kiến trúc:** Controller (`api`) KHÔNG ĐƯỢC gọi trực tiếp Repository (`infrastructure/persistence`). Bắt buộc phải thông qua Service (`application/service`).
*   **Data Transfer Objects (DTO):** Mọi request/response ở Controller phải dùng DTO. Tuyệt đối không trả thẳng Entity của JPA ra API.
*   **Migration DB:** Nếu cần sửa bảng/thêm cột, KHÔNG sửa trực tiếp Entity rồi dùng Hibernate update. BẮT BUỘC tạo file Flyway migration mới (ví dụ: `V2__add_new_column.sql`) trong thư mục `src/main/resources/db/migration/`.
*   **Bảo mật:** Mọi API mới (ngoại trừ login/public) đều phải có header JWT. Đảm bảo cấu hình trong `SecurityConfig` phù hợp.
