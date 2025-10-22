<template>
  <div class="mt-3">
    <!-- Loading State -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 2" :key="i" class="animate-pulse flex space-x-3 pl-5">
        <div class="w-8 h-8 bg-gray-200 rounded-full"></div>
        <div class="flex-1 space-y-2">
          <div class="h-3 bg-gray-200 rounded w-1/4"></div>
          <div class="h-3 bg-gray-200 rounded w-3/4"></div>
        </div>
      </div>
    </div>

    <!-- Replies List -->
    <div v-else-if="replies.length > 0" class="bg-gray-50 rounded-lg px-3 py-2">
      <ReplyItem
        v-for="reply in replies"
        :key="reply.id"
        :reply="reply"
        :post-author-username="postAuthorUsername"
        :is-draft="isDraft"
        @reply-updated="handleReplyUpdated"
        @reply-deleted="handleReplyDeleted"
        @like-changed="handleLikeChanged"
        @reply-clicked="handleReplyClicked"
      />
    </div>

    <!-- Load More Button -->
    <div v-if="hasMore || loadingMore" class="mt-4 text-center">
      <div v-if="loadingMore" class="flex flex-col items-center space-y-2 py-2">
        <div class="animate-spin rounded-full h-6 w-6 border-2 border-primary-600 border-t-transparent"></div>
        <p class="text-xs text-gray-500">正在加载更多回复...</p>
      </div>
      <button
        v-else
        @click="loadMore"
        class="text-sm text-primary-600 hover:text-primary-700 font-medium"
      >
        加载更多回复 ({{ replies.length }} / {{ totalElements }})
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import ReplyItem from './ReplyItem.vue'
import { getCommentReplies } from '@/api/comments'

export default {
  name: 'ReplyList',
  components: {
    ReplyItem
  },
  props: {
    commentId: {
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
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['reply-count-changed', 'reply-clicked'],
  setup(props, { emit }) {
    const loading = ref(false)
    const loadingMore = ref(false)
    const replies = ref([])
    const totalPages = ref(1)
    const totalElements = ref(0)
    const currentPage = ref(0)
    const PAGE_SIZE = 20

    const hasMore = computed(() => {
      return replies.value.length < totalElements.value
    })

    const loadReplies = async (append = false) => {
      if (append) {
        loadingMore.value = true
      } else {
        loading.value = true
        currentPage.value = 0
      }
      
      try {
        const response = await getCommentReplies(props.commentId, {
          page: currentPage.value,
          size: PAGE_SIZE
        })

        if (append) {
          replies.value = [...replies.value, ...(response.content || [])]
        } else {
          replies.value = response.content || []
        }
        
        totalPages.value = response.totalPages || 1
        totalElements.value = response.totalElements || 0
        currentPage.value++

        emit('reply-count-changed', totalElements.value)
      } catch (error) {
        console.error('加载回复失败:', error)
        if (!append) {
          replies.value = []
        }
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    const loadMore = async () => {
      if (loadingMore.value || !hasMore.value) return
      await loadReplies(true)
    }

    const handleReplyUpdated = (updatedReply) => {
      const index = replies.value.findIndex(r => r.id === updatedReply.id)
      if (index !== -1) {
        replies.value[index] = updatedReply
      }
    }

    const handleReplyDeleted = (replyId) => {
      replies.value = replies.value.filter(r => r.id !== replyId)
      totalElements.value = Math.max(0, totalElements.value - 1)
      emit('reply-count-changed', totalElements.value)
      
      // Reload if current page is empty and not the first page
      if (replies.value.length === 0 && currentPage.value > 0) {
        currentPage.value = 0
        loadReplies()
      }
    }

    const handleLikeChanged = ({ replyId, likeCount, liked }) => {
      const replyIndex = replies.value.findIndex(r => r.id === replyId)
      if (replyIndex !== -1) {
        const updatedReply = {
          ...replies.value[replyIndex],
          likeCount,
          liked
        }
        replies.value.splice(replyIndex, 1, updatedReply)
      }
    }

    const handleReplyClicked = (data) => {
      emit('reply-clicked', data)
    }

    // Watch visibility to load replies when shown
    watch(() => props.visible, (newVal) => {
      if (newVal && replies.value.length === 0) {
        loadReplies()
      }
    }, { immediate: true })

    // Expose reload method for parent component
    const reload = () => {
      currentPage.value = 0
      loadReplies()
    }

    return {
      loading,
      loadingMore,
      replies,
      totalPages,
      totalElements,
      hasMore,
      loadMore,
      handleReplyUpdated,
      handleReplyDeleted,
      handleLikeChanged,
      handleReplyClicked,
      reload
    }
  }
}
</script>
