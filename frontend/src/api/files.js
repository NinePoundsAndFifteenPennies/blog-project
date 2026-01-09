import request from '@/utils/request';

/**
 * 上传头像（首次）
 * @param {File} file - 图片文件
 * @returns {Promise<string>} - 返回头像URL
 */
export async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await request({
        url: '/files/upload/avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.avatarUrl;
}

/**
 * 更新头像
 * @param {File} file - 图片文件
 * @returns {Promise<string>} - 返回新头像URL
 */
export async function updateAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await request({
        url: '/files/update/avatar',
        method: 'put',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.avatarUrl;
}

/**
 * 保存头像URL到用户信息
 * @param {string} avatarUrl - 头像URL
 * @returns {Promise<Object>} - 返回更新后的用户信息
 */
export async function saveUserAvatar(avatarUrl) {
    return request({
        url: '/users/me/avatar',
        method: 'post',
        data: { avatarUrl },
    });
}

/**
 * 上传封面图片
 * @param {File} file - 图片文件
 * @returns {Promise<string>} - 返回图片URL
 */
export async function uploadCoverImage(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await request({
        url: '/files/upload/cover',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.imageUrl;
}

/**
 * 删除图片
 * @param {string} imageUrl - 图片URL
 * @returns {Promise<Object>} - 返回删除结果
 */
export async function deleteImage(imageUrl) {
    return request({
        url: '/files/delete',
        method: 'delete',
        params: { imageUrl },
    });
}