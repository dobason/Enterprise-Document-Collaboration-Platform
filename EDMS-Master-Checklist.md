# EDMS — Checklist & Kế hoạch thực thi (Master Checklist)

> File này là **nguồn sự thật duy nhất cho mọi thứ liên quan tiến độ**: quyết định phạm vi, phân vai trò, học nền tảng, task theo tuần, rủi ro. `README.md` (trong zip code) chỉ còn giữ phần giới thiệu đề tài/ERD/setup kỹ thuật/công nghệ — không lặp lại nội dung ở đây.
>
> Copy file này vào Trello/Notion/GitHub Projects, mỗi người tick vào phần của mình. Tick xong 1 mục thì đổi `[ ]` thành `[x]`.

---

## 🔴 VIỆC ƯU TIÊN SỐ 1 — làm trước tất cả mọi thứ khác (P4, ~30 phút)

Code Java trong zip được viết cẩn thận và đã kiểm tra cú pháp/cấu trúc kỹ, nhưng **chưa được biên dịch thật bằng Maven** (môi trường soạn thảo không có sẵn Maven + AWS SDK để build thử). Trước khi cả team dựa vào code này, **bắt buộc** làm bước sau đầu tiên:

- [ ] Cài JDK 17 + Maven trên máy
- [ ] Chạy lệnh sau, xem có function nào lỗi compile không:
  ```bash
  for dir in java/functions/*/; do
    echo "=== $dir ==="
    (cd "$dir" && mvn -q clean package) || echo "!!! LỖI tại $dir !!!"
  done
  ```
- [ ] Nếu có lỗi (thường là version dependency không tồn tại hoặc import sai) — sửa trực tiếp trong `pom.xml`/`App.java` tương ứng, đây là lỗi cú pháp nhỏ, không phải lỗi kiến trúc
- [ ] Sau khi cả 9 module build sạch (thấy `BUILD SUCCESS` cho từng cái) → mới bắt đầu Mục 1 bên dưới

---

## 1. Quyết định phạm vi (Scope Decision)

> Ghi thẳng vào báo cáo — đây cũng là điểm "đóng góp cá nhân" (thể hiện trade-off có chủ đích, không phải làm ẩu).

Dựa trên tài liệu nghiệp vụ `Business.docx`, hệ thống EDMS được thiết kế hoàn toàn trên kiến trúc **Serverless với DynamoDB làm cơ sở dữ liệu chính** (gồm 8 thực thể nghiệp vụ và các GSI tương ứng). Nhóm thống nhất phạm vi triển khai cho 14 chức năng cốt lõi như sau:

