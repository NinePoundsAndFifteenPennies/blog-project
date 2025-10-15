import axios from 'axios'

/**
 * 上传头像（首次）
 * @param {File} file - 图片文件
 * @returns {Promise<string>} - 返回头像URL
 */
export async function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)

    const token = localStorage.getItem('token')
    const response = await axios.post('/api/files/upload/avatar', formData, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    return response.data.avatarUrl
}

/**
 * 更新头像
 * @param {File} file - 图片文件
 * @returns {Promise<string>} - 返回新头像URL
 */
export async function updateAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)

    const token = localStorage.getItem('token')
    const response = await axios.put('/api/files/update/avatar', formData, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    return response.data.avatarUrl
}

/**
 * 保存头像URL到用户信息
 * @param {string} avatarUrl - 头像URL
 * @returns {Promise<Object>} - 返回更新后的用户信息
 */
export async function saveUserAvatar(avatarUrl) {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/users/me/avatar', 
        { avatarUrl }, 
        {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        }
    )
    return response.data
}
