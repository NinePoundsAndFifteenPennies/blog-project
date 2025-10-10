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
 * @returns {Promise<Object>} - 返回用户对象 { username, ... }
 */
export async function getCurrentUser() {
    // 后端返回的是一个字符串 "当前登录用户是: aaaa"
    // 我们需要解析这个字符串，并模拟一个 user 对象
    const responseText = await request({
        url: "/users/me",
        method: "get",
    });

    // 从 "当前登录用户是: username" 中提取 username
    const username = responseText.replace('当前登录用户是: ', '');

    // 为了和localStorage保持一致，我们返回一个包含username的对象
    // 实际项目中，后端最好直接返回JSON对象
    return { username };
}

/**
 * 用户登出 (本地操作)
 */
export function logout() {
    // 登出只是本地操作，清除token即可
    return Promise.resolve();
}