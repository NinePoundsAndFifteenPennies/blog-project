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

// ======================= 文章管理 API =======================

/**
 * 获取文章列表（支持分页和多条件搜索）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从0开始）
 * @param {number} params.size - 每页数量
 * @param {string} params.title - 标题搜索（可选）
 * @param {string} params.author - 作者搜索（可选）
 * @param {string} params.status - 状态过滤（可选）
 * @param {string} params.categoryId - 分类ID过滤（可选）
 * @param {string} params.tag - 标签过滤（可选）
 * @param {string} params.startDate - 创建开始日期 yyyy-MM-dd（可选）
 * @param {string} params.endDate - 创建结束日期 yyyy-MM-dd（可选）
 * @returns {Promise<Object>} - 返回分页文章列表
 */
export async function getPosts(params = {}) {
    const response = await request({
        url: "/admin/posts",
        method: "get",
        params: {
            page: params.page || 0,
            size: params.size || 10,
            title: params.title || undefined,
            author: params.author || undefined,
            status: params.status || undefined,
            categoryId: params.categoryId || undefined,
            tag: params.tag || undefined,
            startDate: params.startDate || undefined,
            endDate: params.endDate || undefined,
        },
    });
    return response;
}

/**
 * 获取文章详情
 * @param {number} postId - 文章ID
 * @returns {Promise<Object>} - 返回文章详情
 */
export async function getPostDetail(postId) {
    const response = await request({
        url: `/admin/posts/${postId}`,
        method: "get",
    });
    return response;
}

/**
 * 执行文章操作（审核通过/拒绝/删除）
 * @param {Object} data - 操作数据
 * @param {string} data.action - 操作类型: APPROVE, REJECT, DELETE
 * @param {number[]} data.postIds - 文章ID列表
 * @param {string} data.formTitle - 表单标题（拒绝/删除时可选）
 * @param {string} data.reason - 理由（拒绝/删除时必填）
 * @param {string} data.extraFields - 扩展字段JSON（可选）
 * @returns {Promise<Object>} - 返回批量操作结果
 */
export async function executePostAction(data) {
    const response = await request({
        url: "/admin/posts/action",
        method: "post",
        data,
    });
    return response;
}

/**
 * 获取表单详情
 * @param {number} formId - 表单ID
 * @returns {Promise<Object>} - 返回表单详情
 */
export async function getFormDetail(formId) {
    const response = await request({
        url: `/admin/forms/${formId}`,
        method: "get",
    });
    return response;
}

/**
 * 获取文章关联的表单列表
 * @param {number} postId - 文章ID
 * @returns {Promise<Object>} - 返回表单列表
 */
export async function getPostForms(postId) {
    const response = await request({
        url: `/admin/posts/${postId}/forms`,
        method: "get",
    });
    return response;
}

// ======================= 评论管理 API =======================

/**
 * 获取评论列表（支持分页和多条件搜索）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从0开始）
 * @param {number} params.size - 每页数量
 * @param {string} params.content - 评论内容搜索（可选）
 * @param {string} params.author - 作者搜索（可选）
 * @param {string} params.postTitle - 文章标题搜索（可选）
 * @param {string} params.status - 状态过滤 PENDING/APPROVED（可选）
 * @param {string} params.startDate - 创建开始日期 yyyy-MM-dd（可选）
 * @param {string} params.endDate - 创建结束日期 yyyy-MM-dd（可选）
 * @param {boolean} params.includeReplies - 是否包含子评论（默认true）
 * @returns {Promise<Object>} - 返回分页评论列表
 */
export async function getComments(params = {}) {
    const response = await request({
        url: "/admin/comments",
        method: "get",
        params: {
            page: params.page || 0,
            size: params.size || 10,
            content: params.content || undefined,
            author: params.author || undefined,
            postTitle: params.postTitle || undefined,
            status: params.status || undefined,
            startDate: params.startDate || undefined,
            endDate: params.endDate || undefined,
            includeReplies: params.includeReplies !== undefined ? params.includeReplies : true,
        },
    });
    return response;
}

/**
 * 获取评论详细信息
 * @param {number} commentId - 评论ID
 * @returns {Promise<Object>} - 返回评论详细信息
 */
export async function getCommentDetail(commentId) {
    const response = await request({
        url: `/admin/comments/${commentId}`,
        method: "get",
    });
    return response;
}

/**
 * 执行评论操作（审核通过/删除）
 * @param {Object} data - 操作数据
 * @param {string} data.action - 操作类型: APPROVE, DELETE
 * @param {number[]} data.commentIds - 评论ID列表
 * @param {string} data.formTitle - 通知标题（删除时可选，用于发送给评论作者的通知）
 * @param {string} data.reason - 删除理由（删除时必填）
 * @param {string} data.extraFields - 扩展字段JSON（可选）
 * @returns {Promise<Object>} - 返回批量操作结果
 */
export async function executeCommentAction(data) {
    const response = await request({
        url: "/admin/comments/action",
        method: "post",
        data,
    });
    return response;
}

// ======================= 标签管理 API =======================

/**
 * 获取标签列表（支持分页和多条件搜索，按热度排序）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从0开始）
 * @param {number} params.size - 每页数量
 * @param {string} params.name - 标签名称搜索（可选）
 * @param {string} params.createdBy - 创建者用户名搜索（可选）
 * @param {string} params.startDate - 创建开始日期 yyyy-MM-dd（可选）
 * @param {string} params.endDate - 创建结束日期 yyyy-MM-dd（可选）
 * @returns {Promise<Object>} - 返回分页标签列表
 */
