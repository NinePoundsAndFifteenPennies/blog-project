/**
 * 获取完整的头像URL
 * 如果是相对路径，则拼接后端服务器地址
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

  // 如果是相对路径，拼接后端服务器地址
  // 在开发环境中，后端服务器地址是 http://localhost:8080
  // 在生产环境中，可以通过环境变量配置
  const backendUrl = process.env.VUE_APP_BACKEND_URL || 'http://localhost:8080'
  
  // 确保URL路径正确拼接（处理可能的双斜杠）
  const url = avatarUrl.startsWith('/') ? avatarUrl : `/${avatarUrl}`
  return `${backendUrl}${url}`
}
