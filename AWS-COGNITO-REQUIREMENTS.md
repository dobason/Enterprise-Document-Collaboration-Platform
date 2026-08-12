# AWS Integration Requirements — Amazon Cognito

> Tài liệu này liệt kê **những gì cần có trên AWS** để code Cognito đã viết hoạt động. **Không tạo data mock** — mọi thông số phải là giá trị thật từ User Pool mới bạn tự tạo.

---

## 1. Thông số cần điền (bắt buộc)

| Biến | File | Lấy từ đâu |
|---|---|---|
| `COGNITO_USER_POOL_ID` | `.env` (gốc) + `frontend/.env` | Sau khi tạo User Pool. VD: `ap-southeast-1_AbCdEfGhI` |
| `COGNITO_CLIENT_ID` | `.env` + `frontend/.env` | Sau khi tạo App Client. (Client cũ `4r1jcbcbhbtcocsqf4lm7oka2l` đã **bỏ, không dùng nữa**) |
| `AWS_REGION` | `.env` | `ap-southeast-1` (đã có) |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | `.env` | IAM user của team (đã có — **nên rotate vì từng bị commit**) |

> ⚠️ Cả 2 giá trị Cognito hiện đang **rỗng** — app vẫn chạy được (lazy-load), nhưng login qua Cognito chưa hoạt động cho tới khi điền xong.

---

## 2. Tạo User Pool mới (từ đầu)

> Client cũ `4r1jcbcbhbtcocsqf4lm7oka2l` không dùng nữa — bạn tạo mới hoàn toàn theo các bước dưới.

**Cách 1 — AWS Console:**
1. Console → **Cognito** → **User Pools** → **Create user pool**
2. **Sign-in experience**: chọn **Email** (hoặc Username)
3. **Security requirements**: bỏ password policy mặc định nếu muốn đơn giản (không cần MFA cho dev)
4. **Cấu hình Attribute**: giữ mặc định (email required)
5. **Create app client**: chọn **Public client** (SPA), bỏ tick *Generate client secret*
   - **BẬT** `Enable username password-based auth (ALLOW_USER_PASSWORD_AUTH)` ⚠️
   - Nếu dùng Hosted UI: thêm callback URL `http://localhost:3000`
6. Tạo xong → vào **User pool → Users → Create user** để tạo tài khoản test
7. Tạo **Groups**: `ADMIN`, `HR`, `SALES`, `FINANCE`, `LEGAL`, `MARKETING`, `IT_SUPPORT`, thêm user test vào group `ADMIN`

**Cách 2 — AWS CLI** (thay `<values>`):
```bash
# 1. Tạo user pool
POOL_ID=$(aws cognito-idp create-user-pool --pool-name edms-users \
  --policies '{"PasswordPolicy":{"MinimumLength":8,"RequireUppercase":true,"RequireLowercase":true,"RequireNumbers":true,"RequireSymbols":true}}' \
  --query 'UserPool.Id' --output text)

# 2. Tạo app client (public, không secret, bật USER_PASSWORD_AUTH)
CLIENT_ID=$(aws cognito-idp create-user-pool-client --user-pool-id "$POOL_ID" \
  --client-name edms-web \
  --generate-secret \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH \
  --query 'UserPoolClient.ClientId' --output text)

# 3. Tạo groups
for g in ADMIN HR SALES FINANCE LEGAL MARKETING IT_SUPPORT; do
  aws cognito-idp create-group --user-pool-id "$POOL_ID" --group-name "$g"
done

# 4. Tạo user admin test
aws cognito-idp admin-create-user --user-pool-id "$POOL_ID" \
  --username admin@edms.vn --temporary-password "TmpPass123!" \
  --user-attributes Name=email,Value=admin@edms.vn

# 5. Đổi sang password vĩnh viễn + vào group ADMIN
aws cognito-idp admin-set-user-password --user-pool-id "$POOL_ID" \
  --username admin@edms.vn --password "Passw0rd!" --permanent
aws cognito-idp admin-add-user-to-group --user-pool-id "$POOL_ID" \
  --username admin@edms.vn --group-name ADMIN

echo "POOL_ID=$POOL_ID"
echo "CLIENT_ID=$CLIENT_ID"
```

