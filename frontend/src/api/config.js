// Cấu hình endpoint - backend Spring Boot và CloudFront
// API_URL mặc định trỏ về backend local (dev). Khi deploy lên Lambda/API Gateway,
// set biến REACT_APP_API_URL trong frontend/.env bằng URL execute-api.
export const CONFIG = {
  API_URL: process.env.REACT_APP_API_URL || "http://localhost:8088",
  API_GATEWAY_URL: process.env.REACT_APP_API_GATEWAY_URL || "https://2jp2rb2d8h.execute-api.ap-southeast-1.amazonaws.com/Prod",
  CLOUDFRONT_URL: process.env.REACT_APP_CLOUDFRONT_URL || "https://d1224pvtm2yk1h.cloudfront.net",
};

export function getCloudFrontUrl(path = "") {
  const baseUrl = CONFIG.CLOUDFRONT_URL.replace(/\/+$/, "");
  const normalizedPath = String(path || "").replace(/^\/+/, "");
  return normalizedPath ? `${baseUrl}/${normalizedPath}` : baseUrl;
}