| Nhóm chức năng                        | Chi tiết nghiệp vụ theo Business.docx                                                                       | Quyết định triển khai                                                                                               |
| :--------------------------------------- | :------------------------------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------- |
| **1. Đăng nhập & Phân quyền** | Xác thực qua Cognito, phân quyền Owner, Editor, Viewer trong bảng`Permissions`                          | **Làm đầy đủ**: Tích hợp Cognito User Pool + Groups, kiểm tra quyền truy cập ở API Gateway & Lambda    |
| **2. Kiểm soát Tải lên**       | Upload file qua Presigned URL, giới hạn dung lượng, Expiration 5-10 phút                                  | **Làm đầy đủ**: Sinh Presigned URL từ Lambda, upload trực tiếp lên S3, cập nhật trạng thái `Files` |
| **3. Lưu trữ Metadata**          | Lưu thông tin tài liệu, tệp tin, phiên bản vào DynamoDB                                                | **Làm đầy đủ**: Thiết kế DynamoDB tables/GSI theo đúng thực thể trong `Business.docx`                |
| **4. Soạn thảo Rich-text**       | Trình soạn thảo trực tuyến, định dạng Heading, Bold, Italic, Alignment, Highlight, Code Snippet, Image | **Làm đầy đủ**: Tích hợp Rich-text Editor ở FE, lưu nội dung dạng JSON vào bảng `DocumentVersions` |
| **5. Quản lý Phiên bản**       | Tự động tạo version mới khi lưu, hiển thị bản mới nhất, quá khứ ở chế độ Read-only            | **Làm đầy đủ**: Quản lý lịch sử phiên bản trong `DocumentVersions`, đánh dấu `isCurrent`        |
| **6. Khôi phục (Rollback)**      | Xem lịch sử và khôi phục phiên bản cũ làm bản hiện hành                                            | **Làm đầy đủ**: Cập nhật `isCurrent` và `currentVersion` của tài liệu khi rollback                 |
| **7. Định dạng Xuất bản**     | Kết xuất và tải xuống tài liệu dưới dạng`.docx`, `.pdf`, `.md`, `.note`                      | **Làm đầy đủ**: Xử lý export định dạng ở Lambda/Frontend                                               |
| **8. Hệ thống Gắn thẻ**        | Gắn nhiều nhãn (Tags) cho tài liệu để phân loại                                                       | **Làm đầy đủ**: Lưu thông tin nhãn trong bảng `Tags`                                                   |
| **9. Tìm kiếm tài liệu**       | Tìm kiếm theo tên file và theo nhãn (Tags)                                                                | **Làm đầy đủ**: Query qua DynamoDB GSI (`GSI_DocumentTags` và tìm kiếm theo tên)                       |
| **10. Vòng đời Tài liệu**     | Soft Delete (vào thùng rác 30 ngày) và Hard Delete (xóa vĩnh viễn tự động qua TTL)                  | **Làm đầy đủ**: Dùng DynamoDB TTL trên thuộc tính `ttl` của bảng `Documents`                       |
| **11. Trích xuất chữ (OCR)**    | Tự động nhận diện text từ ảnh/PDF bằng Amazon Textract                                                 | **Làm đầy đủ**: Kích hoạt bất đồng bộ qua S3 Event -> Lambda -> Textract -> lưu `OCRResults`        |
| **12. Nhật ký truy vết**        | Ghi log hành động Download, Export, Update Permission, Delete kèm IP, timestamp, details                   | **Làm đầy đủ**: Ghi log vào bảng `AuditLogs` với cấu trúc chi tiết dạng Map JSON                    |
| **13. Quy trình Phê duyệt**     | Luồng duyệt tài liệu (Draft -> Pending -> Approved/Rejected) qua Step Functions                            | **Làm đầy đủ**: Tích hợp Step Functions Standard Workflow để lưu vết lịch sử duyệt                  |
| **14. Bảo mật & CI/CD**          | Tường lửa AWS WAF chặn Bot/Spam ở Edge; GitHub Actions deploy qua OIDC                                    | **Làm đầy đủ**: Cấu hình WAF WebACL, thiết lập OIDC Federation cho GitHub Actions, SAM IaC               |

- [ ] Cả team đọc và thống nhất bảng phạm vi trên trước khi bắt đầu code.

---

## 1.5. Thiết kế Cơ sở dữ liệu DynamoDB (Nguồn sự thật từ Business.docx)

Hệ thống sử dụng DynamoDB làm cơ sở dữ liệu chính với 8 thực thể (bảng) nghiệp vụ:

1. **Users**: Quản lý thông tin hồ sơ người dùng (Cognito quản lý xác thực).
2. **Documents**: Thực thể trung tâm quản lý tài liệu, trạng thái (ACTIVE/TRASH), và thuộc tính `ttl` để tự động xóa sau 30 ngày.
3. **DocumentVersions**: Lưu lịch sử soạn thảo trực tuyến dưới dạng JSON, đánh dấu `isCurrent`.
4. **Permissions**: Kiểm soát truy cập chi tiết (OWNER, EDITOR, VIEWER).
5. **Tags**: Nhãn phân loại tài liệu.
6. **Files**: Metadata tệp vật lý trên S3, trạng thái upload (`uploadStatus`: PENDING/COMPLETED/FAILED).
7. **OCRResults**: Kết quả trích xuất văn bản từ hình ảnh/PDF qua Amazon Textract.
8. **AuditLogs**: Nhật ký kiểm toán lưu vết hành động nhạy cảm (DOWNLOAD, EXPORT, UPDATE_PERMISSION, DELETE) kèm IP và details JSON.

