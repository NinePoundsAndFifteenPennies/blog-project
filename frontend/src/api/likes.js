import request from '@/utils/request'

/**
 * 点赞文章
 * @param {Number} postId - 文章ID
 */
export function likePost(postId) {
    return request({
        url: `/posts/${postId}/likes`,
        method: 'post'
    })
}

/**
 * 取消点赞
 * @param {Number} postId - 文章ID
 */
export function unlikePost(postId) {
    return request({
        url: `/posts/${postId}/likes`,
        method: 'delete'
    })
}

/**
 * 获取点赞信息
 * @param {Number} postId - 文章ID
 */
export function getLikeInfo(postId) {
    return request({
        url: `/posts/${postId}/likes`,
        method: 'get'
    })
}
