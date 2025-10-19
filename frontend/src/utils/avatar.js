/**
 * 获取完整的头像URL
 * 在开发环境中，返回相对路径让dev server的proxy处理
 * 在生产环境中，如果需要则拼接后端服务器地址
 * @param {string} avatarUrl - 头像URL（可能是相对路径或完整URL）
 * @returns {string} 完整的头像URL
 */
export function getFullAvatarUrl(avatarUrl) {
    if (!avatarUrl) {
        return null
    }

    // 如果已经是完整的URL（以http://或https://开头），直接返回
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
        return avatarUrl
    }

    // 在开发环境中，返回相对路径，让vue dev server的proxy转发到后端
    // proxy配置在 vue.config.js 中: /uploads -> http://localhost:8080/uploads
    if (process.env.NODE_ENV === 'development') {
        const url = avatarUrl.startsWith('/') ? avatarUrl : `/${avatarUrl}`
        return url
    }

    // 在生产环境中，如果配置了后端URL，则拼接完整地址
    // 如果没有配置，返回相对路径（适用于前后端部署在同一域名的情况）
    if (process.env.VUE_APP_BACKEND_URL) {
        const url = avatarUrl.startsWith('/') ? avatarUrl : `/${avatarUrl}`
        return `${process.env.VUE_APP_BACKEND_URL}${url}`
    }

    // 默认返回相对路径
    return avatarUrl.startsWith('/') ? avatarUrl : `/${avatarUrl}`
}