Các GSI cần cấu hình trên DynamoDB:

- `GSI_OwnerDocuments` (Partition Key: `ownerId`)
- `GSI_UserPermissions` (Partition Key: `userId`)
- `GSI_DocumentVersions` (Partition Key: `documentId`)
- `GSI_DocumentFiles` (Partition Key: `documentId`)
- `GSI_DocumentTags` (Partition Key: `documentId`)
- `GSI_AuditByDocument` (Partition Key: `documentId`)
- `GSI_AuditByUser` (Partition Key: `userId`)

---

## 2. Chia vai trò 5 người

Chia theo **chiều dọc kiến trúc** để mỗi người sở hữu trọn một luồng nghiệp vụ, tránh conflict git liên tục.

| Vai trò                                          | Người phụ trách | Sở hữu                                                                                                                                                | Sản phẩm bàn giao                                                                                       |
| :------------------------------------------------ | :------------------ | :------------------------------------------------------------------------------------------------------------------------------------------------------ | :--------------------------------------------------------------------------------------------------------- |
| **P1 — Auth, Frontend & Rich-text Lead**   | Tuấn               | Cognito User Pool/Groups, React SPA (Login, Rich-text Editor, Version History, Rollback, Export UI, Tags UI, Search UI, WAF integration)                | FE chạy mượt mà, đầy đủ tính năng soạn thảo, lịch sử phiên bản và xuất file              |
| **P2 — Storage & Core API Lead**           | Hương và Quân   | S3 bucket, DynamoDB Tables & GSIs, Lambda:`upload_init`, `document_crud`, `list_documents`, `search`, `version_mgmt`                          | API CRUD tài liệu, quản lý phiên bản, rollback, xuất file, và gắn thẻ hoạt động tốt          |
| **P3 — Workflow, OCR & Notification Lead** | Hưng               | Step Functions approval state machine, Amazon Textract integration, SNS topic, Lambda:`ocr_processor`, `share_link`, `notify`, `approval_tasks` | Luồng duyệt tài liệu, trích xuất OCR tự động từ S3 event, và email thông báo chạy end-to-end |
| **P4 — DevOps, Security & Infra Lead**     | Sơn                | `template.yaml` (SAM), GitHub Actions + OIDC, IAM roles, AWS WAF WebACL, CloudWatch Logs/Alarms/Dashboard                                             | Pipeline CI/CD tự động deploy, bảo mật WAF ở Edge, giám sát tập trung                             |
| **P5 — QA, Audit & Docs Lead**             | Hưng               | Unit/integration tests, DynamoDB`AuditLogs` integration, báo cáo song ngữ, video demo, clean-up tài nguyên                                       | Bộ báo cáo nộp hoàn chỉnh, log audit ghi nhận đầy đủ, test suite pass 100%                      |

**Nguyên tắc phối hợp bắt buộc:**

- [ ] Dùng 1 tài khoản AWS chung, IAM user riêng từng người (KHÔNG share 1 access key).
- [ ] Dùng 1 repo GitHub chung, mỗi người 1 nhánh riêng (`feature/auth`, `feature/editor`...), PR vào `develop`, P4 merge vào `main` để trigger CI/CD.
- [ ] Sử dụng board task chung (Trello/Notion/GitHub Projects) để track tiến độ.

---

## 3. Kiến thức nền tảng nhanh (đọc trước Weekend 1, ~10 phút)

