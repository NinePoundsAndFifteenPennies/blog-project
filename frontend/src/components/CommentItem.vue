<template>
  <div class="border-b border-gray-100 py-6 last:border-b-0">
    <div class="flex space-x-4">
      <!-- Avatar -->
      <div class="flex-shrink-0">
        <div 
          class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-gradient-primary"
        >
          <img 
            v-if="displayAvatarUrl && !avatarLoadError" 
            :src="displayAvatarUrl" 
            :alt="comment.authorUsername"
            :key="displayAvatarUrl"
            class="w-full h-full object-cover"
            @error="handleAvatarError"
            @load="handleAvatarLoad"
          />
          <span v-else>{{ authorInitial }}</span>
        </div>
      </div>

      <!-- Content -->
      <div class="flex-1 min-w-0">
        <!-- Header -->
        <div class="flex items-center justify-between mb-2">
          <div>
            <span class="font-semibold text-gray-900">{{ comment.authorUsername }}</span>
            <span class="text-gray-400 text-sm ml-2">{{ formatDate(comment.createdAt) }}</span>
            <span v-if="comment.updatedAt" class="text-gray-400 text-xs ml-2">(已编辑)</span>
          </div>
          
          <!-- Actions for comment author or post author -->
          <div v-if="canManage" class="flex items-center space-x-2">
            <button
              v-if="isCommentAuthor && !isEditing"
              @click="startEdit"
              class="text-sm text-primary-600 hover:text-primary-700"
              title="编辑评论"
            >
              编辑
            </button>
            <button
              @click="handleDelete"
              class="text-sm text-red-600 hover:text-red-700"
              title="删除评论"
            >
              删除
            </button>
          </div>
        </div>

        <!-- Edit Mode -->
        <div v-if="isEditing" class="mb-3">
          <textarea
            v-model="editContent"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
            rows="4"
            placeholder="输入评论内容..."
          ></textarea>
          <div class="flex items-center justify-end space-x-2 mt-2">
            <button
              @click="cancelEdit"
              class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800"
            >
              取消
            </button>
            <button
              @click="saveEdit"
              class="px-4 py-2 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700"
              :disabled="!editContent.trim() || saving"
            >
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>

        <!-- Content Display -->
        <div v-else>
          <!-- Render based on content type -->
          <div v-if="isMarkdown" class="prose prose-sm max-w-none text-gray-700 mb-3" v-html="renderedContent"></div>
          <div v-else class="text-gray-700 mb-3 whitespace-pre-wrap">{{ comment.content }}</div>
        </div>

        <!-- Footer Actions -->
        <div class="flex items-center space-x-4 text-sm">
          <!-- Like Button -->
          <button
            @click="handleLike"
            class="flex items-center space-x-1 transition-colors"
            :class="comment.liked ? 'text-red-500' : 'text-gray-400 hover:text-red-500'"
            :title="comment.liked ? '取消点赞' : '点赞'"
          >
            <svg 
              class="w-4 h-4" 
              :fill="comment.liked ? 'currentColor' : 'none'" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <span>{{ comment.likeCount || 0 }}</span>
          </button>
          
          <!-- Show post title if this is from "My Comments" view -->
          <router-link
            v-if="comment.postId && comment.postTitle && showPostLink"
            :to="`/post/${comment.postId}`"
            class="text-gray-500 hover:text-primary-600 flex items-center space-x-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span class="truncate max-w-xs">{{ comment.postTitle }}</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue' // 1. 导入 watch
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import { updateComment, deleteComment, likeComment, unlikeComment } from '@/api/comments'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'CommentItem',
  props: {
    comment: {
      type: Object,
      required: true
    },
    postAuthorUsername: {
      type: String,
      default: ''
    },
    showPostLink: {
      type: Boolean,
      default: false
    }
  },
  emits: ['comment-updated', 'comment-deleted', 'like-changed'],
  setup(props, { emit }) {
    const store = useStore()
    const router = useRouter()
    const avatarLoadError = ref(false)
    const isEditing = ref(false)
    const editContent = ref('')
    const saving = ref(false)

    const currentUser = computed(() => store.getters.currentUser)
    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    
    const isCommentAuthor = computed(() => {
      return currentUser.value?.username === props.comment.authorUsername
    })

    const isPostAuthor = computed(() => {
      return currentUser.value?.username === props.postAuthorUsername
    })

    const canManage = computed(() => {
      return isCommentAuthor.value || isPostAuthor.value
    })
    
    // Check if the comment author is the current user
    const isCurrentUser = computed(() => {
      return currentUser.value?.username === props.comment.authorUsername
    })
    
    // Use current user's avatar if author is current user, otherwise use comment author's avatar
    const displayAvatarUrl = computed(() => {
      if (isCurrentUser.value && currentUser.value?.avatarUrl) {
        return getFullAvatarUrl(currentUser.value.avatarUrl)
      }
      return props.comment.authorAvatarUrl
    })
    
    // Reset avatar error when avatar URL changes
    watch(displayAvatarUrl, () => {
      avatarLoadError.value = false
    })

    const authorInitial = computed(() => {
      const name = props.comment.authorUsername || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
    })

    // Check if content looks like markdown
    const isMarkdown = computed(() => {
      const content = props.comment.content || ''
      // Simple heuristic: check for common markdown patterns
      return /[#*`[\]_~]/.test(content)
    })

    const renderedContent = computed(() => {
      if (!props.comment.content) return ''
      try {
        return marked(props.comment.content)
      } catch (error) {
        console.error('Markdown rendering error:', error)
        return props.comment.content
      }
    })

    const handleAvatarError = () => {
      avatarLoadError.value = true
    }

    const handleAvatarLoad = () => {
      avatarLoadError.value = false
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / (1000 * 60))
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      if (days < 30) return `${Math.floor(days / 7)}周前`
      if (days < 365) return `${Math.floor(days / 30)}个月前`

      return date.toLocaleDateString('zh-CN')
    }

    const startEdit = () => {
      editContent.value = props.comment.content
      isEditing.value = true
    }

    const cancelEdit = () => {
      isEditing.value = false
      editContent.value = ''
    }

    const saveEdit = async () => {
      if (!editContent.value.trim()) return

      saving.value = true
      try {
        const response = await updateComment(props.comment.id, editContent.value)
        emit('comment-updated', response)
        isEditing.value = false
      } catch (error) {
        console.error('更新评论失败:', error)
        alert('更新评论失败，请稍后重试')
      } finally {
        saving.value = false
      }
    }

    const handleDelete = async () => {
      if (!confirm('确定要删除这条评论吗？')) return

      try {
        await deleteComment(props.comment.id)
        emit('comment-deleted', props.comment.id)
      } catch (error) {
        console.error('删除评论失败:', error)
        alert('删除评论失败，请稍后重试')
      }
    }

    const handleLike = async () => {
      if (!isLoggedIn.value) {
        router.push({
          path: '/login',
          query: {
            redirect: router.currentRoute.value.fullPath,
            message: '请先登录后再点赞'
          }
        })
        return
      }

      try {
        let response
        if (props.comment.liked) {
          response = await unlikeComment(props.comment.id)
        } else {
          response = await likeComment(props.comment.id)
        }

        emit('like-changed', {
          commentId: props.comment.id,
          likeCount: response.likeCount,
          liked: response.liked
        })
      } catch (error) {
        console.error('点赞操作失败:', error)
      }
    }

    return {
      avatarLoadError,
      displayAvatarUrl,
      isEditing,
      editContent,
      saving,
      isCommentAuthor,
      isPostAuthor,
      canManage,
      authorInitial,
      isMarkdown,
      renderedContent,
      handleAvatarError,
      handleAvatarLoad,
      formatDate,
      startEdit,
      cancelEdit,
      saveEdit,
      handleDelete,
      handleLike
    }
  }
}
</script>
