<template>
  <div class="min-h-screen">
    <Header />

    <!-- Hero Section -->
    <section class="relative pt-24 pb-20 bg-gradient-to-br from-white via-primary-50/30 to-secondary-50/30 dark:from-dark-950 dark:via-dark-900 dark:to-dark-950 overflow-hidden">
      <!-- Animated background elements -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-primary-200/20 dark:bg-primary-500/10 rounded-full blur-3xl animate-float"></div>
        <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-secondary-200/20 dark:bg-secondary-500/10 rounded-full blur-3xl animate-float" style="animation-delay: 2s;"></div>
      </div>
      
      <div class="container mx-auto px-4 relative z-10">
        <div class="max-w-4xl mx-auto text-center">
          <h1 class="text-5xl md:text-7xl font-bold mb-6 animate-fade-in leading-tight">
            <span class="text-gray-900 dark:text-gray-100">分享你的</span>
            <span class="bg-gradient-to-r from-primary-600 via-purple-600 to-secondary-600 dark:from-primary-400 dark:via-purple-400 dark:to-secondary-400 bg-clip-text text-transparent">技术见解</span>
          </h1>
          <p class="text-xl md:text-2xl text-gray-600 dark:text-gray-400 mb-8 animate-slide-up leading-relaxed">
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
    <section class="py-16 relative bg-gray-50 dark:bg-dark-950">
      <div class="container mx-auto px-4">
        <div class="max-w-7xl mx-auto">
          <!-- Section Header -->
          <div class="flex items-center justify-between mb-12">
            <div>
              <h2 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">{{ sortTitle }}</h2>
              <p class="text-gray-600 dark:text-gray-400">探索社区成员分享的精彩内容</p>
            </div>
            <div class="flex items-center space-x-4">
              <!-- Sorting Dropdown -->
              <div class="relative">
                <select 
                  v-model="selectedSort" 
                  @change="handleSortChange"
                  class="appearance-none bg-white dark:bg-dark-800 border border-gray-300 dark:border-dark-700 rounded-lg px-4 py-2 pr-10 text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 cursor-pointer"
                >
                  <option value="time_desc">按时间 (新→旧)</option>
                  <option value="time_asc">按时间 (旧→新)</option>
                  <option value="hotness_desc">按热度 (高→低)</option>
                  <option value="hotness_asc">按热度 (低→高)</option>
                </select>
                <div class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none">
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
              </div>
              <router-link v-if="isLoggedIn" to="/post/create" class="btn-secondary hidden md:inline-flex items-center space-x-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                <span>写文章</span>
              </router-link>
            </div>
          </div>

          <!-- Stats -->
          <div v-if="totalElements > 0" class="mb-8 text-center">
            <span class="inline-flex items-center px-4 py-2 bg-white dark:bg-dark-800 rounded-full shadow-sm border border-gray-200 dark:border-dark-700">
              <span class="text-gray-600 dark:text-gray-400">共找到</span>
              <span class="mx-2 font-semibold text-primary-600 dark:text-primary-400">{{ totalElements }}</span>
              <span class="text-gray-600 dark:text-gray-400">篇文章</span>
            </span>
          </div>

          <!-- Main Content Layout -->
          <div class="flex flex-col lg:flex-row gap-8">
            <!-- Main Content Area -->
            <div class="flex-1 order-1 lg:order-1">
              <!-- Loading State -->
              <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div v-for="i in 6" :key="i" class="animate-pulse">
                  <div class="card p-6 bg-white dark:bg-dark-900 border-gray-200 dark:border-dark-800">
                    <div class="h-48 bg-gray-200 dark:bg-dark-800 rounded-lg mb-4"></div>
                    <div class="h-6 bg-gray-200 dark:bg-dark-800 rounded w-3/4 mb-3"></div>
                    <div class="h-4 bg-gray-200 dark:bg-dark-800 rounded w-full mb-2"></div>
                    <div class="h-4 bg-gray-200 dark:bg-dark-800 rounded w-5/6"></div>
                  </div>
                </div>
              </div>

              <!-- Empty State -->
              <div v-else-if="!posts.length" class="text-center py-20">
                <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-primary-100 dark:bg-primary-900/30 mb-6">
                  <svg class="w-12 h-12 text-primary-600 dark:text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <h3 class="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-3">还没有文章</h3>
                <p class="text-gray-600 dark:text-gray-400 mb-8">成为第一个分享内容的人吧!</p>
                <router-link v-if="isLoggedIn" to="/post/create" class="btn-primary">
                  写第一篇文章
                </router-link>
              </div>

              <!-- Posts Grid -->
              <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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

            <!-- Sidebar - Popular Tags (Right Side) -->
            <aside class="lg:w-72 flex-shrink-0 order-2 lg:order-2">
              <div class="lg:sticky lg:top-24">
                <PopularTags />
              </div>
            </aside>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'
