import request from '@/utils/request'

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

// 注意：刷新Token请使用 /api/users/refresh-token 接口（在 auth.js 中）
// 该接口对所有已认证用户通用，包括管理员
