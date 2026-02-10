import request from '@/utils/request'

/**
 * 检查 AI 服务状态
 */
export function getAiStatus() {
    return request({
        url: '/ai/status',
        method: 'get'
    })
}

/**
 * 生成文章摘要
 * @param {String} content - 文章内容
 */
export function generateSummary(content) {
    return request({
        url: '/ai/summary',
        method: 'post',
        data: { content }
    })
}

/**
 * 推荐文章标题
 * @param {String} content - 文章内容
 */
export function suggestTitles(content) {
    return request({
        url: '/ai/suggest-titles',
        method: 'post',
        data: { content }
    })
}

/**
 * 推荐文章标签
 * @param {String} title - 文章标题
 * @param {String} content - 文章内容
 */
export function suggestTags(title, content) {
    return request({
        url: '/ai/suggest-tags',
        method: 'post',
        data: { title, content }
    })
}

/**
 * AI 写作辅助（流式输出）
 * @param {String} prompt - 用户提示
 * @param {String} content - 已有内容（可选）
 * @param {Function} onChunk - 接收流式数据的回调
 * @param {Function} onDone - 完成回调
 * @param {Function} onError - 错误回调
 * @returns {AbortController} 用于取消请求
 */
export function assistWriting(prompt, content, onChunk, onDone, onError) {
    const controller = new AbortController()
    const token = localStorage.getItem('token')

    fetch('/api/ai/assist', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify({ prompt, content }),
        signal: controller.signal
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
        }
        const reader = response.body.getReader()
        const decoder = new TextDecoder()

        function read() {
            reader.read().then(({ done, value }) => {
                if (done) {
                    onDone && onDone()
                    return
                }
                const text = decoder.decode(value, { stream: true })
                // Parse SSE data lines
                const lines = text.split('\n')
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        const data = line.slice(5)
                        onChunk && onChunk(data)
                    }
                }
                read()
            }).catch(err => {
                if (err.name !== 'AbortError') {
                    onError && onError(err)
                }
            })
        }
        read()
    })
    .catch(err => {
        if (err.name !== 'AbortError') {
            onError && onError(err)
        }
    })

    return controller
}
