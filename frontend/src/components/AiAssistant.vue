<template>
  <div class="card p-6">
    <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
      <div class="flex items-center space-x-2">
        <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
        </svg>
        <h3 class="text-lg font-semibold text-gray-900">AI 助手</h3>
      </div>
      <span v-if="!aiAvailable" class="text-xs text-yellow-600 bg-yellow-50 px-2 py-1 rounded">
        未配置
      </span>
      <span v-else class="text-xs text-green-600 bg-green-50 px-2 py-1 rounded">
        可用
      </span>
    </div>

    <div v-if="!aiAvailable" class="text-center py-4 text-gray-400 text-sm">
      <p>AI 功能需要配置 API Key 后启用</p>
    </div>

    <div v-else class="space-y-3">
      <!-- 快捷功能按钮 -->
      <div class="grid grid-cols-3 gap-2">
        <button
          @click="handleSuggestTitles"
          :disabled="!content || loading"
          class="ai-action-btn"
          title="根据内容推荐标题"
        >
          <svg class="w-4 h-4 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
          <span class="text-xs">推荐标题</span>
        </button>
        <button
          @click="handleSuggestTags"
          :disabled="!content || loading"
          class="ai-action-btn"
          title="根据内容推荐标签"
        >
          <svg class="w-4 h-4 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
          </svg>
          <span class="text-xs">推荐标签</span>
        </button>
        <button
          @click="handleGenerateSummary"
          :disabled="!content || loading"
          class="ai-action-btn"
          title="生成文章摘要"
        >
          <svg class="w-4 h-4 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <span class="text-xs">生成摘要</span>
        </button>
      </div>

      <!-- 写作辅助输入 -->
      <div class="space-y-2">
        <label class="text-sm font-medium text-gray-700">写作辅助</label>
        <textarea
          v-model="aiPrompt"
          placeholder="告诉 AI 你需要什么帮助，例如：帮我续写下一段、润色这段文字、给这篇文章写个结尾..."
          class="w-full h-20 px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 resize-none"
          :disabled="loading"
        ></textarea>
        <div class="flex space-x-2">
          <button
            @click="handleAssistWriting"
            :disabled="!aiPrompt || loading"
            class="flex-1 px-3 py-2 bg-purple-600 text-white text-sm rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          >
            <div v-if="loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
            <svg v-else class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            {{ loading ? '生成中...' : 'AI 生成' }}
          </button>
          <button
            v-if="streamController"
            @click="stopStreaming"
            class="px-3 py-2 bg-red-500 text-white text-sm rounded-lg hover:bg-red-600 transition-colors"
          >
            停止
          </button>
        </div>
      </div>

      <!-- AI 结果展示区域 -->
      <div v-if="aiResult || aiSuggestions.length > 0" class="mt-3 p-3 bg-purple-50 rounded-lg border border-purple-100">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium text-purple-700">AI 结果</span>
          <button @click="clearResult" class="text-xs text-gray-400 hover:text-gray-600">清除</button>
        </div>

        <!-- 文本结果（摘要、写作辅助） -->
        <div v-if="aiResult" class="text-sm text-gray-700 whitespace-pre-wrap mb-2">{{ aiResult }}</div>
        <button
          v-if="aiResult && resultType === 'writing'"
          @click="appendToContent"
          class="text-xs px-3 py-1 bg-purple-600 text-white rounded hover:bg-purple-700 transition-colors"
        >
          插入到文章末尾
        </button>
        <button
          v-if="aiResult && resultType === 'summary'"
          @click="copyResult"
          class="text-xs px-3 py-1 bg-purple-600 text-white rounded hover:bg-purple-700 transition-colors"
        >
          {{ copied ? '已复制' : '复制摘要' }}
        </button>

        <!-- 列表结果（标题、标签建议） -->
        <div v-if="aiSuggestions.length > 0" class="space-y-1">
          <button
            v-for="(item, index) in aiSuggestions"
            :key="index"
            @click="handleSuggestionClick(item)"
            class="block w-full text-left text-sm px-3 py-2 rounded-md hover:bg-purple-100 text-gray-700 transition-colors"
          >
            {{ item }}
          </button>
          <p class="text-xs text-purple-500 mt-1">
            {{ resultType === 'titles' ? '点击标题即可使用' : '点击标签即可添加' }}
          </p>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="mt-2 p-2 bg-red-50 border border-red-100 rounded-lg">
        <p class="text-sm text-red-600">{{ errorMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getAiStatus, generateSummary, suggestTitles, suggestTags, assistWriting } from '@/api/ai'

export default {
  name: 'AiAssistant',
  props: {
    title: {
      type: String,
      default: ''
    },
    content: {
      type: String,
      default: ''
    }
  },
  emits: ['update-title', 'update-content', 'add-tag'],
  setup(props, { emit }) {
    const aiAvailable = ref(false)
    const loading = ref(false)
    const aiPrompt = ref('')
    const aiResult = ref('')
    const aiSuggestions = ref([])
    const resultType = ref('')
    const errorMessage = ref('')
    const streamController = ref(null)
    const copied = ref(false)

    onMounted(async () => {
      try {
        const status = await getAiStatus()
        aiAvailable.value = status.available
      } catch {
        aiAvailable.value = false
      }
    })

    onBeforeUnmount(() => {
      if (streamController.value) {
        streamController.value.abort()
      }
    })

    const clearResult = () => {
      aiResult.value = ''
      aiSuggestions.value = []
      resultType.value = ''
      errorMessage.value = ''
    }

    const handleSuggestTitles = async () => {
      loading.value = true
      errorMessage.value = ''
      clearResult()
      try {
        const response = await suggestTitles(props.content)
        aiSuggestions.value = response.suggestions || []
        resultType.value = 'titles'
      } catch (err) {
        errorMessage.value = '标题推荐失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    const handleSuggestTags = async () => {
      loading.value = true
      errorMessage.value = ''
      clearResult()
      try {
        const response = await suggestTags(props.title, props.content)
        aiSuggestions.value = response.suggestions || []
        resultType.value = 'tags'
      } catch (err) {
        errorMessage.value = '标签推荐失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    const handleGenerateSummary = async () => {
      loading.value = true
      errorMessage.value = ''
      clearResult()
      try {
        const response = await generateSummary(props.content)
        aiResult.value = response.result || ''
        resultType.value = 'summary'
      } catch (err) {
        errorMessage.value = '摘要生成失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    const handleAssistWriting = () => {
      loading.value = true
      errorMessage.value = ''
      clearResult()
      resultType.value = 'writing'

      const controller = assistWriting(
        aiPrompt.value,
        props.content,
        (chunk) => {
          aiResult.value += chunk
        },
        () => {
          loading.value = false
          streamController.value = null
        },
        (err) => {  // eslint-disable-line no-unused-vars
          loading.value = false
          streamController.value = null
          errorMessage.value = '写作辅助失败，请稍后重试'
        }
      )
      streamController.value = controller
    }

    const stopStreaming = () => {
      if (streamController.value) {
        streamController.value.abort()
        streamController.value = null
        loading.value = false
      }
    }

    const handleSuggestionClick = (item) => {
      if (resultType.value === 'titles') {
        emit('update-title', item)
      } else if (resultType.value === 'tags') {
        emit('add-tag', item)
      }
    }

    const appendToContent = () => {
      if (aiResult.value) {
        const separator = props.content && !props.content.endsWith('\n') ? '\n\n' : ''
        emit('update-content', props.content + separator + aiResult.value)
      }
    }

    const copyResult = async () => {
      try {
        await navigator.clipboard.writeText(aiResult.value)
        copied.value = true
        setTimeout(() => { copied.value = false }, 2000)
      } catch {
        // fallback
        const textarea = document.createElement('textarea')
        textarea.value = aiResult.value
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        copied.value = true
        setTimeout(() => { copied.value = false }, 2000)
      }
    }

    return {
      aiAvailable,
      loading,
      aiPrompt,
      aiResult,
      aiSuggestions,
      resultType,
      errorMessage,
      streamController,
      copied,
      clearResult,
      handleSuggestTitles,
      handleSuggestTags,
      handleGenerateSummary,
      handleAssistWriting,
      stopStreaming,
      handleSuggestionClick,
      appendToContent,
      copyResult
    }
  }
}
</script>

<style scoped>
.ai-action-btn {
  @apply flex flex-col items-center justify-center px-2 py-2.5 rounded-lg border border-gray-200
  text-gray-600 hover:text-purple-600 hover:border-purple-300 hover:bg-purple-50
  disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200;
}
</style>
