import request from '@/utils/request'

/**
 * 发送私信
 * @param {Number} receiverId - 接收者用户ID
 * @param {String} content - 消息内容
 * @returns {Promise<Object>} - 发送的消息
 */
export function sendMessage(receiverId, content) {
    return request({
        url: `/messages/${receiverId}`,
        method: 'post',
        data: { content }
    })
}

/**
 * 获取与某用户的对话消息
 * @param {Number} partnerId - 对话伙伴ID
 * @param {Object} params - 分页参数 { page, size }
 * @returns {Promise<Object>} - 分页消息数据
 */
export function getConversation(partnerId, params) {
    return request({
        url: `/messages/conversation/${partnerId}`,
        method: 'get',
        params: params
    })
}

/**
 * 获取所有会话列表
 * @returns {Promise<Array>} - 会话列表
 */
export function getConversations() {
    return request({
        url: '/messages/conversations',
        method: 'get'
    })
}

/**
 * 标记与某用户的消息为已读
 * @param {Number} partnerId - 对话伙伴ID
 * @returns {Promise<void>}
 */
export function markAsRead(partnerId) {
    return request({
        url: `/messages/read/${partnerId}`,
        method: 'put'
    })
}

/**
 * 获取未读消息数
 * @returns {Promise<Object>} - { count: number }
 */
export function getUnreadCount() {
    return request({
        url: '/messages/unread/count',
        method: 'get'
    })
}

/**
 * 检查是否可以给用户发送消息
 * @param {Number} receiverId - 接收者用户ID
 * @returns {Promise<Object>} - { canSend: boolean }
 */
export function canSendMessage(receiverId) {
    return request({
        url: `/messages/can-send/${receiverId}`,
        method: 'get'
    })
}
