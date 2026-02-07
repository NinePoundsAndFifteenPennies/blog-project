import request from '@/utils/request'

/**
 * 获取所有分类（分页）
 * @param {Object} params - 查询参数 { page, size }
 */
export function getCategories(params) {
    return request({
        url: '/categories',
        method: 'get',
        params: params
    })
}

/**
 * 获取热门分类
 */
export function getPopularCategories() {
    return request({
        url: '/categories/popular',
        method: 'get'
    })
}

/**
 * 根据ID获取分类详情
 * @param {Number} id - 分类ID
 */
export function getCategoryById(id) {
    return request({
        url: `/categories/${id}`,
        method: 'get'
    })
}

/**
 * 根据名称获取分类详情
 * @param {String} name - 分类名称
 */
export function getCategoryByName(name) {
    return request({
        url: `/categories/name/${encodeURIComponent(name)}`,
        method: 'get'
    })
}

/**
 * 创建分类
 * @param {Object} categoryData - 分类数据 { name, description, color, icon, sortOrder }
 */
export function createCategory(categoryData) {
    return request({
        url: '/categories',
        method: 'post',
        data: categoryData
    })
}

/**
 * 更新分类
 * @param {Number} id - 分类ID
 * @param {Object} categoryData - 分类数据
 */
export function updateCategory(id, categoryData) {
    return request({
        url: `/categories/${id}`,
        method: 'put',
        data: categoryData
    })
}

/**
 * 删除分类
 * @param {Number} id - 分类ID
 */
export function deleteCategory(id) {
    return request({
        url: `/categories/${id}`,
        method: 'delete'
    })
}
