<template>
  <div class="min-h-screen">
    <Header />

    <!-- Enhanced Hero Section with Animated Background -->
    <section class="relative pt-24 pb-20 overflow-hidden">
      <!-- Animated background elements -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute top-20 left-10 w-72 h-72 bg-primary-300 rounded-full mix-blend-multiply filter blur-xl opacity-30 animate-float"></div>
        <div class="absolute top-40 right-10 w-72 h-72 bg-purple-300 rounded-full mix-blend-multiply filter blur-xl opacity-30 animate-float" style="animation-delay: 2s;"></div>
        <div class="absolute -bottom-8 left-1/2 w-72 h-72 bg-pink-300 rounded-full mix-blend-multiply filter blur-xl opacity-30 animate-float" style="animation-delay: 4s;"></div>
      </div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-4xl mx-auto text-center">
          <h1 class="text-5xl md:text-7xl font-bold mb-6 bg-gradient-primary bg-clip-text text-transparent animate-fade-in leading-tight">
            分享你的技术见解
          </h1>
          <p class="text-xl md:text-2xl text-gray-600 mb-8 animate-slide-up leading-relaxed">
            在这里记录学习、分享经验、交流技术 ✨
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center animate-slide-up" style="animation-delay: 0.2s;">
            <router-link
                v-if="!isLoggedIn"
                to="/register"
                class="btn-primary inline-flex items-center justify-center space-x-2 px-8 py-4 text-lg"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              <span>立即开始写作</span>
            </router-link>
            <router-link
                v-else
                to="/post/create"
                class="btn-primary inline-flex items-center justify-center space-x-2 px-8 py-4 text-lg"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              <span>创建新文章</span>
            </router-link>
            <router-link
                to="/register"
                v-if="!isLoggedIn"
                class="btn-secondary inline-flex items-center justify-center space-x-2 px-8 py-4 text-lg"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>了解更多</span>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Posts Section -->
    <section class="py-16 relative">
      <div class="container mx-auto px-4">
        <div class="max-w-7xl mx-auto">
          <!-- Section Header -->
          <div class="flex items-center justify-between mb-12">
            <div>
              <h2 class="text-3xl font-bold text-gray-900 mb-2">最新文章</h2>
              <p class="text-gray-600">探索社区成员分享的精彩内容</p>
            </div>
            <router-link v-if="isLoggedIn" to="/post/create" class="btn-secondary hidden md:inline-flex items-center space-x-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              <span>写文章</span>
            </router-link>
          </div>

          <!-- Stats -->
          <div v-if="totalElements > 0" class="mb-8 text-center">
            <span class="inline-flex items-center px-4 py-2 bg-white/80 backdrop-blur-sm rounded-full shadow-sm border border-gray-100">
              <span class="text-gray-600">共找到</span>
              <span class="mx-2 font-semibold text-primary-600">{{ totalElements }}</span>
              <span class="text-gray-600">篇文章</span>
            </span>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="card p-6">
                <div class="h-48 bg-gradient-to-br from-gray-200 to-gray-300 rounded-lg mb-4"></div>
                <div class="h-6 bg-gray-200 rounded w-3/4 mb-3"></div>
                <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
                <div class="h-4 bg-gray-200 rounded w-5/6"></div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else-if="!posts.length" class="text-center py-20">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gradient-to-br from-primary-100 to-purple-100 mb-6 animate-float">
              <svg class="w-12 h-12 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">还没有文章</h3>
            <p class="text-gray-600 mb-8">成为第一个分享内容的人吧!</p>
            <router-link v-if="isLoggedIn" to="/post/create" class="btn-primary">
              写第一篇文章
            </router-link>
          </div>

          <!-- Posts Grid -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <PostCard
                v-for="post in posts"
                :key="post.id"
                :post="post"
                class="animate-scale-in"
                @like-changed="handleLikeChanged"
            />
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-12">
            <Pagination
                :current-page="currentPage"
                :total-pages="totalPages"
                @page-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import Header from '@/components/Header.vue'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'
import { getPosts } from '@/api/posts'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Home',
  components: {
    Header,
    PostCard,
    Pagination
  },
  setup() {
    const store = useStore()

    const loading = ref(false)
    const posts = ref([])
    const currentPage = ref(1)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const pageSize = 6 // 每页显示6篇文章

    const isLoggedIn = computed(() => store.getters.isLoggedIn)

    // 加载文章列表
    const loadPosts = async () => {
      loading.value = true
      try {
        // 调用API获取文章列表 (page从0开始)
        const response = await getPosts({
          page: currentPage.value - 1,
          size: pageSize
        })

        // 处理后端返回的Spring Data格式
        const contentArray = response.content || []

        // 转换数据格式以适配前端组件
        posts.value = contentArray.map(post => ({
          id: post.id,
          title: post.title,
          content: post.content,
          summary: post.content ? post.content.substring(0, 150).replace(/[#*`\n]/g, '') : '',
          author: {
            username: post.authorUsername,
            avatarUrl: getFullAvatarUrl(post.authorAvatarUrl)  // 转换为完整URL
          },
          createdAt: post.createdAt,
          updatedAt: post.updatedAt,
          likeCount: post.likeCount || 0,  // 从后端获取点赞数
          isLiked: post.isLiked || false,  // 从后端获取是否已点赞
          // 暂时显示静态数据,后续实现
          views: 0,
          comments: 0,
          tags: [],
          category: null

        }))

        totalPages.value = response.totalPages || 1
        totalElements.value = response.totalElements || 0

      } catch (error) {
        console.error('加载文章失败:', error)
        posts.value = []
      } finally {
        loading.value = false
      }
    }

    // 分页切换
    const handlePageChange = (page) => {
      currentPage.value = page
      loadPosts()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // 处理点赞变化
    const handleLikeChanged = ({ postId, likeCount, isLiked }) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        post.likeCount = likeCount
        post.isLiked = isLiked
      }
    }

    onMounted(() => {
      loadPosts()
    })

    return {
      loading,
      posts,
      currentPage,
      totalPages,
      totalElements,
      isLoggedIn,
      handlePageChange,
      handleLikeChanged
    }
  }
}
</script>