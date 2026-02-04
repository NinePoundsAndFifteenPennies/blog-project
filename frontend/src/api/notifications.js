import request from '@/utils/request'

/**
 * 获取通知列表
 * @param {Object} params - 分页和过滤参数 { page, size, filter }
 * @returns {Promise<Object>} - 分页通知数据
 */
export function getNotifications(params) {
    return request({
        url: '/notifications',
        method: 'get',
        params: params
    })
}

/**
 * 获取最近的通知（用于下拉预览）
 * @returns {Promise<Array>} - 最近的通知列表
 */
export function getRecentNotifications() {
    return request({
        url: '/notifications/recent',
        method: 'get'
    })
}

/**
 * 获取未读通知数量
 * @returns {Promise<Object>} - { count: number }
 */
export function getNotificationUnreadCount() {
    return request({
        url: '/notifications/unread/count',
        method: 'get'
    })
}

/**
 * 标记单个通知为已读
 * @param {Number} notificationId - 通知ID
 * @returns {Promise<void>}
 */
export function markNotificationAsRead(notificationId) {
    return request({
        url: `/notifications/${notificationId}/read`,
        method: 'put'
    })
}

/**
 * 标记所有通知为已读
 * @param {String} filter - 过滤器: "all", "comments", "likes", "follows"
 * @returns {Promise<Object>} - { markedCount: number }
 */
export function markAllNotificationsAsRead(filter = 'all') {
    return request({
        url: '/notifications/read',
        method: 'put',
        params: { filter }
    })
}

/**
 * 获取文章拒绝/删除表单详情（用于用户查看被拒绝或删除的原因）
 * @param {Number} postId - 文章ID
 * @returns {Promise<Object>} - 表单详情
 */
export function getFormByPostId(postId) {
    return request({
        url: `/notifications/forms/post/${postId}`,
        method: 'get'
    })
}

/**
 * 根据通知ID获取关联的表单详情（用于文章被删除后仍能查看原因）
 * @param {Number} notificationId - 通知ID
 * @returns {Promise<Object>} - 表单详情
 */
export function getFormByNotificationId(notificationId) {
    return request({
        url: `/notifications/${notificationId}/form`,
        method: 'get'
    })
}
