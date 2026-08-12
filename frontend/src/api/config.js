// Cấu hình endpoint - backend Spring Boot và CloudFront
export const CONFIG = {
  API_URL: "https://wwwl5so707.execute-api.ap-southeast-1.amazonaws.com/prod",
  CLOUDFRONT_URL: "https://d1224pvtm2yk1h.cloudfront.net",
};

export function getCloudFrontUrl(path = "") {
  const baseUrl = CONFIG.CLOUDFRONT_URL.replace(/\/+$/, "");
  const normalizedPath = String(path || "").replace(/^\/+/, "");
  return normalizedPath ? `${baseUrl}/${normalizedPath}` : baseUrl;
}
