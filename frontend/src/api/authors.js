import request from '@/utils/request'

/**
 * 获取热门作者列表
 * @param {Object} params - 查询参数 { limit }
 * @returns {Promise<Array>} - 热门作者列表
 */
export function getHotAuthors(params) {
    return request({
        url: '/authors/hot',
        method: 'get',
        params: params
    })
}