- **Serverless & DynamoDB**: Không quản lý máy chủ. DynamoDB là NoSQL cơ sở dữ liệu cực kỳ mạnh mẽ, hỗ trợ cơ chế TTL tự động xóa dữ liệu (dùng cho Hard Delete sau 30 ngày trong thùng rác) và GSI để truy vấn nhanh theo các chiều thông tin khác nhau.
- **Amazon Textract**: Dịch vụ AI trích xuất văn bản từ tài liệu quét hoặc hình ảnh tự động mà không cần dựng model.
- **AWS WAF (Web Application Firewall)**: Bảo vệ ứng dụng web khỏi các cuộc tấn công phổ biến và bot/spam bằng cách lọc lưu lượng truy cập ở tầng Edge (CloudFront).
- **OIDC Federation**: Cơ chế xác thực an toàn giữa GitHub Actions và AWS, giúp deploy code mà không cần lưu trữ AWS Access Key tĩnh trong repo.

---

## 4. Async pre-work trong tuần (mỗi người tự làm trước Weekend 1, ~30-45 phút/buổi tối)

**P1 (Auth & Frontend):**

- [ ] Tìm hiểu thư viện Rich-text Editor cho React (ví dụ: Quill, Draft.js hoặc Editor.js) hỗ trợ định dạng Heading, Code Snippet và chèn ảnh.
- [ ] Đọc tài liệu Cognito Auth SDK để tích hợp đăng nhập và lấy JWT token.

**P2 (Storage & Core API):**

- [ ] Thiết kế cấu trúc JSON lưu trữ nội dung Rich-text trong DynamoDB.
- [ ] Đọc tài liệu DynamoDB GSI và viết các câu lệnh truy vấn mẫu bằng AWS SDK Java.

**P3 (Workflow, OCR & Notification):**

- [ ] Đọc tài liệu Amazon Textract Java SDK (phương thức `detectDocumentText` hoặc `analyzeDocument`).
- [ ] Thiết lập thử nghiệm Step Functions với trạng thái chờ duyệt (`waitForTaskToken`).

**P4 (DevOps/Infra):**

- [ ] Tìm hiểu cách cấu hình AWS WAF WebACL trong SAM template.
- [ ] Thiết lập IAM OIDC Identity Provider trên AWS Console để chuẩn bị cho GitHub Actions.

**P5 (QA & Docs):**

- [ ] Thiết kế cấu trúc bảng `AuditLogs` và các API log tương ứng.
- [ ] Chuẩn bị khung báo cáo theo các yêu cầu nghiệp vụ của `Business.docx`.

---

## 5. GIAI ĐOẠN 0 — Trước Weekend 1 (làm trong tuần, async)

- [ ] Cả team đọc qua `README.md` và tài liệu nghiệp vụ `Business.docx`.
- [ ] Thống nhất vai trò và tạo board task chung.
- [ ] Khởi tạo repo Git và cấu hình phân nhánh.

---

## 6. GIAI ĐOẠN 1 — Setup hạ tầng & Khởi tạo DB (P4 chủ trì, ~1.5-2 giờ)

- [ ] (README mục A) Tạo IAM user riêng, MFA, Billing Alarm.
- [ ] (README mục B) Cài đặt JDK 17, Maven, SAM CLI, AWS CLI.
- [ ] (README mục C) Tạo các bảng DynamoDB (Users, Documents, DocumentVersions, Permissions, Tags, Files, OCRResults, AuditLogs) và các GSI tương ứng trong `template.yaml`.
- [ ] (README mục D) Deploy stack ban đầu lên AWS để kiểm tra tài nguyên.
- [ ] (README mục E) Tạo Cognito User Pool & App Client, cấu hình Groups (SALES, HR, IT...).
- [ ] (README mục F) Cấu hình SNS Topic `edms-doc-events` và đăng ký email nhận thông báo.
- [ ] (README mục G) Thiết lập OIDC Role cho GitHub Actions.

---

## 7. GIAI ĐOẠN 2 — Weekend 1: Xác thực, Upload & Soạn thảo cơ bản

**Mục tiêu cuối Chủ nhật:** Đăng nhập thành công, upload file qua Presigned URL an toàn, và hiển thị giao diện soạn thảo Rich-text cơ bản.

