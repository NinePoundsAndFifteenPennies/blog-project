import request from '@/utils/request'

/**
 * 获取所有标签（分页）
 * @param {Object} params - 查询参数 { page, size }
 */
export function getTags(params) {
    return request({
        url: '/tags',
        method: 'get',
        params: params
    })
}

/**
 * 获取热门标签
 */
export function getPopularTags() {
    return request({
        url: '/tags/popular',
        method: 'get'
    })
}

/**
 * 根据ID获取标签详情
 * @param {Number} id - 标签ID
 */
export function getTagById(id) {
    return request({
        url: `/tags/${id}`,
        method: 'get'
    })
}

/**
 * 根据名称获取标签详情
 * @param {String} name - 标签名称
 */
export function getTagByName(name) {
    return request({
        url: `/tags/name/${encodeURIComponent(name)}`,
        method: 'get'
    })
}

/**
 * 创建标签
 * @param {Object} tagData - 标签数据 { name, description, color, icon, sortOrder }
 */
export function createTag(tagData) {
    return request({
        url: '/tags',
        method: 'post',
        data: tagData
    })
}

/**
 * 更新标签
 * @param {Number} id - 标签ID
 * @param {Object} tagData - 标签数据
 */
export function updateTag(id, tagData) {
    return request({
        url: `/tags/${id}`,
        method: 'put',
        data: tagData
    })
}

/**
 * 删除标签
 * @param {Number} id - 标签ID
 */
export function deleteTag(id) {
    return request({
        url: `/tags/${id}`,
        method: 'delete'
    })
}
