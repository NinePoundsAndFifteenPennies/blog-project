<template>
  <div 
    class="py-4 border-b border-gray-200 last:border-b-0 transition-all"
    :style="{ paddingLeft: indentLevel + 'px' }"
  >
    <div class="flex space-x-3">
      <!-- Avatar -->
      <div class="flex-shrink-0">
        <div 
          class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-gradient-primary"
        >
          <img 
            v-if="displayAvatarUrl && !avatarLoadError" 
            :src="displayAvatarUrl" 
            :alt="reply.authorUsername"
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
        <div class="flex items-center justify-between mb-1">
          <div class="flex items-center space-x-2">
            <span class="font-medium text-gray-900 text-sm">{{ reply.authorUsername }}</span>
            <span v-if="reply.replyToUsername" class="text-gray-500 text-xs">
              回复 <span class="text-primary-600">@{{ reply.replyToUsername }}</span>
            </span>
            <span class="text-gray-400 text-xs">{{ formatDate(reply.updatedAt || reply.createdAt) }}</span>
            <span v-if="reply.updatedAt" class="text-gray-400 text-xs">(已编辑)</span>
          </div>
          
          <!-- Actions for reply author or post author -->
          <div v-if="canManage" class="flex items-center space-x-1">
            <button
              v-if="isReplyAuthor"
              @click="handleEdit"
              class="inline-flex items-center px-2 py-1 text-xs font-medium text-primary-700 bg-primary-50 rounded hover:bg-primary-100 transition-colors"
              title="编辑"
            >
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <button
              @click="handleDelete"
              class="inline-flex items-center px-2 py-1 text-xs font-medium text-red-700 bg-red-50 rounded hover:bg-red-100 transition-colors"
              title="删除"
            >
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Content Display -->
        <div>
          <!-- Show @mention if replying to a user -->
          <span v-if="reply.replyToUsername" class="text-primary-600 font-medium">
            @{{ reply.replyToUsername }}
          </span>
          <span> </span>
          <!-- Render based on content type -->
          <span v-if="isMarkdown" class="markdown-body text-gray-700 text-sm inline" v-html="renderedContent"></span>
          <span v-else class="text-gray-700 text-sm whitespace-pre-wrap">{{ reply.content }}</span>
        </div>

        <!-- Footer Actions -->
        <div class="flex items-center space-x-4 mt-2 text-xs">
          <!-- Like Button -->
          <button
            @click="handleLike"
            class="flex items-center space-x-1 transition-colors"
            :class="reply.liked ? 'text-red-500' : 'text-gray-400 hover:text-red-500'"
            :title="reply.liked ? '取消点赞' : '点赞'"
          >
            <svg 
              class="w-3.5 h-3.5" 
              :fill="reply.liked ? 'currentColor' : 'none'" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <span>{{ reply.likeCount || 0 }}</span>
          </button>
          
          <!-- Reply Button -->
          <button
            v-if="isLoggedIn && !isDraft"
            @click="handleReply"
            class="flex items-center space-x-1 text-gray-400 hover:text-primary-600 transition-colors"
          >
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
            </svg>
            <span>回复</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import { deleteComment, likeComment, unlikeComment } from '@/api/comments'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'ReplyItem',
  props: {
    reply: {
      type: Object,
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
  emits: ['reply-updated', 'reply-deleted', 'like-changed', 'reply-clicked'],
  setup(props, { emit }) {
    const store = useStore()
    const router = useRouter()
    const avatarLoadError = ref(false)

    const currentUser = computed(() => store.getters.currentUser)
    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    
    const isReplyAuthor = computed(() => {
      return currentUser.value?.username === props.reply.authorUsername
    })

    const isPostAuthor = computed(() => {
      return currentUser.value?.username === props.postAuthorUsername
    })

    const canManage = computed(() => {
      return isReplyAuthor.value || isPostAuthor.value
    })
    
    // Check if the reply author is the current user
    const isCurrentUser = computed(() => {
      return currentUser.value?.username === props.reply.authorUsername
    })
    
    // Use current user's avatar if author is current user, otherwise use reply author's avatar
    const displayAvatarUrl = computed(() => {
      if (isCurrentUser.value && currentUser.value?.avatarUrl) {
        return getFullAvatarUrl(currentUser.value.avatarUrl)
      }
      return props.reply.authorAvatarUrl
    })
    
    // Calculate indentation based on level (level 1: 20px, level 2: 40px, level 3: 60px)
    const indentLevel = computed(() => {
      const level = props.reply.level || 0
      return Math.min(level, 3) * 20
    })
    
    // Reset avatar error when avatar URL changes
    watch(displayAvatarUrl, () => {
      avatarLoadError.value = false
    })

    const authorInitial = computed(() => {
      const name = props.reply.authorUsername || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
    })

    // Check if content looks like markdown
    const isMarkdown = computed(() => {
      const content = props.reply.content || ''
      // Simple heuristic: check for common markdown patterns
      return /[#*`[\]_~]/.test(content)
    })

    const renderedContent = computed(() => {
      if (!props.reply.content) return ''
      try {
        return marked(props.reply.content)
      } catch (error) {
        console.error('Markdown rendering error:', error)
        return props.reply.content
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

    const handleEdit = () => {
      // Navigate to comment edit page with reply data
      router.push({
        name: 'CommentEdit',
        params: { id: props.reply.id },
        state: {
          comment: {
            id: props.reply.id,
            content: props.reply.content,
            postId: props.reply.postId
          }
        }
      })
    }

    const handleDelete = async () => {
      if (!confirm('确定要删除这条回复吗？')) return

      try {
        await deleteComment(props.reply.id)
        emit('reply-deleted', props.reply.id)
      } catch (error) {
        console.error('删除回复失败:', error)
        alert('删除回复失败，请稍后重试')
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
        if (props.reply.liked) {
          response = await unlikeComment(props.reply.id)
        } else {
          response = await likeComment(props.reply.id)
        }

        emit('like-changed', {
          replyId: props.reply.id,
          likeCount: response.likeCount,
          liked: response.liked
        })
      } catch (error) {
        console.error('点赞操作失败:', error)
      }
    }

    const handleReply = () => {
      // When replying to a reply, pass the reply's ID so it can be used as the parent
      emit('reply-clicked', {
        replyId: props.reply.id,
        replyToUserId: null,
        replyToUsername: props.reply.authorUsername
      })
    }

    return {
      avatarLoadError,
      displayAvatarUrl,
      isReplyAuthor,
      isPostAuthor,
      canManage,
      isLoggedIn,
      authorInitial,
      isMarkdown,
      renderedContent,
      indentLevel,
      handleAvatarError,
      handleAvatarLoad,
      formatDate,
      handleEdit,
      handleDelete,
      handleLike,
      handleReply
    }
  }
}
</script>

<style scoped>
.markdown-body {
  display: inline;
}

.markdown-body p {
  display: inline;
  margin: 0;
}

.markdown-body p:first-child {
  margin-top: 0;
}

.markdown-body p:last-child {
  margin-bottom: 0;
}
</style>