**P1 — Auth & Frontend:**

- [ ] Hoàn thiện trang Đăng nhập kết nối Cognito User Pool.
- [ ] Tích hợp Rich-text Editor vào workspace, hỗ trợ định dạng Heading, Bold, Italic, Alignment, Highlight.
- [ ] Thiết kế form upload file, gọi API lấy Presigned URL và upload trực tiếp lên S3.

**P2 — Storage & Core API:**

- [ ] Hoàn thiện Lambda `upload_init` sinh Presigned URL với thời gian sống 5-10 phút và giới hạn dung lượng.
- [ ] Viết Lambda `document_crud` xử lý lưu metadata tài liệu gốc vào bảng `Documents` khi S3 trigger event `ObjectCreated`.

**P3 — Workflow & Notification:**

- [ ] Tạo Step Functions State Machine cho luồng duyệt tài liệu (Draft -> Pending -> Approved/Rejected).
- [ ] Cấu hình SNS Topic gửi email thông báo khi có yêu cầu duyệt mới.

**P4 — DevOps/Infra:**

- [ ] Cấu hình AWS WAF WebACL cơ bản gắn với API Gateway để chặn spam request.
- [ ] Thiết lập pipeline CI/CD GitHub Actions chạy test tự động khi push code.

**P5 — QA & Docs:**

- [ ] Viết unit test cho Lambda `upload_init` và `document_crud`.
- [ ] Chụp ảnh minh chứng: Cognito User Pool, S3 Bucket, DynamoDB Tables, WAF WebACL.

✅ **DoD Weekend 1:** Đăng nhập được -> Vào workspace soạn thảo -> Upload file lên S3 thành công qua Presigned URL -> Metadata lưu vào DynamoDB.

---

## 8. GIAI ĐOẠN 3 — Weekend 2: Quản lý Phiên bản, Rollback & Xuất bản

**Mục tiêu:** Lưu trữ phiên bản tự động, khôi phục phiên bản cũ (Rollback), xuất file đa định dạng, và gắn thẻ phân loại.

**P1:**

- [ ] Giao diện hiển thị danh sách phiên bản tài liệu (gọi API lấy dữ liệu từ `DocumentVersions`).
- [ ] Nút "Rollback" khôi phục phiên bản cũ (past versions hiển thị ở chế độ Read-only).
- [ ] Nút "Export" cho phép tải xuống dưới dạng `.docx`, `.pdf`, `.md`, `.note`.
- [ ] Giao diện gắn thẻ (Tags) cho tài liệu.

**P2:**

- [ ] Viết API lưu phiên bản mới (tự động tăng `versionNumber`, lưu nội dung JSON vào `DocumentVersions`).
- [ ] Viết API Rollback: cập nhật `isCurrent` của phiên bản được chọn và `currentVersion` của tài liệu gốc.
- [ ] Viết API Export: chuyển đổi JSON nội dung sang các định dạng `.docx`, `.pdf`, `.md`, `.note`.
- [ ] Viết API gắn thẻ (lưu vào bảng `Tags`).

**P3:**

- [ ] Viết Lambda `ocr_processor` nhận S3 Event (khi upload hình ảnh/PDF) -> gọi Amazon Textract trích xuất text -> lưu vào bảng `OCRResults`.
- [ ] Cấu hình SNS gửi email thông báo khi tài liệu được chia sẻ.

**P4:**

- [ ] Cấu hình DynamoDB TTL trên thuộc tính `ttl` của bảng `Documents` để tự động xóa vĩnh viễn sau 30 ngày (kể từ khi `status` chuyển sang `TRASH`).
- [ ] Cấu hình CloudWatch Log Group và Alarm cho các Lambda mới.

**P5:**

- [ ] Thiết lập tích hợp ghi log vào bảng `AuditLogs` cho các hành động: DOWNLOAD, EXPORT, UPDATE_PERMISSION, DELETE.
- [ ] Viết unit test cho luồng Versioning, Rollback và OCR.

