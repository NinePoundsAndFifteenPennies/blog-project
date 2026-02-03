import request from '@/utils/request'

// 注意：刷新Token请使用 /api/users/refresh-token 接口（在 auth.js 中）
// 该接口对所有已认证用户通用，包括管理员

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

// ======================= 用户管理 API =======================

/**
 * 获取用户列表（支持分页和多条件搜索）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从0开始）
 * @param {number} params.size - 每页数量
 * @param {string} params.username - 用户名搜索（可选）
 * @param {string} params.email - 邮箱搜索（可选）
 * @param {string} params.role - 角色过滤 USER/ADMIN（可选）
 * @param {boolean} params.enabled - 状态过滤（可选）
 * @param {string} params.startDate - 注册开始日期 yyyy-MM-dd（可选）
 * @param {string} params.endDate - 注册结束日期 yyyy-MM-dd（可选）
 * @returns {Promise<Object>} - 返回分页用户列表
 */
export async function getUsers(params = {}) {
    const response = await request({
        url: "/admin/users",
        method: "get",
        params: {
            page: params.page || 0,
            size: params.size || 10,
            username: params.username || undefined,
            email: params.email || undefined,
            role: params.role || undefined,
            enabled: params.enabled,
            startDate: params.startDate || undefined,
            endDate: params.endDate || undefined,
        },
    });
    return response;
}

/**
 * 获取用户详细信息（包含发文数、评论数统计）
 * @param {number} userId - 用户ID
 * @returns {Promise<Object>} - 返回用户详细信息
 */
export async function getUserDetail(userId) {
    const response = await request({
        url: `/admin/users/${userId}`,
        method: "get",
    });
    return response;
}

/**
 * 更新用户状态（启用/禁用）
 * @param {number} userId - 用户ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise<Object>} - 返回更新后的用户信息
 */
export async function updateUserStatus(userId, enabled) {
    const response = await request({
        url: `/admin/users/${userId}/status`,
        method: "put",
        data: { enabled },
    });
    return response;
}

/**
 * 更新用户角色
 * @param {number} userId - 用户ID
 * @param {string} role - 新角色 USER/ADMIN
 * @returns {Promise<Object>} - 返回更新后的用户信息
 */
export async function updateUserRole(userId, role) {
    const response = await request({
        url: `/admin/users/${userId}/role`,
        method: "put",
        data: { role },
    });
    return response;
}
