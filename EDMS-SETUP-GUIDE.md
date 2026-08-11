# HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN EDMS (SETUP GUIDE)
**Dự án:** Enterprise Document Collaboration Platform
**Cập nhật:** Tháng 8/2026
**Kiến trúc:** AWS Serverless (Cognito, S3, API Gateway, Lambda, Aurora)

Tài liệu này giúp các thành viên mới hoặc anh em trong team (Quân, Hương) đồng bộ môi trường làm việc local một cách trơn tru nhất. Đọc kỹ và làm theo từng bước để tránh lỗi lặt vặt nhé!

---

## 1. YÊU CẦU HỆ THỐNG (Prerequisites)
Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau. Mở Terminal/CMD lên và gõ lệnh kiểm tra version để chắc chắn:

*   **Node.js (v18 trở lên):** Bắt buộc cho Frontend React. (`node -v`)
*   **Java 21 (JDK):** Bắt buộc cho Backend AWS Lambda. Khuyên dùng bản *Amazon Corretto 21*. (`java -version`)
*   **Apache Maven:** Công cụ build cho Java. (`mvn -version`)
*   **AWS CLI & AWS SAM CLI:** Để giao tiếp với AWS và build/deploy hạ tầng Serverless. (`sam --version`)
*   **Docker (Tùy chọn):** Rất cần thiết nếu bạn muốn giả lập chạy AWS Lambda ở dưới máy local (lệnh `sam local start-api`).

---

## 2. ĐỒNG BỘ SOURCE CODE
Luôn đảm bảo bạn đang ở nhánh `main` và có code mới nhất trước khi làm việc:

```bash
git checkout main
git pull origin main
```

---

## 3. CẤU HÌNH FRONTEND (REACT)
Frontend nằm trong thư mục `frontend/`.

**Bước 3.1: Cài đặt thư viện**
```bash
cd frontend
npm install
```

**Bước 3.2: Cấu hình Biến môi trường (.env)**
Tạo một file có tên là `.env` nằm ngay trong thư mục `frontend/` (ngang hàng với `package.json`) và dán nội dung sau vào:

```env
# AWS GLOBAL CONFIG
REACT_APP_AWS_REGION=ap-southeast-1

# AMAZON S3 (Lưu trữ tài liệu)
REACT_APP_S3_BUCKET_NAME=edms-docs-865189667297-ap-southeast-1

# AMAZON COGNITO (Xác thực người dùng)
REACT_APP_COGNITO_CLIENT_ID=4r1jcbcbhbtcocsqf4lm7oka2l
# Lưu ý: Lấy thêm thông số REACT_APP_COGNITO_USER_POOL_ID trong group Zalo để điền vào đây.
```
*(Lưu ý: Nếu dùng Vite thì đổi tiền tố `REACT_APP_` thành `VITE_`)*

**Bước 3.3: Chạy ứng dụng**
```bash
npm start
```
Ứng dụng sẽ chạy ở `http://localhost:3000`.

---

## 4. CẤU HÌNH BACKEND (AWS SERVERLESS & JAVA)
Toàn bộ cấu hình hạ tầng nằm ở file `template.yaml` (thư mục gốc), còn source code logic Java nằm trong thư mục `backend/`.

**Bước 4.1: Build code Backend**
Đứng ở **thư mục gốc** của dự án (nơi có file `template.yaml`), chạy lệnh:
```bash
sam build
```
Lệnh này sẽ dùng Maven để compile code Java 21 và đóng gói chuẩn bị cho Lambda.

**Bước 4.2: Lưu ý về Cơ sở dữ liệu (Database)**
*   Dự án sử dụng **Amazon Aurora** (Hệ quản trị CSDL quan hệ).
*   *Lưu ý:* Chúng ta ĐÃ BỎ DynamoDB. Toàn bộ code logic liên quan đến DB phải sử dụng SQL/JDBC để kết nối vào Aurora. VPC Config cho Lambda sẽ được cập nhật sau khi khởi tạo cluster Aurora.

---

## 5. QUY TRÌNH LÀM VIỆC NHÓM (GIT WORKFLOW)
Để tránh "dẫm chân" lên nhau (Conflict) và hỏng hạ tầng AWS, team thống nhất:
1. **Không code thẳng lên nhánh `main`.**
2. Nhận task -> Tạo nhánh mới: `git checkout -b feature/ten-tinh-nang`
3. Code xong -> Push nhánh lên GitHub -> Tạo **Pull Request (PR)**.
4. Báo cho team (Hưng/Quân) vào Review.
5. GitHub Actions (CI/CD) sẽ tự động chạy Test. Nếu báo tick xanh (Passed) thì người review mới được bấm Merge vào `main`.

*Chúc anh em code mượt, ít bug, qua môn xuất sắc! Cần support hạ tầng thì hú Hưng nhé!*