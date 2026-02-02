import request from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} credentials - 登录凭证 { username, password, rememberMe }
 * @returns {Promise<string>} - 返回JWT token
 */
export async function adminLogin(credentials) {
    const response = await request({
        url: "/admin/login",
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
 * 获取当前管理员信息
 * @returns {Promise<Object>} - 返回管理员用户对象
 */
export async function getCurrentAdmin() {
    const response = await request({
        url: "/admin/me",
        method: "get",
    });
    return response;
}

/**
 * 管理员刷新Token
 * @param {boolean} rememberMe - 是否记住我
 * @returns {Promise<string>} - 返回新的JWT token
 */
export async function adminRefreshToken(rememberMe) {
    const response = await request({
        url: "/admin/refresh-token",
        method: "post",
        data: rememberMe || false,
    });
    return response.accessToken;
}

/**
 * 获取管理员仪表盘数据
 * @returns {Promise<Object>} - 返回仪表盘数据
 */
export async function getDashboard() {
    const response = await request({
        url: "/admin/dashboard",
        method: "get",
    });
    return response;
}
