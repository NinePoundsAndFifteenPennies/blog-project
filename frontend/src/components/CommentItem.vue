<template>
  <div :id="`comment-${comment.id}`" class="border-b border-gray-100 py-6 last:border-b-0">
    <div class="flex space-x-4">
      <div class="flex-shrink-0">
        <UserProfileHoverCard
            v-if="comment.authorUsername"
            :username="comment.authorUsername"
            :user-data="authorData"
        >
          <div
              class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600"
          >
            <img
                v-if="displayAvatarUrl && !avatarLoadError"
                :src="displayAvatarUrl"
                :alt="authorDisplayName"
                :key="displayAvatarUrl"
                class="w-full h-full object-cover"
                @error="handleAvatarError"
                @load="handleAvatarLoad"
            />
            <span v-else>{{ authorInitial }}</span>
          </div>
        </UserProfileHoverCard>
        <div
            v-else
            class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600"
        >
          <span>{{ authorInitial }}</span>
        </div>
      </div>

      <div class="flex-1 min-w-0">
        <div class="flex items-center justify-between mb-2">
          <div>
            <span class="font-semibold text-gray-900">{{ authorDisplayName }}</span>
            <span class="text-gray-400 text-sm ml-2">{{ formatDate(comment.updatedAt || comment.createdAt) }}</span>
            <span v-if="comment.updatedAt" class="text-gray-400 text-xs ml-2">(已编辑)</span>
          </div>

          <div v-if="canManage" class="flex items-center space-x-2">
            <button
                v-if="isCommentAuthor"
                @click="handleEdit"
                class="inline-flex items-center px-3 py-1.5 text-sm font-medium text-primary-700 bg-primary-50 rounded-lg hover:bg-primary-100 transition-colors"
                title="编辑评论"
            >
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              编辑
            </button>
            <button
                @click="handleDelete"
                class="inline-flex items-center px-3 py-1.5 text-sm font-medium text-red-700 bg-red-50 rounded-lg hover:bg-red-100 transition-colors"
                title="删除评论"
            >
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              删除
            </button>
          </div>
        </div>

        <div>
          <div class="markdown-body text-gray-700 mb-3" v-html="renderedContent"></div>
        </div>

        <div class="flex items-center space-x-4 text-sm">
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

          <button
              v-if="!showPostLink && isLoggedIn && !isDraft && comment.level === 0"
              @click="navigateToReply"
              class="flex items-center space-x-1 text-gray-400 hover:text-primary-600 transition-colors"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
            </svg>
            <span>回复</span>
          </button>

          <button
              v-if="!showPostLink && comment.level === 0 && (comment.replyCount > 0 || localReplyCount > 0)"
              @click="toggleRepliesVisibility"
              class="flex items-center space-x-1 text-primary-600 hover:text-primary-700 font-medium transition-colors"
          >
            <svg
                class="w-4 h-4 transition-transform"
                :class="{ 'rotate-180': showReplies }"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
            <span>{{ showReplies ? '收起回复' : `查看 ${displayReplyCount} 条回复` }}</span>
          </button>

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

        <ReplyList
            v-if="showReplies && !showPostLink"
            ref="replyListRef"
            :comment-id="comment.id"
            :post-author-username="postAuthorUsername"
            :is-draft="isDraft"
            :visible="showReplies"
            @reply-count-changed="handleReplyCountChanged"
            @reply-clicked="handleReplyToReply"
        />
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
import ReplyList from './ReplyList.vue'
import UserProfileHoverCard from './UserProfileHoverCard.vue'

export default {
  name: 'CommentItem',
  components: {
    ReplyList,
    UserProfileHoverCard
  },
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
    },
    isDraft: {
      type: Boolean,
      default: false
    }
  },
  emits: ['comment-updated', 'comment-deleted', 'like-changed'],
  setup(props, { emit }) {
    const store = useStore()
    const router = useRouter()
    const avatarLoadError = ref(false)
    const showReplies = ref(false)
    const replyListRef = ref(null)
    const localReplyCount = ref(props.comment.replyCount || 0)

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

    const isCurrentUser = computed(() => {
      return currentUser.value?.username === props.comment.authorUsername
    })

    const displayAvatarUrl = computed(() => {
      if (isCurrentUser.value && currentUser.value?.avatarUrl) {
        return getFullAvatarUrl(currentUser.value.avatarUrl)
      }
      return props.comment.authorAvatarUrl
    })

    const displayReplyCount = computed(() => {
      return localReplyCount.value || props.comment.replyCount || 0
    })

    watch(displayAvatarUrl, () => {
      avatarLoadError.value = false
    })

    const authorInitial = computed(() => {
      const name = props.comment.authorNickname || props.comment.authorUsername || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
    })

    const authorDisplayName = computed(() => {
      return props.comment.authorNickname || props.comment.authorUsername || '匿名用户'
    })

    const authorData = computed(() => {
      return {
        username: props.comment.authorUsername,
        nickname: props.comment.authorNickname,
        avatarUrl: displayAvatarUrl.value
      }
    })

    // [修改点3]：删除 isMarkdown 计算属性，我们现在总是渲染 Markdown
    // const isMarkdown = computed(() => { ... })

    const renderedContent = computed(() => {
      if (!props.comment.content) return ''
      try {
        return marked(props.comment.content)
      } catch (error) {
        console.error('Markdown rendering error:', error)
        return props.comment.content
      }
    })

    // ... (后续方法保持不变: handleAvatarError, formatDate, handleEdit, handleDelete, handleLike, toggleReplies 等) ...

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
      router.push({
        name: 'CommentEdit',
        params: { id: props.comment.id },
        state: {
          comment: {
            id: props.comment.id,
            content: props.comment.content,
            postId: props.comment.postId
          }
        }
      })
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

    const toggleRepliesVisibility = () => {
      showReplies.value = !showReplies.value
    }

    // Navigate to the ReplyCreate page
    const navigateToReply = () => {
      router.push({
        name: 'ReplyCreate',
        params: { id: props.comment.id },
        state: {
          replyData: {
            commentId: props.comment.id,
            postId: props.comment.postId,
            replyToUserId: null,
            replyToUsername: props.comment.authorUsername
          }
        }
      })
    }

    const handleReplyCountChanged = (count) => {
      localReplyCount.value = count
    }

    // Handle reply to reply - navigate to ReplyCreate page with the reply info
    const handleReplyToReply = ({ replyId, replyToUserId: userId, replyToUsername: username }) => {
      router.push({
        name: 'ReplyCreate',
        params: { id: replyId },
        state: {
          replyData: {
            commentId: replyId,
            postId: props.comment.postId,
            replyToUserId: userId,
            replyToUsername: username
          }
        }
      })
    }

    return {
      avatarLoadError,
      displayAvatarUrl,
      isCommentAuthor,
      isPostAuthor,
      canManage,
      isLoggedIn,
      authorInitial,
      authorDisplayName,
      authorData,
      renderedContent,
      showReplies,
      replyListRef,
      localReplyCount,
      displayReplyCount,
      handleAvatarError,
      handleAvatarLoad,
      formatDate,
      handleEdit,
      handleDelete,
      handleLike,
      navigateToReply,
      toggleRepliesVisibility,
      handleReplyCountChanged,
      handleReplyToReply
    }
  }
}
</script>