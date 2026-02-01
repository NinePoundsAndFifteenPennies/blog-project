<template>
  <div class="min-h-screen">
    <Header />

    <!-- Hero Section -->
    <section class="relative pt-24 pb-20 bg-gradient-to-b from-white to-gray-50">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto text-center">
          <h1 class="text-5xl md:text-7xl font-bold mb-6 animate-fade-in leading-tight">
            <span class="text-gray-900">分享你的</span><span class="text-primary-600">技术见解</span>
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
          <!-- Main Content Layout -->
          <div class="flex flex-col lg:flex-row gap-8">
            <!-- Main Content Area (Left - 2/3 width) -->
            <div class="flex-1 lg:w-2/3 order-1 lg:order-1 space-y-12">
              
              <!-- Module 1: Hot Articles (Top 12 by hotness) -->
              <div>
                <div class="flex items-center justify-between mb-6">
                  <h2 class="text-2xl font-bold text-gray-900 flex items-center">
                    <svg class="w-6 h-6 mr-2 text-red-500" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                    </svg>
                    热门文章
                  </h2>
                </div>
                
                <!-- Hot Articles Loading State -->
                <div v-if="loadingHot" class="space-y-4">
                  <div v-for="i in 3" :key="'hot-loading-' + i" class="animate-pulse">
                    <div class="card p-4 flex gap-4">
                      <div class="w-32 h-24 bg-gray-200 rounded-lg flex-shrink-0"></div>
                      <div class="flex-1">
                        <div class="h-6 bg-gray-200 rounded w-3/4 mb-2"></div>
                        <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
                        <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Hot Articles List (Scrollable container) -->
                <div v-else-if="hotPosts.length > 0" class="space-y-4 max-h-[800px] overflow-y-auto pr-2">
                  <PostCard
                      v-for="post in hotPosts"
                      :key="'hot-' + post.id"
                      :post="post"
                      :list-view="true"
                      class="animate-scale-in"
                      @like-changed="handleLikeChanged"
                  />
                </div>

                <!-- No Hot Articles -->
                <div v-else class="text-center py-10 card">
                  <p class="text-gray-500">暂无热门文章</p>
                </div>
              </div>

              <!-- Module 2: Latest Articles (with pagination) -->
              <div>
                <div class="flex items-center justify-between mb-6">
                  <h2 class="text-2xl font-bold text-gray-900 flex items-center">
                    <svg class="w-6 h-6 mr-2 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    最新文章
                  </h2>
                  <router-link v-if="isLoggedIn" to="/post/create" class="btn-secondary hidden md:inline-flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                    </svg>
                    <span>写文章</span>
                  </router-link>
                </div>

                <!-- Latest Articles Loading State -->
                <div v-if="loading" class="space-y-4">
                  <div v-for="i in 6" :key="'latest-loading-' + i" class="animate-pulse">
                    <div class="card p-4 flex gap-4">
                      <div class="w-32 h-24 bg-gray-200 rounded-lg flex-shrink-0"></div>
                      <div class="flex-1">
                        <div class="h-6 bg-gray-200 rounded w-3/4 mb-2"></div>
                        <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
                        <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Empty State -->
                <div v-else-if="!posts.length" class="text-center py-20 card">
                  <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-primary-100 mb-6">
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

                <!-- Latest Articles List -->
                <div v-else class="space-y-4">
                  <PostCard
                      v-for="post in posts"
                      :key="'latest-' + post.id"
                      :post="post"
                      :list-view="true"
                      class="animate-scale-in"
                      @like-changed="handleLikeChanged"
                  />
                </div>
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

            <!-- Sidebar (Right - 1/3 width) -->
            <aside class="lg:w-1/3 flex-shrink-0 order-2 lg:order-2">
              <div class="lg:sticky lg:top-24 space-y-6">
                <!-- Search Box -->
                <div class="card p-6 overflow-visible">
                  <h3 class="text-lg font-bold text-gray-900 mb-4">搜索文章</h3>
                  <SearchPreview
                    :search-function="searchGlobal"
                    placeholder="搜索文章..."
                    @select="handleSearchSelect"
                    @search="handleSearch"
                  />
                </div>

                <!-- Popular Tags -->
                <PopularTags />

                <!-- Hot Authors -->
                <HotAuthors />

                <!-- Community Stats -->
                <CommunityStats />
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
import SearchPreview from '@/components/SearchPreview.vue'
import CommunityStats from '@/components/CommunityStats.vue'
import HotAuthors from '@/components/HotAuthors.vue'
import { getPosts, searchPosts } from '@/api/posts'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Home',
  components: {
    Header,
    PostCard,
    Pagination,
    PopularTags,
    SearchPreview,
    CommunityStats,
    HotAuthors
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    const loading = ref(false)
    const loadingHot = ref(false)
    const posts = ref([])
    const hotPosts = ref([])
    // Initialize page from URL query parameter
    const currentPage = ref(parseInt(route.query.page) || 1)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const pageSize = 9 // 每页显示9篇文章

    const isLoggedIn = computed(() => store.getters.isLoggedIn)

    // 加载热门文章（热度最高的12篇）
    const loadHotPosts = async () => {
      loadingHot.value = true
      try {
        const response = await getPosts({
          page: 0,
          size: 12,
          sortBy: 'hotness',
          order: 'desc'
        })

        hotPosts.value = response.content.map(post => ({
          ...post,
          id: post.id,
          title: post.title,
          summary: post.summary,
          author: post.author || {},
          authorUsername: post.authorUsername,
          coverImageUrl: post.coverImageUrl,
          createdAt: post.createdAt,
          updatedAt: post.updatedAt,
          publishedAt: post.publishedAt,
          likeCount: post.likeCount || 0,
          commentCount: post.commentCount || 0,
          viewCount: post.viewCount || 0,
          isLiked: post.isLiked || false,
          tags: post.tags || [],
          views: 0
        }))
      } catch (error) {
        console.error('加载热门文章失败:', error)
        hotPosts.value = []
      } finally {
        loadingHot.value = false
      }
    }

    // 加载最新文章列表（按时间排序）
    const loadPosts = async () => {
      loading.value = true
      try {
        // 调用API获取文章列表 (page从0开始) - 按时间倒序
        const response = await getPosts({
          page: currentPage.value - 1,
          size: pageSize,
          sortBy: 'time',
          order: 'desc'
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

    // 处理点赞变化
    const handleLikeChanged = ({ postId, likeCount, isLiked }) => {
      // Update in latest posts list
      const postIndex = posts.value.findIndex(p => p.id === postId)
      if (postIndex !== -1) {
        // Update the post object to ensure reactivity
        posts.value[postIndex] = {
          ...posts.value[postIndex],
          likeCount,
          isLiked
        }
      }
      
      // Also update in hot posts list
      const hotPostIndex = hotPosts.value.findIndex(p => p.id === postId)
      if (hotPostIndex !== -1) {
        hotPosts.value[hotPostIndex] = {
          ...hotPosts.value[hotPostIndex],
          likeCount,
          isLiked
        }
      }
    }

    // 全局搜索函数（用于 SearchPreview）
    const searchGlobal = async (keyword) => {
      try {
        const res = await searchPosts({
          keyword,
          sortBy: 'hotness',
          size: 8
        })
        return res.content || []
      } catch (error) {
        console.error('Search failed:', error)
        return []
      }
    }

    // 处理搜索预览选中
    const handleSearchSelect = (post) => {
      router.push(`/post/${post.id}`)
    }

    // 处理搜索（回车跳转到搜索页）
    const handleSearch = (keyword) => {
      if (keyword && keyword.trim()) {
        router.push({ 
          path: '/search', 
          query: { keyword: keyword.trim() } 
        })
      }
    }

    onMounted(() => {
      loadHotPosts()
      loadPosts()
    })

    return {
      loading,
      loadingHot,
      posts,
      hotPosts,
      currentPage,
      totalPages,
      totalElements,
      isLoggedIn,
      handlePageChange,
      handleLikeChanged,
      searchGlobal,
      handleSearchSelect,
      handleSearch
    }
  }
}
</script>