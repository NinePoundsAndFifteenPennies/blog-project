<template>
  <div class="bg-white rounded-lg shadow-sm p-6">
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-bold text-gray-900">
        {{ sortTitle }} ({{ totalElements }})
      </h3>
      <div class="relative">
        <select
            v-model="selectedSort"
            @change="handleSortChange"
            class="appearance-none bg-white border border-gray-300 rounded-lg px-3 py-1.5 pr-8 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 cursor-pointer"
        >
          <option value="time_asc">按时间 (旧→新)</option>
          <option value="time_desc">按时间 (新→旧)</option>
          <option value="hotness_desc">按热度 (高→低)</option>
          <option value="hotness_asc">按热度 (低→高)</option>
        </select>
        <div class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </div>
    </div>

    <div v-if="isLoggedIn && !isDraft" class="mb-8">
      <div class="flex space-x-4">
        <div class="flex-shrink-0">
          <div
              class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600"
          >
            <img
                v-if="userAvatarUrl && !avatarLoadError"
                :src="userAvatarUrl"
                :alt="currentUser?.username"
                class="w-full h-full object-cover"
                @error="handleAvatarError"
                @load="handleAvatarLoad"
            />
            <span v-else>{{ userInitial }}</span>
          </div>
        </div>

        <div class="flex-1">
          <!-- Markdown Toolbar -->
          <div class="mb-2 flex flex-wrap gap-1 pb-2 border-b border-gray-100">
            <button @click="insertMarkdown('heading')" type="button" class="toolbar-btn" title="标题">
              <span class="font-bold text-sm">H</span>
            </button>
            <button @click="insertMarkdown('bold')" type="button" class="toolbar-btn" title="粗体">
              <span class="font-bold">B</span>
            </button>
            <button @click="insertMarkdown('italic')" type="button" class="toolbar-btn" title="斜体">
              <span class="italic">I</span>
            </button>
            <button @click="insertMarkdown('strikethrough')" type="button" class="toolbar-btn" title="删除线">
              <span class="line-through">S</span>
            </button>
            <button @click="insertMarkdown('code')" type="button" class="toolbar-btn" title="行内代码">
              <span class="font-mono text-xs">&lt;/&gt;</span>
            </button>

            <div class="w-px h-6 bg-gray-300 mx-1"></div>

            <button @click="insertMarkdown('ul')" type="button" class="toolbar-btn" title="无序列表">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <button @click="insertMarkdown('ol')" type="button" class="toolbar-btn" title="有序列表">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </button>
            <button @click="insertMarkdown('quote')" type="button" class="toolbar-btn" title="引用">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
              </svg>
            </button>

            <div class="w-px h-6 bg-gray-300 mx-1"></div>

            <button @click="insertMarkdown('link')" type="button" class="toolbar-btn" title="链接">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
              </svg>
            </button>
            <button @click="insertMarkdown('codeblock')" type="button" class="toolbar-btn" title="代码块">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
              </svg>
            </button>

            <div class="w-px h-6 bg-gray-300 mx-1"></div>

            <EmojiPicker @select="insertEmoji" title="表情" />
          </div>

          <textarea
              v-model="newComment"
              ref="commentTextarea"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
              rows="6"
              placeholder="支持 Markdown 语法，如 **加粗**、*斜体*、# 标题 等..."
          ></textarea>

          <div v-if="newComment.trim()" class="mt-2 p-3 border border-gray-200 rounded-lg bg-gray-50">
            <div class="text-xs text-gray-500 mb-2">预览:</div>
            <div class="markdown-body" v-html="previewContent"></div>
          </div>

          <div class="flex items-center justify-between mt-3">
            <span class="text-sm text-gray-500">
              {{ newComment.length }} / 3000
            </span>
            <button
                @click="submitComment"
                class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
                :disabled="!newComment.trim() || newComment.length > 3000 || submitting"
            >
              {{ submitting ? '发送中...' : '发表评论' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!isLoggedIn" class="mb-8 p-4 bg-gray-50 rounded-lg text-center">
      <p class="text-gray-600 mb-3">登录后才能发表评论</p>
      <router-link to="/login" class="btn-primary inline-block">
        立即登录
      </router-link>
    </div>

    <div v-else-if="isDraft" class="mb-8 p-4 bg-yellow-50 rounded-lg text-center">
      <p class="text-yellow-700">草稿文章不支持评论功能</p>
    </div>

    <div v-if="loading" class="space-y-4">
      <div v-for="i in 3" :key="i" class="animate-pulse flex space-x-4">
        <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-gray-200 rounded w-1/4"></div>
          <div class="h-4 bg-gray-200 rounded w-3/4"></div>
          <div class="h-4 bg-gray-200 rounded w-1/2"></div>
        </div>
      </div>
    </div>

    <div v-else-if="comments.length > 0" class="space-y-0">
      <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          :post-author-username="postAuthorUsername"
          :is-draft="isDraft"
          :auto-expand="expandCommentId && Number(expandCommentId) === comment.id"
          @comment-updated="handleCommentUpdated"
          @comment-deleted="handleCommentDeleted"
          @like-changed="handleLikeChanged"
      />
    </div>

    <div v-else class="text-center py-12">
      <svg class="w-16 h-16 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
      </svg>
      <p class="text-gray-500">暂无评论，快来发表第一条评论吧！</p>
    </div>

    <div v-if="hasMore || loadingMore" class="mt-8 text-center">
      <div v-if="loadingMore" class="flex flex-col items-center space-y-3 py-4">
        <div class="animate-spin rounded-full h-8 w-8 border-3 border-primary-600 border-t-transparent"></div>
        <p class="text-sm text-gray-500">正在加载更多评论...</p>
      </div>
      <button
          v-else
          @click="loadMore"
          class="btn-secondary inline-flex items-center space-x-2"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
        <span>加载更多评论</span>
      </button>
      <p class="text-sm text-gray-500 mt-2">
        已加载 {{ comments.length }} / {{ totalElements }} 条评论
      </p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { marked } from 'marked'
import CommentItem from './CommentItem.vue'
import EmojiPicker from './EmojiPicker.vue'
import { createComment, getPostComments } from '@/api/comments'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'CommentList',
  components: {
    CommentItem,
    EmojiPicker
  },
  props: {
    postId: {
      type: Number,
      required: true
    },
    postAuthorUsername: {
      type: String,
      default: ''
    },
    isDraft: {
      type: Boolean,
      default: false
    },
    expandCommentId: {
      type: [Number, String],
      default: null
    }
  },
  emits: ['comment-count-changed'],
  setup(props, { emit }) {
    const store = useStore()

    const loading = ref(false)
    const loadingMore = ref(false)
    const submitting = ref(false)
    const comments = ref([])
    const newComment = ref('')
    const commentTextarea = ref(null)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const PAGE_SIZE = 10
    const LOAD_MORE_PAGES = 2
    const avatarLoadError = ref(false)
    const loadedCount = ref(0)

    // 排序相关
    const selectedSort = ref('time_asc')

    const currentUser = computed(() => store.getters.currentUser)
    const isLoggedIn = computed(() => store.getters.isLoggedIn)

    // 动态标题
    const sortTitle = computed(() => {
      if (selectedSort.value.startsWith('hotness')) {
        return '热门评论'
      }
      return '评论'
    })

    // 解析排序参数
    const parseSortParams = () => {
      const parts = selectedSort.value.split('_')
      return {
        sortBy: parts[0], // 'time' or 'hotness'
        order: parts[1]   // 'asc' or 'desc'
      }
    }

    const userInitial = computed(() => {
      const name = currentUser.value?.username || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
    })

    const userAvatarUrl = computed(() => getFullAvatarUrl(currentUser.value?.avatarUrl))

    const previewContent = computed(() => {
      if (!newComment.value) return ''
      try {
        return marked(newComment.value)
      } catch (error) {
        console.error('Markdown preview error:', error)
        return newComment.value
      }
    })

    const hasMore = computed(() => {
      return comments.value.length < totalElements.value
    })

    const handleAvatarError = () => {
      avatarLoadError.value = true
    }

    const handleAvatarLoad = () => {
      avatarLoadError.value = false
    }

    const loadComments = async (append = false) => {
      if (append) {
        loadingMore.value = true
      } else {
        loading.value = true
      }

      const { sortBy, order } = parseSortParams()

      try {
        if (!append) {
          loadedCount.value = 0
          const response = await getPostComments(props.postId, {
            page: 0,
            size: PAGE_SIZE,
            sortBy: sortBy,
            order: order
          })

          comments.value = response.content || []
          loadedCount.value = comments.value.length
          totalPages.value = response.totalPages || 1
          totalElements.value = response.totalElements || 0
        } else {
          const startPage = Math.floor(loadedCount.value / PAGE_SIZE)
          const newComments = []

          for (let i = 0; i < LOAD_MORE_PAGES; i++) {
            const page = startPage + i
            if (page * PAGE_SIZE >= totalElements.value) break

            const response = await getPostComments(props.postId, {
              page: page,
              size: PAGE_SIZE,
              sortBy: sortBy,
              order: order
            })

            if (response.content && response.content.length > 0) {
              newComments.push(...response.content)
              totalPages.value = response.totalPages || 1
              totalElements.value = response.totalElements || 0
            }
          }

          comments.value = [...comments.value, ...newComments]
          loadedCount.value = comments.value.length
        }

        emit('comment-count-changed', totalElements.value)
      } catch (error) {
        console.error('加载评论失败:', error)
        if (!append) {
          comments.value = []
          loadedCount.value = 0
        }
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    const loadMore = async () => {
      if (loadingMore.value || !hasMore.value) return
      await loadComments(true)
    }

    const handleSortChange = () => {
      loadedCount.value = 0
      loadComments()
    }

    const submitComment = async () => {
      if (!newComment.value.trim() || newComment.value.length > 3000) return

      submitting.value = true
      try {
        await createComment(props.postId, newComment.value)
        newComment.value = ''
        // No need to reset commentMode

        loadedCount.value = 0
        await loadComments()
      } catch (error) {
        console.error('发表评论失败:', error)
        if (error.response?.status === 403) {
          alert('无法评论草稿文章')
        } else {
          alert('发表评论失败，请稍后重试')
        }
      } finally {
        submitting.value = false
      }
    }

    const handleCommentUpdated = (updatedComment) => {
      const index = comments.value.findIndex(c => c.id === updatedComment.id)
      if (index !== -1) {
        comments.value[index] = updatedComment
      }
    }

    const handleCommentDeleted = (commentId) => {
      comments.value = comments.value.filter(c => c.id !== commentId)
      totalElements.value = Math.max(0, totalElements.value - 1)
      loadedCount.value = comments.value.length
      emit('comment-count-changed', totalElements.value)

      if (comments.value.length === 0 && loadedCount.value > 0) {
        loadedCount.value = 0
        loadComments()
      }
    }

    const handleLikeChanged = ({ commentId, likeCount, liked }) => {
      const commentIndex = comments.value.findIndex(c => c.id === commentId)
      if (commentIndex !== -1) {
        const updatedComment = {
          ...comments.value[commentIndex],
          likeCount,
          liked
        }
        comments.value.splice(commentIndex, 1, updatedComment)
      }
    }

    const handleScroll = () => {
      if (loadingMore.value || !hasMore.value) return

      const scrollPosition = window.innerHeight + window.scrollY
      const documentHeight = document.documentElement.scrollHeight

      if (scrollPosition >= documentHeight - 200) {
        loadMore()
      }
    }

    const insertMarkdown = (type) => {
      const textarea = commentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const selectedText = newComment.value.substring(start, end)
      const beforeText = newComment.value.substring(0, start)
      const afterText = newComment.value.substring(end)

      let insertText = ''
      let cursorOffset = 0

      switch (type) {
        case 'heading':
          insertText = `## ${selectedText || '标题'}`
          cursorOffset = selectedText ? insertText.length : insertText.length - 2
          break
        case 'bold':
          insertText = `**${selectedText || '粗体文本'}**`
          cursorOffset = selectedText ? insertText.length : insertText.length - 2
          break
        case 'italic':
          insertText = `*${selectedText || '斜体文本'}*`
          cursorOffset = selectedText ? insertText.length : insertText.length - 1
          break
        case 'strikethrough':
          insertText = `~~${selectedText || '删除线文本'}~~`
          cursorOffset = selectedText ? insertText.length : insertText.length - 2
          break
        case 'code':
          insertText = `\`${selectedText || '代码'}\``
          cursorOffset = selectedText ? insertText.length : insertText.length - 1
          break
        case 'ul':
          insertText = selectedText ? `- ${selectedText}` : '- 列表项'
          cursorOffset = insertText.length
          break
        case 'ol':
          insertText = selectedText ? `1. ${selectedText}` : '1. 列表项'
          cursorOffset = insertText.length
          break
        case 'quote':
          insertText = selectedText ? `> ${selectedText}` : '> 引用内容'
          cursorOffset = insertText.length
          break
        case 'link':
          insertText = `[${selectedText || '链接文本'}](url)`
          cursorOffset = selectedText ? insertText.length - 4 : insertText.length - 5
          break
        case 'codeblock':
          insertText = selectedText ? `\`\`\`\n${selectedText}\n\`\`\`` : '```\n代码块\n```'
          cursorOffset = selectedText ? insertText.length - 4 : insertText.length - 5
          break
        default:
          return
      }

      newComment.value = beforeText + insertText + afterText

      setTimeout(() => {
        textarea.focus()
        textarea.setSelectionRange(start + cursorOffset, start + cursorOffset)
      }, 0)
    }

    // 插入表情
    const insertEmoji = (emoji) => {
      const textarea = commentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const beforeText = newComment.value.substring(0, start)
      const afterText = newComment.value.substring(end)

      newComment.value = beforeText + emoji + afterText

      // 等待下一个tick后设置光标位置
      setTimeout(() => {
        textarea.focus()
        const newPosition = start + emoji.length
        textarea.setSelectionRange(newPosition, newPosition)
      }, 0)
    }

    onMounted(() => {
      loadComments()
      window.addEventListener('scroll', handleScroll)
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
    })

    return {
      loading,
      loadingMore,
      submitting,
      comments,
      newComment,
      commentTextarea,
      totalPages,
      totalElements,
      hasMore,
      currentUser,
      isLoggedIn,
      userInitial,
      userAvatarUrl,
      avatarLoadError,
      previewContent,
      handleAvatarError,
      handleAvatarLoad,
      submitComment,
      handleCommentUpdated,
      handleCommentDeleted,
      handleLikeChanged,
      loadMore,
      selectedSort,
      sortTitle,
      handleSortChange,
      insertMarkdown,
      insertEmoji
    }
  }
}
</script>

<style scoped>
.toolbar-btn {
  @apply px-2 py-1.5 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100
  transition-colors duration-150 flex items-center justify-center min-w-[28px];
}

.toolbar-btn:active {
  @apply bg-gray-200;
}
</style>