export async function getAdminTags(params = {}) {
    const response = await request({
        url: "/admin/tags",
        method: "get",
        params: {
            page: params.page || 0,
            size: params.size || 10,
            name: params.name || undefined,
            createdBy: params.createdBy || undefined,
            startDate: params.startDate || undefined,
            endDate: params.endDate || undefined,
        },
    });
    return response;
}

/**
 * 获取标签详细信息
 * @param {number} tagId - 标签ID
 * @returns {Promise<Object>} - 返回标签详细信息
 */
export async function getAdminTagDetail(tagId) {
    const response = await request({
        url: `/admin/tags/${tagId}`,
        method: "get",
    });
    return response;
}

/**
 * 管理员创建标签
 * @param {Object} tagData - 标签数据 { name, description, color, icon, sortOrder }
 * @returns {Promise<Object>} - 返回创建的标签
 */
export async function createAdminTag(tagData) {
    const response = await request({
        url: "/admin/tags",
        method: "post",
        data: tagData,
    });
    return response;
}

/**
 * 管理员更新标签
 * @param {number} tagId - 标签ID
 * @param {Object} tagData - 标签数据
 * @returns {Promise<Object>} - 返回更新后的标签
 */
export async function updateAdminTag(tagId, tagData) {
    const response = await request({
        url: `/admin/tags/${tagId}`,
        method: "put",
        data: tagData,
    });
    return response;
}

/**
 * 执行标签操作（软删除/硬删除）
 * @param {Object} data - 操作数据
 * @param {string} data.action - 操作类型: SOFT_DELETE, HARD_DELETE
 * @param {number[]} data.tagIds - 标签ID列表
 * @param {string} data.formTitle - 通知标题（可选）
 * @param {string} data.reason - 删除理由（必填）
 * @param {string} data.extraFields - 扩展字段JSON（可选）
 * @returns {Promise<Object>} - 返回批量操作结果
 */
export async function executeTagAction(data) {
    const response = await request({
        url: "/admin/tags/action",
        method: "post",
        data,
    });
    return response;
}

// ======================= 分类管理 API =======================

/**
 * 获取分类列表（支持分页和多条件搜索，按文章数排序）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码（从0开始）
 * @param {number} params.size - 每页数量
 * @param {string} params.name - 分类名称搜索（可选）
 * @param {string} params.createdBy - 创建者用户名搜索（可选）
 * @param {string} params.startDate - 创建开始日期 yyyy-MM-dd（可选）
 * @param {string} params.endDate - 创建结束日期 yyyy-MM-dd（可选）
 * @returns {Promise<Object>} - 返回分页分类列表
 */
export async function getAdminCategories(params = {}) {
    const response = await request({
        url: "/admin/categories",
        method: "get",
        params: {
            page: params.page || 0,
            size: params.size || 10,
            name: params.name || undefined,
            createdBy: params.createdBy || undefined,
            startDate: params.startDate || undefined,
            endDate: params.endDate || undefined,
        },
    });
    return response;
}

/**
 * 获取分类详细信息
 * @param {number} categoryId - 分类ID
 * @returns {Promise<Object>} - 返回分类详细信息
 */
export async function getAdminCategoryDetail(categoryId) {
    const response = await request({
        url: `/admin/categories/${categoryId}`,
        method: "get",
    });
    return response;
}

/**
 * 管理员创建分类
 * @param {Object} categoryData - 分类数据 { name, description, color, icon, sortOrder }
 * @returns {Promise<Object>} - 返回创建的分类
 */
export async function createAdminCategory(categoryData) {
    const response = await request({
        url: "/admin/categories",
        method: "post",
        data: categoryData,
    });
    return response;
}

/**
 * 管理员更新分类
 * @param {number} categoryId - 分类ID
 * @param {Object} categoryData - 分类数据
 * @returns {Promise<Object>} - 返回更新后的分类
 */
export async function updateAdminCategory(categoryId, categoryData) {
    const response = await request({
        url: `/admin/categories/${categoryId}`,
        method: "put",
        data: categoryData,
    });
    return response;
}

/**
 * 将文章归入指定分类
 * @param {number} categoryId - 分类ID
 * @param {number} postId - 文章ID
 * @returns {Promise<Object>} - 返回更新后的分类详情
 */
export async function assignPostToCategory(categoryId, postId) {
    const response = await request({
        url: `/admin/categories/${categoryId}/posts/${postId}`,
        method: "post",
    });
    return response;
}

/**
 * 将文章从指定分类移除
 * @param {number} categoryId - 分类ID
 * @param {number} postId - 文章ID
 * @returns {Promise<Object>} - 返回更新后的分类详情
 */
export async function removePostFromCategory(categoryId, postId) {
    const response = await request({
        url: `/admin/categories/${categoryId}/posts/${postId}`,
        method: "delete",
    });
    return response;
}

/**
 * 执行分类操作（删除）
 * @param {Object} data - 操作数据
 * @param {string} data.action - 操作类型: DELETE
 * @param {number[]} data.categoryIds - 分类ID列表
 * @param {string} data.reason - 删除理由（必填）
 * @param {string} data.extraFields - 扩展字段JSON（可选）
 * @returns {Promise<Object>} - 返回批量操作结果
 */
export async function executeCategoryAction(data) {
    const response = await request({
        url: "/admin/categories/action",
        method: "post",
        data,
    });
    return response;
}
