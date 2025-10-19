<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Page Header -->
          <div class="mb-8">
            <h1 class="text-3xl font-bold text-gray-900 mb-2">我的评论</h1>
            <p class="text-gray-600">查看和管理你发表的所有评论</p>
          </div>

          <!-- Stats -->
          <div v-if="totalElements > 0" class="mb-6">
            <span class="inline-flex items-center px-4 py-2 bg-white rounded-full shadow-sm border border-gray-100">
              <span class="text-gray-600">共有</span>
              <span class="mx-2 font-semibold text-primary-600">{{ totalElements }}</span>
              <span class="text-gray-600">条评论</span>
            </span>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="bg-white rounded-lg shadow-sm p-6">
            <div v-for="i in 5" :key="i" class="animate-pulse flex space-x-4 mb-6 last:mb-0">
              <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-200 rounded w-1/4"></div>
                <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                <div class="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          </div>

          <!-- Comments List -->
          <div v-else-if="comments.length > 0" class="bg-white rounded-lg shadow-sm p-6">
            <CommentItem
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              :post-author-username="''"
              :show-post-link="true"
              @comment-updated="handleCommentUpdated"
              @comment-deleted="handleCommentDeleted"
              @like-changed="handleLikeChanged"
            />
          </div>

          <!-- Empty State -->
          <div v-else class="bg-white rounded-lg shadow-sm p-12 text-center">
            <svg class="w-16 h-16 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
            </svg>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">还没有评论</h3>
            <p class="text-gray-600 mb-6">去文章下面发表你的第一条评论吧！</p>
            <router-link to="/" class="btn-primary inline-block">
              浏览文章
            </router-link>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-8">
            <Pagination
              :current-page="currentPage"
              :total-pages="totalPages"
              @page-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import CommentItem from '@/components/CommentItem.vue'
import Pagination from '@/components/Pagination.vue'
import { getMyComments } from '@/api/comments'

export default {
  name: 'MyComments',
  components: {
    Header,
    CommentItem,
    Pagination
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const comments = ref([])
    // Initialize page from URL query parameter
    const currentPage = ref(parseInt(route.query.page) || 1)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const pageSize = 10

    const loadComments = async () => {
      loading.value = true
      try {
        const response = await getMyComments({
          page: currentPage.value - 1,
          size: pageSize
        })

        comments.value = response.content || []
        totalPages.value = response.totalPages || 1
        totalElements.value = response.totalElements || 0
      } catch (error) {
        console.error('加载我的评论失败:', error)
        comments.value = []
      } finally {
        loading.value = false
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
      
      // Reload if current page is empty and not the first page
      if (comments.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        loadComments()
      }
    }

    const handleLikeChanged = ({ commentId, likeCount, liked }) => {
      const commentIndex = comments.value.findIndex(c => c.id === commentId)
      if (commentIndex !== -1) {
        // Update using splice to ensure Vue reactivity
        const updatedComment = {
          ...comments.value[commentIndex],
          likeCount,
          liked
        }
        comments.value.splice(commentIndex, 1, updatedComment)
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      // Update URL with page parameter
      router.push({ query: { ...route.query, page } })
      loadComments()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // Watch for route query changes (e.g., browser back/forward)
    watch(() => route.query.page, (newPage) => {
      const page = parseInt(newPage) || 1
      if (page !== currentPage.value) {
        currentPage.value = page
        loadComments()
      }
    })

    onMounted(() => {
      loadComments()
    })

    return {
      loading,
      comments,
      currentPage,
      totalPages,
      totalElements,
      handleCommentUpdated,
      handleCommentDeleted,
      handleLikeChanged,
      handlePageChange
    }
  }
}
</script>