✅ **DoD Weekend 2:** Soạn thảo -> Lưu tự động tạo version mới -> Rollback version cũ thành công -> Export ra các định dạng -> OCR tự động chạy khi upload ảnh/PDF -> Log ghi nhận vào AuditLogs.

---

## 9. GIAI ĐOẠN 4 — Weekend 3: Phân quyền chi tiết, Tìm kiếm & Phê duyệt hoàn chỉnh

**Mục tiêu:** Phân quyền Owner/Editor/Viewer hoạt động, tìm kiếm theo tên/nhãn, và luồng duyệt Step Functions chạy end-to-end.

**P1:**

- [ ] Giao diện quản lý quyền truy cập (Owner cấp quyền Editor/Viewer cho user khác).
- [ ] Thanh tìm kiếm nâng cao hỗ trợ tìm theo tên file và theo nhãn (Tags).
- [ ] Giao diện phê duyệt dành cho Manager (Approve/Reject tài liệu).

**P2:**

- [ ] Viết API cấp quyền (lưu vào bảng `Permissions`).
- [ ] Viết API tìm kiếm: sử dụng `GSI_DocumentTags` để tìm theo nhãn và query theo tên file.
- [ ] Tích hợp kiểm tra quyền (Owner/Editor mới được sửa, Viewer chỉ được xem, past versions chỉ được xem).

**P3:**

- [ ] Hoàn thiện Step Functions: tích hợp Lambda `approval_tasks` để cập nhật trạng thái tài liệu (`status` sang PENDING/APPROVED/REJECTED) và gửi email SNS thông báo kết quả.
- [ ] Viết API sinh link chia sẻ tài liệu có thời hạn (Pre-signed GET URL).

**P4:**

- [ ] Nâng cấp AWS WAF WebACL với các rule nâng cao (SQLi, XSS, Rate Limiting).
- [ ] Tối ưu hóa phân quyền IAM Role cho các Lambda (least privilege).

**P5:**

- [ ] Viết integration test chạy giả lập toàn bộ luồng từ Upload -> Duyệt -> Chia sẻ -> Ghi log Audit.
- [ ] Chụp ảnh minh chứng: Step Functions Graph, Email SNS, WAF Blocked Requests.

✅ **DoD Weekend 3:** Phân quyền hoạt động chính xác -> Tìm kiếm ra tài liệu theo tên/tag -> Gửi duyệt và Manager duyệt thành công từ UI -> Link chia sẻ hết hạn đúng TTL.

---

## 10. GIAI ĐOẠN 5 — Weekend 4: Hoàn thiện CI/CD, Kiểm thử, Báo cáo & Clean-up

**Mục tiêu:** Đóng gói toàn bộ dự án, chạy test suite sạch sẽ, quay video demo và dọn dẹp tài nguyên.

**P1:**

- [ ] Polish UI/UX toàn bộ hệ thống, đảm bảo responsive và hiển thị mượt mà.
- [ ] Tích hợp hiển thị kết quả OCR trên giao diện để copy nhanh.

**P2 & P3:**

- [ ] Fix toàn bộ bug tồn đọng từ các tuần trước.
- [ ] Kiểm tra cơ chế TTL của DynamoDB hoạt động đúng cho các tài liệu trong thùng rác.

**P4:**

- [ ] Hoàn thiện pipeline CI/CD GitHub Actions sử dụng OIDC hoàn toàn (không dùng static access key).
- [ ] Tạo CloudWatch Dashboard tổng hợp giám sát hệ thống.

**P5:**

- [ ] Chạy toàn bộ test suite (`mvn test` và integration script), đảm bảo pass 100%.
- [ ] Tổng hợp báo cáo song ngữ hoàn chỉnh (gồm phần Phản tư của cả 5 thành viên).
- [ ] Quay video demo end-to-end (3-5 phút).
- [ ] **Chạy quy trình dọn dẹp tài nguyên (Clean-up)** để đưa chi phí về $0.
- [ ] Chụp ảnh Billing Console xác nhận chi phí ≈ $0.

