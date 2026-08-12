import { AuthenticationDetails, CognitoUser } from "amazon-cognito-identity-js";
import { apiFetch, getToken } from "./client";
import { getCognitoUserPool, isCognitoConfigured } from "./cognito.config";

const TOKEN_KEY = "edms_token";
const USER_KEY = "edms_user";

export async function login(email, password) {
  if (!email || !password) {
    throw new Error("Email and password are required");
  }

  if (isCognitoConfigured()) {
    return await cognitoLogin(email, password);
  }

  return await backendLogin(email, password);
}

async function backendLogin(email, password) {
  const data = await apiFetch("/auth/login", {
    method: "POST",
    body: { email, password },
  });

  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));

  return { token: data.token, user: data.user };
}

function cognitoLogin(email, password) {
  const userPool = getCognitoUserPool();
  const cognitoUser = new CognitoUser({ Username: email, Pool: userPool });
  const authDetails = new AuthenticationDetails({ Username: email, Password: password });

  return new Promise((resolve, reject) => {
    cognitoUser.authenticateUser(authDetails, {
      onSuccess: (session) => {
        const idToken = session.getIdToken().getJwtToken();
        const payload = JSON.parse(atob(idToken.split(".")[1]));
        const groups = payload["cognito:groups"] || [];

        const user = {
          id: payload.sub,
          email: payload.email || email,
          name: payload.name || payload.email || email,
          role: groups.includes("ADMIN") ? "ADMIN" : "VIEWER",
          department: groups[0] || null,
        };

        localStorage.setItem(TOKEN_KEY, idToken);
        localStorage.setItem(USER_KEY, JSON.stringify(user));

        resolve({ token: idToken, user });
      },
      onFailure: (err) => {
        reject(new Error(err?.message || "Đăng nhập thất bại"));
      },
      newPasswordRequired: () => {
        reject(new Error("Cần đổi mật khẩu tạm thời trước khi đăng nhập"));
      },
    });
  });
}

export async function logout() {
  try {
    if (isCognitoConfigured()) {
      const userPool = getCognitoUserPool();
      const token = getToken();
      if (token) {
        const payload = JSON.parse(atob(token.split(".")[1]));
        const cognitoUser = new CognitoUser({ Username: payload.email, Pool: userPool });
        cognitoUser.signOut();
      }
    } else {
      await apiFetch("/auth/logout", { method: "POST" });
    }
  } catch {
    // best-effort
  }
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getCurrentUser() {
  const token = localStorage.getItem(TOKEN_KEY);
  const userStr = localStorage.getItem(USER_KEY);

  if (!token || !userStr) {
    return null;
  }

  try {
    const user = JSON.parse(userStr);
    return { token, user };
  } catch {
    return null;
  }
}

export function isAuthenticated() {
  return !!localStorage.getItem(TOKEN_KEY);
}

export { getToken };
