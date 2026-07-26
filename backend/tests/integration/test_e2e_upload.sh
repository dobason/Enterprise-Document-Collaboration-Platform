#!/bin/bash
# Integration test end-to-end: login -> upload -> verify metadata -> cleanup test item
# Yêu cầu: đã deploy stack, đã cài `jq`, đã export các biến môi trường bên dưới.
#
# Cách chạy: bash tests/integration/test_e2e_upload.sh
set -e

API_URL="${API_URL:?Chưa set biến API_URL}"
USER_POOL_CLIENT_ID="${USER_POOL_CLIENT_ID:?Chưa set biến USER_POOL_CLIENT_ID}"
TEST_EMAIL="${TEST_EMAIL:?Chưa set biến TEST_EMAIL}"
TEST_PASSWORD="${TEST_PASSWORD:?Chưa set biến TEST_PASSWORD}"
TABLE_NAME="${TABLE_NAME:?Chưa set biến TABLE_NAME}"
DEPARTMENT="SALES"

echo "== [1/6] Đăng nhập lấy JWT token =="
TOKEN=$(aws cognito-idp initiate-auth \
  --auth-flow USER_PASSWORD_AUTH \
  --client-id "$USER_POOL_CLIENT_ID" \
  --auth-parameters USERNAME="$TEST_EMAIL",PASSWORD="$TEST_PASSWORD" \
  --query 'AuthenticationResult.IdToken' --output text)

if [ -z "$TOKEN" ] || [ "$TOKEN" == "None" ]; then
  echo "FAIL: không lấy được token"; exit 1
fi
echo "OK: đã lấy token"

echo "== [2/6] Gọi API xin pre-signed URL =="
RESP=$(curl -s -X POST "$API_URL/documents/upload-url" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"fileName\":\"test-doc.pdf\",\"department\":\"$DEPARTMENT\",\"contentType\":\"application/pdf\"}")

UPLOAD_URL=$(echo "$RESP" | jq -r '.uploadUrl')
DOC_ID=$(echo "$RESP" | jq -r '.docId')
echo "OK: docId=$DOC_ID"

echo "== [3/6] Upload file test lên S3 =="
echo "%PDF-1.4 test file content" > /tmp/test-doc.pdf
curl -s -X PUT -T /tmp/test-doc.pdf -H "Content-Type: application/pdf" "$UPLOAD_URL"
echo "OK: đã upload"

echo "== [4/6] Chờ EventBridge xử lý (5s) rồi kiểm tra DynamoDB =="
sleep 5
ITEM=$(aws dynamodb query --table-name "$TABLE_NAME" \
  --key-condition-expression "PK = :p AND SK = :s" \
  --expression-attribute-values "{\":p\":{\"S\":\"DEPT#$DEPARTMENT\"},\":s\":{\"S\":\"DOC#$DOC_ID\"}}")

COUNT=$(echo "$ITEM" | jq '.Items | length')
if [ "$COUNT" != "1" ]; then
  echo "FAIL: không tìm thấy metadata trong DynamoDB"; exit 1
fi
echo "OK: metadata đã được ghi đúng"

echo "== [5/6] Kiểm tra log CloudWatch có ghi lại request này (thủ công) =="
echo "  -> Vào CloudWatch Logs Insights, filter theo docId=$DOC_ID để xác nhận"

echo "== [6/6] Dọn dẹp item test =="
aws dynamodb delete-item --table-name "$TABLE_NAME" \
  --key "{\"PK\":{\"S\":\"DEPT#$DEPARTMENT\"},\"SK\":{\"S\":\"DOC#$DOC_ID\"}}"
rm -f /tmp/test-doc.pdf

echo "=================================="
echo "  INTEGRATION TEST: PASSED"
echo "=================================="
