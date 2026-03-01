import request from '@/utils/request'

/**
 * 提交举报
 * @param {Object} data - 举报数据
 * @param {string} data.targetType - 举报类型: POST, COMMENT
 * @param {number} data.targetId - 目标ID
 * @param {string} data.reason - 举报原因
 * @returns {Promise<Object>}
 */
export function submitReport(data) {
    return request({
        url: '/reports',
        method: 'post',
        data
    })
}
