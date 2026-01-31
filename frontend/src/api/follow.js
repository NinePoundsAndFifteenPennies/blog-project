import request from '@/utils/request'

/**
 * 关注用户
 * @param {Number} userId - 要关注的用户ID
 * @returns {Promise<Object>} - { following, friend, message }
 */
export function followUser(userId) {
    return request({
        url: `/users/${userId}/follow`,
        method: 'post'
    })
}

/**
 * 取消关注用户
 * @param {Number} userId - 要取消关注的用户ID
 * @returns {Promise<Object>} - { following, friend, message }
 */
export function unfollowUser(userId) {
    return request({
        url: `/users/${userId}/follow`,
        method: 'delete'
    })
}

/**
 * 获取用户的关注统计数据
 * @param {Number} userId - 用户ID
 * @returns {Promise<Object>} - { followingCount, followerCount, friendCount, isFollowing, isFriend }
 */
export function getFollowStats(userId) {
    return request({
        url: `/users/${userId}/follow/stats`,
        method: 'get'
    })
}

/**
 * 获取用户的关注列表
 * @param {Number} userId - 用户ID
 * @param {Object} params - 分页参数 { page, size }
 * @returns {Promise<Object>} - 分页数据
 */
export function getFollowingList(userId, params) {
    return request({
        url: `/users/${userId}/following`,
        method: 'get',
        params: params
    })
}

/**
 * 获取用户的粉丝列表
 * @param {Number} userId - 用户ID
 * @param {Object} params - 分页参数 { page, size }
 * @returns {Promise<Object>} - 分页数据
 */
export function getFollowersList(userId, params) {
    return request({
        url: `/users/${userId}/followers`,
        method: 'get',
        params: params
    })
}

/**
 * 获取用户的朋友列表（互相关注）
 * @param {Number} userId - 用户ID
 * @param {Object} params - 分页参数 { page, size }
 * @returns {Promise<Object>} - 分页数据
 */
export function getFriendsList(userId, params) {
    return request({
        url: `/users/${userId}/friends`,
        method: 'get',
        params: params
    })
}

/**
 * 获取当前用户的可见性设置
 * @returns {Promise<Object>} - 可见性设置对象
 */
export function getVisibilitySettings() {
    return request({
        url: '/follow/visibility',
        method: 'get'
    })
}

/**
 * 更新可见性设置
 * @param {Object} settings - 可见性设置对象
 * @returns {Promise<Object>} - 更新后的设置
 */
export function updateVisibilitySettings(settings) {
    return request({
        url: '/follow/visibility',
        method: 'put',
        data: settings
    })
}
