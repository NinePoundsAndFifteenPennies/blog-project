import request from '@/utils/request'  // ../utils/request.js

/**
 * 用户注册
 * @param {Object} userData - 用户数据 { username, email, password }
 */
export function register(userData) {
  return request({
    url: "/users/register",
    method: "post",
    data: userData,
  });
}

/**
 * 用户登录
 * @param {Object} credentials - 登录凭证 { username, password }
 */
export function login(credentials) {
  return request({
    url: "/users/login",
    method: "post",
    data: credentials,
  });
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: "/users/me",
    method: "get",
  });
}

/**
 * 用户登出
 */
export function logout() {
  // 清除本地存储
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  return Promise.resolve();
}