✅ **DoD cuối cùng:**

- [ ] 14 chức năng chạy đúng nghiệp vụ `Business.docx`, có screenshot minh chứng.
- [ ] CI/CD chạy sạch qua OIDC.
- [ ] Log audit ghi nhận đầy đủ, CloudWatch Dashboard hoạt động.
- [ ] Đã clean-up toàn bộ tài nguyên, billing ≈ $0.
- [ ] Video demo và báo cáo song ngữ sẵn sàng nộp.

---

## 11. Rủi ro thường gặp & cách phòng tránh

| Rủi ro                                                 | Cách phòng tránh                                                                                                                                                         |
| :------------------------------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Quên bật TTL hoặc cấu hình sai thuộc tính`ttl` | Thuộc tính`ttl` trong DynamoDB phải lưu dưới dạng **Unix Timestamp (epoch seconds)**. Cần kiểm tra kỹ định dạng số trước khi lưu.                  |
| Amazon Textract bị quá hạn mức hoặc chi phí cao   | Chỉ kích hoạt OCR cho các tệp tin có định dạng hình ảnh (`.png`, `.jpg`, `.jpeg`) hoặc tài liệu `.pdf` rõ chữ. Giới hạn kích thước tệp quét. |
| Lộ thông tin nhạy cảm trong log                     | Không ghi nội dung tài liệu (`content` JSON) hoặc thông tin cá nhân chưa mã hóa vào CloudWatch Logs. Sử dụng correlation ID để truy vết.                 |
| Xóa nhầm dữ liệu trong DynamoDB                     | Sử dụng cơ chế Soft Delete (`status: TRASH`) trước. Chỉ để DynamoDB TTL tự động xóa vĩnh viễn sau 30 ngày.                                                |

---

## 12. Phụ lục: Bảng quy chiếu file → vai trò (tra cứu nhanh)

| File                                           | Vai trò | Việc cần làm theo Business.docx                                                    |
| :--------------------------------------------- | :------- | :------------------------------------------------------------------------------------ |
| `java/functions/upload_init/.../App.java`    | P2       | Sinh Presigned URL (5-10 phút), validate fileType/size                               |
| `java/functions/document_crud/.../App.java`  | P2       | Xử lý Soft Delete (`status: TRASH`, set `ttl` = now + 30 days), Hard Delete     |
| `java/functions/list_documents/.../App.java` | P2       | Lấy danh sách tài liệu theo quyền truy cập của user                            |
| `java/functions/version_mgmt/.../App.java`   | P2       | Lưu phiên bản mới vào`DocumentVersions`, xử lý Rollback và Export           |
| `java/functions/search/.../App.java`         | P2       | Tìm kiếm tài liệu theo tên file và nhãn (Tags)                                 |
| `java/functions/share_link/.../App.java`     | P3       | Sinh link chia sẻ (GET URL) có thời hạn, kiểm tra quyền                         |
| `java/functions/ocr_processor/.../App.java`  | P3       | Nhận S3 event, gọi Amazon Textract, lưu kết quả vào`OCRResults`               |
| `java/functions/approval_tasks/.../App.java` | P3       | Cập nhật trạng thái tài liệu trong quy trình duyệt Step Functions             |
| `java/functions/notify/.../App.java`         | P3       | Gửi email thông báo qua SNS khi có sự kiện duyệt hoặc chia sẻ                |
| `frontend/src/*`                             | P1       | Giao diện Rich-text Editor, lịch sử phiên bản, export, tags, search, permissions |
| `.github/workflows/deploy.yml`               | P4       | Pipeline CI/CD deploy tự động qua OIDC                                             |
| `java/functions/*/src/test/`                 | P5       | Viết unit test và integration test cho các luồng nghiệp vụ                      |

_Hết tài liệu / End of document._
