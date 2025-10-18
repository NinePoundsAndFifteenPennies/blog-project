import request from '@/utils/request'

/**
 * 创建评论
 * @param {Number} postId - 文章ID
 * @param {String} content - 评论内容
 */
export function createComment(postId, content) {
    return request({
        url: `/posts/${postId}/comments`,
        method: 'post',
        data: { content }
    })
}

/**
 * 获取文章评论列表
 * @param {Number} postId - 文章ID
 * @param {Object} params - 分页参数 { page, size }
 */
export function getPostComments(postId, params = {}) {
    return request({
        url: `/posts/${postId}/comments`,
        method: 'get',
        params: {
            page: params.page || 0,
            size: params.size || 20
        }
    })
}

/**
 * 获取我的评论列表
 * @param {Object} params - 分页参数 { page, size }
 */
export function getMyComments(params = {}) {
    return request({
        url: `/comments/my`,
        method: 'get',
        params: {
            page: params.page || 0,
            size: params.size || 10
        }
    })
}

/**
 * 更新评论
 * @param {Number} commentId - 评论ID
 * @param {String} content - 评论内容
 */
export function updateComment(commentId, content) {
    return request({
        url: `/comments/${commentId}`,
        method: 'put',
        data: { content }
    })
}

/**
 * 删除评论
 * @param {Number} commentId - 评论ID
 */
export function deleteComment(commentId) {
    return request({
        url: `/comments/${commentId}`,
        method: 'delete'
    })
}

/**
 * 点赞评论
 * @param {Number} commentId - 评论ID
 */
export function likeComment(commentId) {
    return request({
        url: `/comments/${commentId}/likes`,
        method: 'post'
    })
}

/**
 * 取消点赞评论
 * @param {Number} commentId - 评论ID
 */
export function unlikeComment(commentId) {
    return request({
        url: `/comments/${commentId}/likes`,
        method: 'delete'
    })
}

/**
 * 获取评论点赞信息
 * @param {Number} commentId - 评论ID
 */
export function getCommentLikeInfo(commentId) {
    return request({
        url: `/comments/${commentId}/likes`,
        method: 'get'
    })
}
