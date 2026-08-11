// Cấu hình Amazon Cognito cho SPA React.
// Khi REACT_APP_COGNITO_USER_POOL_ID được điền, frontend đăng nhập trực tiếp qua Cognito
// (luồng chuẩn SPA). Ngược lại fallback về backend /auth/login (chế độ dev local).
import { CognitoUserPool } from "amazon-cognito-identity-js";

export const COGNITO_CONFIG = {
  UserPoolId: process.env.REACT_APP_COGNITO_USER_POOL_ID,
  ClientId: process.env.REACT_APP_COGNITO_CLIENT_ID,
  region: process.env.REACT_APP_COGNITO_REGION || "ap-southeast-1",
};

export function isCognitoConfigured() {
  return !!(COGNITO_CONFIG.UserPoolId && COGNITO_CONFIG.ClientId);
}

export function getCognitoUserPool() {
  if (!isCognitoConfigured()) {
    throw new Error("Cognito chưa được cấu hình (thiếu COGNITO_USER_POOL_ID trong .env)");
  }
  return new CognitoUserPool({
    UserPoolId: COGNITO_CONFIG.UserPoolId,
    ClientId: COGNITO_CONFIG.ClientId,
  });
}
