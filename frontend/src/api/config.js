// Cấu hình endpoint - backend Spring Boot và CloudFront
export const CONFIG = {
  API_URL: process.env.REACT_APP_API_URL || "http://localhost:8088",
  CLOUDFRONT_URL: process.env.REACT_APP_CLOUDFRONT_URL || "https://d1224pvtm2yk1h.cloudfront.net",
};

export function getCloudFrontUrl(path = "") {
  const baseUrl = CONFIG.CLOUDFRONT_URL.replace(/\/+$/, "");
  const normalizedPath = String(path || "").replace(/^\/+/, "");
  return normalizedPath ? `${baseUrl}/${normalizedPath}` : baseUrl;
}
