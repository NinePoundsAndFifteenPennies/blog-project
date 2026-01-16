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
 * 获取当前用户的所有文章（包括草稿）
 * @param {Object} params - 查询参数 { page, size }
 */
export function getMyPosts(params) {
    return request({
        url: '/posts/my',
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

/**
 * 获取指定用户的已发布文章（公开接口）
 * @param {String} username - 用户名
 * @param {Object} params - 查询参数 { page, size }
 */
export function getPostsByUsername(username, params) {
    return request({
        url: `/posts/user/${username}`,
        method: 'get',
        params: params
    })
}

/**
 * 从文章中移除标签（软删除）
 * @param {Number} postId - 文章ID
 * @param {String} tagName - 标签名称
 */
export function removeTagFromPost(postId, tagName) {
    return request({
        url: `/posts/${postId}/tags/${encodeURIComponent(tagName)}`,
        method: 'delete'
    })
}

/**
 * 从文章中移除分类（软删除）
 * @param {Number} postId - 文章ID
 */
export function removeCategoryFromPost(postId) {
    return request({
        url: `/posts/${postId}/category`,
        method: 'delete'
    })
}

/**
 * 搜索文章
 * 支持多维度搜索：关键词（标题+内容+作者昵称+标签）、作者、标题、标签
 * @param {Object} params - 搜索参数
 * @param {String} params.keyword - 通用关键词，匹配标题、内容、作者昵称/用户名和标签名称
 * @param {String} params.author - 作者用户名或昵称（模糊匹配）
 * @param {String} params.title - 标题关键词（模糊匹配）
 * @param {String} params.tag - 标签名称（模糊匹配）
 * @param {String} params.sortBy - 排序方式：time（默认）或 hotness
 * @param {String} params.order - 排序顺序：desc（默认）或 asc
 * @param {Number} params.page - 页码（从0开始）
 * @param {Number} params.size - 每页数量
 */
export function searchPosts(params) {
    return request({
        url: '/posts/search',
        method: 'get',
        params: params
    })
}