> ⚠️ Nếu dùng `--generate-secret` → App Client có secret → frontend SPA sẽ không login được. Để **bỏ secret**, xóa tham số `--generate-secret`.

---

## 3. User Groups (tạo theo mục 2)

| Group | Vai trò trong code |
|---|---|
| `ADMIN` | → `role = ADMIN` (CognitoJwtValidator.mapRole) |
| `HR`, `SALES`, `FINANCE`, `LEGAL`, `MARKETING`, `IT_SUPPORT` | → `department` trong UserDto |

**Ràng buộc code hiện tại:**
- Chỉ group `ADMIN` → `role = ADMIN`; mọi group khác → `role = VIEWER` (CognitoJwtValidator.java:86-90).
- Nếu cần user được phân quyền `OWNER/EDITOR/MANAGER`, backend đang **không map** được từ groups — cần thêm mapping hoặc gán trong DB sau khi sync.

---

## 4. Luồng xác thực trong code

### Backend (profile `mysql` hoặc `aws`)
1. `POST /auth/login` → `CognitoAuthenticationService.login()` gọi Cognito `InitiateAuth` (`USER_PASSWORD_AUTH`) với email + password.
2. Cognito trả `idToken` → `CognitoJwtValidator` tải JWKS từ `https://cognito-idp.<region>.amazonaws.com/<poolId>/.well-known/jwks.json`, verify chữ ký RS256 + `aud` = clientId + `token_use = id`.
3. Sync user vào DB local (tạo mới theo `cognitoSub` nếu chưa có — **đây là dữ liệu thật từ Cognito, không phải mock**).
4. Mọi request sau đó: frontend gửi `Authorization: Bearer <idToken>` → `JwtAuthenticationFilter` verify qua JWKS.

### Frontend
- Nếu `REACT_APP_COGNITO_USER_POOL_ID` được điền → login trực tiếp qua `amazon-cognito-identity-js` (`CognitoUser.authenticateUser`), lưu idToken vào `localStorage`.
- Ngược lại → fallback về backend `/auth/login` (dev local).

---

## 5. IAM permission cần cho backend

IAM user (đang dùng chung cho S3) phải có thêm policy để gọi Cognito:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "cognito-idp:InitiateAuth",
        "cognito-idp:RespondToAuthChallenge",
        "cognito-idp:GetUser"
      ],
      "Resource": "*"
    }
  ]
}
```

> `cognito-idp:GetUser` dùng khi cần lấy thông tin user theo access token (chưa wire trong code — note cho bước sau).

---

## 6. Kiểm tra nhanh (sau khi điền UserPoolId)

```bash
# 1. Lấy JWKS xem có tồn tại không (user pool id + region đúng là được)
curl "https://cognito-idp.ap-southeast-1.amazonaws.com/<COGNITO_USER_POOL_ID>/.well-known/jwks.json"

# 2. Chạy backend với profile aws
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=aws

# 3. Đăng nhập thử (dùng tài khoản ADMIN demo do admin cấp)
curl -X POST http://localhost:8088/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"<email-demo>","password":"<password>"}'
```

**Expected:** trả về `{ "token": "<JWT Cognito>", "user": { "role": "ADMIN", ... } }`.
**Lỗi hay gặp:**
- `NotAuthorizedException` → sai email/password, hoặc App Client chưa bật `ALLOW_USER_PASSWORD_AUTH`.
- `authenticationResult is null` → user đang ở trạng thái `FORCE_CHANGE_PASSWORD`.
- `UnrecognizedClientException` → access key sai / thiếu quyền `cognito-idp:*`.

---

## 7. TODO tiếp theo (các service kế tiếp)

- [ ] Tạo User Pool mới (mục 2) — Console hoặc CLI
- [ ] Điền `COGNITO_USER_POOL_ID` + `COGNITO_CLIENT_ID` vào `.env` + `frontend/.env`
- [ ] Tạo user test + group `ADMIN`, set password vĩnh viễn
- [ ] Xác nhận App Client bật `USER_PASSWORD_AUTH` (không có secret)
- [ ] Map thêm role `OWNER/EDITOR/MANAGER` từ Cognito groups (nếu cần)
- [ ] Bước tiếp theo trong thiết kế: **API Gateway** (đặt Spring Boot đằng sau API Gateway + Cognito Authorizer) hoặc **Textract OCR** (thay `LocalOcrService`)
