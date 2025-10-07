import request from '@/utils/request'

/**
 * 获取文章列表(分页)
 * @param {Object} params - 查询参数 { page, size }
 */
export function getPosts(params) {
    return request({
        url: '/posts',
        method: 'get',
        params: params
    })
}

/**
 * 根据ID获取文章详情
 * @param {Number} id - 文章ID
 */
export function getPostById(id) {
    return request({
        url: `/posts/${id}`,
        method: 'get'
    })
}

/**
 * 创建文章
 * @param {Object} postData - 文章数据 { title, content, contentType: "MARKDOWN" }
 */
export function createPost(postData) {
    return request({
        url: '/posts',
        method: 'post',
        data: {
            ...postData,
            contentType: 'MARKDOWN'
        }
    })
}

/**
 * 更新文章
 * @param {Number} id - 文章ID
 * @param {Object} postData - 文章数据
 */
export function updatePost(id, postData) {
    return request({
        url: `/posts/${id}`,
        method: 'put',
        data: {
            ...postData,
            contentType: 'MARKDOWN'
        }
    })
}

/**
 * 删除文章
 * @param {Number} id - 文章ID
 */
export function deletePost(id) {
    return request({
        url: `/posts/${id}`,
        method: 'delete'
    })
}