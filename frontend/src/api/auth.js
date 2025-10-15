import request from '@/utils/request'

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
 * @param {Object} credentials - 登录凭证 { username, password, rememberMe }
 * @returns {Promise<string>} - 返回JWT token
 */
export async function login(credentials) {
    // 后端返回的是 { accessToken: '...', tokenType: 'Bearer' }
    // 我们在这里解构，只返回上层需要的 token 字符串
    const response = await request({
        url: "/users/login",
        method: "post",
        data: {
            username: credentials.username,
            password: credentials.password,
            rememberMe: credentials.rememberMe || false
        },
    });
    return response.accessToken;
}

/**
 * 获取当前用户信息
 * @returns {Promise<Object>} - 返回用户对象 { id, username, email, avatarUrl, createdAt }
 */
export async function getCurrentUser() {
    // 后端返回完整的用户信息JSON对象
    const userResponse = await request({
        url: "/users/me",
        method: "get",
    });

    return userResponse;
}

/**
 * 用户登出 (本地操作)
 */
export function logout() {
    // 登出只是本地操作，清除token即可
    return Promise.resolve();
}

/**
 * 刷新Token
 * @param {boolean} rememberMe - 是否记住我
 * @returns {Promise<string>} - 返回新的JWT token
 */
export async function refreshToken(rememberMe) {
    const response = await request({
        url: "/users/refresh-token",
        method: "post",
        data: {
            rememberMe: rememberMe || false
        },
    });
    return response.accessToken;
}