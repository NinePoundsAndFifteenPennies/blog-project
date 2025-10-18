<template>
  <div class="bg-white rounded-lg shadow-sm p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-bold text-gray-900">
        评论 ({{ totalElements }})
      </h3>
    </div>

    <!-- Comment Form (only for logged-in users and non-draft posts) -->
    <div v-if="isLoggedIn && !isDraft" class="mb-8">
      <div class="flex space-x-4">
        <div class="flex-shrink-0">
          <div 
            class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-gradient-primary"
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
          <!-- Mode selector -->
          <div class="flex items-center space-x-2 mb-2">
            <button
              @click="commentMode = 'text'"
              class="px-3 py-1 text-sm rounded-md transition-colors"
              :class="commentMode === 'text' ? 'bg-primary-100 text-primary-700' : 'text-gray-600 hover:bg-gray-100'"
            >
              纯文本
            </button>
            <button
              @click="commentMode = 'markdown'"
              class="px-3 py-1 text-sm rounded-md transition-colors"
              :class="commentMode === 'markdown' ? 'bg-primary-100 text-primary-700' : 'text-gray-600 hover:bg-gray-100'"
            >
              Markdown
            </button>
          </div>

          <!-- Comment input -->
          <textarea
            v-model="newComment"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
            :rows="commentMode === 'markdown' ? 6 : 4"
            :placeholder="commentMode === 'markdown' ? '支持 Markdown 语法，如 **加粗**、*斜体*、# 标题 等...' : '写下你的评论...'"
          ></textarea>

          <!-- Preview (for Markdown mode) -->
          <div v-if="commentMode === 'markdown' && newComment.trim()" class="mt-2 p-3 border border-gray-200 rounded-lg bg-gray-50">
            <div class="text-xs text-gray-500 mb-2">预览:</div>
            <div class="prose prose-sm max-w-none" v-html="previewContent"></div>
          </div>

          <!-- Submit button -->
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

    <!-- Login prompt for non-logged-in users -->
    <div v-else-if="!isLoggedIn" class="mb-8 p-4 bg-gray-50 rounded-lg text-center">
      <p class="text-gray-600 mb-3">登录后才能发表评论</p>
      <router-link to="/login" class="btn-primary inline-block">
        立即登录
      </router-link>
    </div>

    <!-- Draft notice -->
    <div v-else-if="isDraft" class="mb-8 p-4 bg-yellow-50 rounded-lg text-center">
      <p class="text-yellow-700">草稿文章不支持评论功能</p>
    </div>

    <!-- Loading State -->
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

    <!-- Comments List -->
    <div v-else-if="comments.length > 0" class="space-y-0">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        :post-author-username="postAuthorUsername"
        @comment-updated="handleCommentUpdated"
        @comment-deleted="handleCommentDeleted"
        @like-changed="handleLikeChanged"
      />
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12">
      <svg class="w-16 h-16 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
      </svg>
      <p class="text-gray-500">暂无评论，快来发表第一条评论吧！</p>
    </div>

    <!-- Load More Button and Loading Indicator (Dynamic Loading) -->
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
import { createComment, getPostComments } from '@/api/comments'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'CommentList',
  components: {
    CommentItem
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
    const commentMode = ref('text') // 'text' or 'markdown'
    const currentPage = ref(1)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const initialPageSize = 10 // First load: 10 comments
    const pageSize = 20 // Subsequent loads: 20 comments
    const avatarLoadError = ref(false)

    const currentUser = computed(() => store.getters.currentUser)
    const isLoggedIn = computed(() => store.getters.isLoggedIn)

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
      
      try {
        // Use initialPageSize for first load, pageSize for subsequent loads
        const size = (currentPage.value === 1 && !append) ? initialPageSize : pageSize
        
        const response = await getPostComments(props.postId, {
          page: currentPage.value - 1,
          size: size
        })

        if (append) {
          // Append to existing comments for "load more"
          comments.value = [...comments.value, ...(response.content || [])]
        } else {
          // Replace comments for initial load
          comments.value = response.content || []
        }
        
        totalPages.value = response.totalPages || 1
        totalElements.value = response.totalElements || 0

        emit('comment-count-changed', totalElements.value)
      } catch (error) {
        console.error('加载评论失败:', error)
        if (!append) {
          comments.value = []
        }
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    const loadMore = async () => {
      if (loadingMore.value || !hasMore.value) return
      currentPage.value++
      await loadComments(true)
    }

    const submitComment = async () => {
      if (!newComment.value.trim() || newComment.value.length > 3000) return

      submitting.value = true
      try {
        await createComment(props.postId, newComment.value)
        newComment.value = ''
        commentMode.value = 'text'
        
        // Reload comments (go to first page)
        currentPage.value = 1
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
      emit('comment-count-changed', totalElements.value)
      
      // Reload if current page is empty and not the first page
      if (comments.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        loadComments()
      }
    }

    const handleLikeChanged = ({ commentId, likeCount, isLiked }) => {
      const commentIndex = comments.value.findIndex(c => c.id === commentId)
      if (commentIndex !== -1) {
        // Create new object to ensure reactivity
        comments.value[commentIndex] = {
          ...comments.value[commentIndex],
          likeCount,
          isLiked
        }
      }
    }

    // Infinite scroll handler
    const handleScroll = () => {
      if (loadingMore.value || !hasMore.value) return

      const scrollPosition = window.innerHeight + window.scrollY
      const documentHeight = document.documentElement.scrollHeight
      
      // Trigger load more when within 200px of bottom
      if (scrollPosition >= documentHeight - 200) {
        loadMore()
      }
    }

    onMounted(() => {
      loadComments()
      // Add scroll listener for infinite scroll
      window.addEventListener('scroll', handleScroll)
    })

    onUnmounted(() => {
      // Clean up scroll listener
      window.removeEventListener('scroll', handleScroll)
    })

    return {
      loading,
      loadingMore,
      submitting,
      comments,
      newComment,
      commentMode,
      currentPage,
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
      loadMore
    }
  }
}
</script>