import PopularTags from '@/components/PopularTags.vue'
import { getPosts } from '@/api/posts'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Home',
  components: {
    Header,
    PostCard,
    Pagination,
    PopularTags
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    const loading = ref(false)
    const posts = ref([])
    // Initialize page from URL query parameter
    const currentPage = ref(parseInt(route.query.page) || 1)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const pageSize = 9 // 每页显示9篇文章 (3x3 grid)
    
    // 排序相关
    const selectedSort = ref(route.query.sort || 'time_desc')

    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    
    // 动态标题
    const sortTitle = computed(() => {
      if (selectedSort.value.startsWith('hotness')) {
        return '热门文章'
      }
      return '最新文章'
    })

    // 解析排序参数
    const parseSortParams = () => {
      const parts = selectedSort.value.split('_')
      return {
        sortBy: parts[0], // 'time' or 'hotness'
        order: parts[1]   // 'asc' or 'desc'
      }
    }

    // 加载文章列表
    const loadPosts = async () => {
      loading.value = true
      try {
        const { sortBy, order } = parseSortParams()
        
        // 调用API获取文章列表 (page从0开始)
        const response = await getPosts({
          page: currentPage.value - 1,
          size: pageSize,
          sortBy: sortBy,
          order: order
        })

        // 处理后端返回的Spring Data格式
        const contentArray = response.content || []

        // 转换数据格式以适配前端组件
        posts.value = contentArray.map(post => ({
          ...post,  // 保留所有原始字段（包括 authorUsername, authorNickname, authorAvatarUrl 等）
          
          // 处理需要转换的字段
          summary: post.content ? post.content.substring(0, 150).replace(/[#*`\n]/g, '') : '',
          authorAvatarUrl: getFullAvatarUrl(post.authorAvatarUrl),  // 转换为完整URL
          
          // 确保这些字段有默认值
          likeCount: post.likeCount || 0,
          isLiked: post.isLiked || false,
          commentCount: post.commentCount || 0,
          tags: post.tags || [],
          views: 0
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

    // 排序切换
    const handleSortChange = () => {
      currentPage.value = 1
      // Update URL with sort and page parameters, preserving other query params
      router.push({ query: { ...route.query, sort: selectedSort.value, page: 1 } })
      loadPosts()
    }

    // 分页切换
    const handlePageChange = (page) => {
      currentPage.value = page
      // Update URL with page parameter
      router.push({ query: { ...route.query, page } })
      loadPosts()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // Watch for route query changes (e.g., browser back/forward)
    watch(() => route.query.page, (newPage) => {
      const page = parseInt(newPage) || 1
      if (page !== currentPage.value) {
        currentPage.value = page
        loadPosts()
      }
    })
    
    // Watch for sort query changes (e.g., browser back/forward)
    watch(() => route.query.sort, (newSort) => {
      const sort = newSort || 'time_desc'
      if (sort !== selectedSort.value) {
        selectedSort.value = sort
        loadPosts()
      }
    })

    // 处理点赞变化
    const handleLikeChanged = ({ postId, likeCount, isLiked }) => {
      const postIndex = posts.value.findIndex(p => p.id === postId)
      if (postIndex !== -1) {
        // Update the post object to ensure reactivity
        posts.value[postIndex] = {
          ...posts.value[postIndex],
          likeCount,
          isLiked
        }
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
      selectedSort,
      sortTitle,
      handlePageChange,
      handleSortChange,
      handleLikeChanged
    }
  }
}
</script>