<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-7xl mx-auto">
          <!-- Search Header -->
          <div class="mb-8">
            <h1 class="text-3xl font-bold text-gray-900 mb-4">搜索结果</h1>
            
            <!-- Advanced Search Form -->
            <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
                <!-- Keyword Search -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">关键词</label>
                  <input
                    v-model="searchKeyword"
                    type="text"
                    placeholder="搜索标题或内容..."
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    @keyup.enter="handleSearch"
                  />
                </div>
                
                <!-- Author Search -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">作者</label>
                  <input
                    v-model="searchAuthor"
                    type="text"
                    placeholder="搜索作者..."
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    @keyup.enter="handleSearch"
                  />
                </div>
                
                <!-- Title Search -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">标题</label>
                  <input
                    v-model="searchTitle"
                    type="text"
                    placeholder="搜索标题..."
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    @keyup.enter="handleSearch"
                  />
                </div>
                
                <!-- Tag Search -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">标签</label>
                  <input
                    v-model="searchTag"
                    type="text"
                    placeholder="搜索标签..."
                    class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    @keyup.enter="handleSearch"
                  />
                </div>
              </div>
              
              <!-- Sort Options and Search Button -->
              <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div class="flex items-center space-x-4">
                  <!-- Sort By -->
                  <div class="flex items-center space-x-2">
                    <label class="text-sm font-medium text-gray-700">排序:</label>
                    <select 
                      v-model="selectedSort" 
                      class="appearance-none bg-white border border-gray-300 rounded-lg px-3 py-2 pr-8 text-gray-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 cursor-pointer"
                    >
                      <option value="time_desc">时间 (新→旧)</option>
                      <option value="time_asc">时间 (旧→新)</option>
                      <option value="hotness_desc">热度 (高→低)</option>
                      <option value="hotness_asc">热度 (低→高)</option>
                    </select>
                  </div>
                </div>
                
                <div class="flex items-center space-x-3">
                  <button 
                    @click="clearSearch" 
                    class="btn-ghost px-4 py-2"
                  >
                    清除
                  </button>
                  <button 
                    @click="handleSearch" 
                    class="btn-primary px-6 py-2 inline-flex items-center space-x-2"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <span>搜索</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Search Summary -->
            <div v-if="hasSearched" class="flex items-center justify-between">
              <div class="text-gray-600">
                <span v-if="loading">正在搜索...</span>
                <span v-else-if="totalElements > 0">
                  找到 <span class="font-semibold text-primary-600">{{ totalElements }}</span> 篇相关文章
                </span>
                <span v-else>未找到相关文章</span>
              </div>
              
              <!-- Active Filters -->
              <div v-if="activeFilters.length > 0" class="flex items-center space-x-2">
                <span class="text-sm text-gray-500">筛选条件:</span>
                <div class="flex flex-wrap gap-2">
                  <span 
                    v-for="filter in activeFilters" 
                    :key="filter.key" 
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-700"
                  >
                    {{ filter.label }}: {{ filter.value }}
                    <button 
                      @click="removeFilter(filter.key)" 
                      class="ml-1 hover:text-primary-900"
                    >
                      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Back button -->
          <div class="mb-6">
            <router-link to="/" class="btn-ghost inline-flex items-center space-x-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              <span>返回首页</span>
            </router-link>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="card p-6">
                <div class="h-48 bg-gray-200 rounded-lg mb-4"></div>
                <div class="h-6 bg-gray-200 rounded w-3/4 mb-3"></div>
                <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
                <div class="h-4 bg-gray-200 rounded w-5/6"></div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else-if="hasSearched && !posts.length" class="text-center py-20">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-primary-100 mb-6">
              <svg class="w-12 h-12 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">未找到相关文章</h3>
            <p class="text-gray-600 mb-8">尝试使用其他关键词或减少筛选条件</p>
            <button @click="clearSearch" class="btn-primary">
              清除搜索条件
            </button>
          </div>

          <!-- Initial State (before search) -->
          <div v-else-if="!hasSearched" class="text-center py-20">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gray-100 mb-6">
              <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">开始搜索</h3>
            <p class="text-gray-600">在上方输入搜索条件，查找您感兴趣的文章</p>
          </div>

          <!-- Posts Grid -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <PostCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
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
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'
import { searchPosts } from '@/api/posts'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Search',
  components: {
    Header,
    PostCard,
    Pagination
  },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const loading = ref(false)
    const posts = ref([])
    const currentPage = ref(1)
    const totalPages = ref(0)
    const totalElements = ref(0)
    const hasSearched = ref(false)
    const pageSize = 9

    // Search parameters
    const searchKeyword = ref('')
    const searchAuthor = ref('')
    const searchTitle = ref('')
    const searchTag = ref('')
    const selectedSort = ref('time_desc')

    // Computed active filters for display
    const activeFilters = computed(() => {
      const filters = []
      if (searchKeyword.value) filters.push({ key: 'keyword', label: '关键词', value: searchKeyword.value })
      if (searchAuthor.value) filters.push({ key: 'author', label: '作者', value: searchAuthor.value })
      if (searchTitle.value) filters.push({ key: 'title', label: '标题', value: searchTitle.value })
      if (searchTag.value) filters.push({ key: 'tag', label: '标签', value: searchTag.value })
      return filters
    })

    // Parse sort parameters
    const parseSortParams = () => {
      const parts = selectedSort.value.split('_')
      return {
        sortBy: parts[0],
        order: parts[1]
      }
    }

    // Load search results
    const loadSearchResults = async (page = 1) => {
      // Check if there are any search criteria
      if (!searchKeyword.value && !searchAuthor.value && !searchTitle.value && !searchTag.value) {
        hasSearched.value = false
        posts.value = []
        return
      }

      loading.value = true
      hasSearched.value = true

      try {
        const { sortBy, order } = parseSortParams()

        const response = await searchPosts({
          keyword: searchKeyword.value || undefined,
          author: searchAuthor.value || undefined,
          title: searchTitle.value || undefined,
          tag: searchTag.value || undefined,
          sortBy: sortBy,
          order: order,
          page: page - 1, // Backend uses 0-based indexing
          size: pageSize
        })

        // Transform posts data
        posts.value = (response.content || []).map(post => ({
          ...post,
          summary: post.content ? post.content.substring(0, 150).replace(/[#*`\n]/g, '') : '',
          authorAvatarUrl: getFullAvatarUrl(post.authorAvatarUrl),
          likeCount: post.likeCount || 0,
          isLiked: post.isLiked || false,
          commentCount: post.commentCount || 0,
          tags: post.tags || []
        }))

        totalPages.value = response.totalPages || 1
        totalElements.value = response.totalElements || 0
        currentPage.value = page

      } catch (error) {
        console.error('搜索失败:', error)
        posts.value = []
        totalElements.value = 0
      } finally {
        loading.value = false
      }
    }

    // Handle search button click
    const handleSearch = () => {
      currentPage.value = 1
      
      // Update URL with search parameters
      router.push({
        path: '/search',
        query: {
          keyword: searchKeyword.value || undefined,
          author: searchAuthor.value || undefined,
          title: searchTitle.value || undefined,
          tag: searchTag.value || undefined,
          sort: selectedSort.value !== 'time_desc' ? selectedSort.value : undefined,
          page: 1
        }
      })
      
      loadSearchResults(1)
    }

    // Clear all search filters
    const clearSearch = () => {
      searchKeyword.value = ''
      searchAuthor.value = ''
      searchTitle.value = ''
      searchTag.value = ''
      selectedSort.value = 'time_desc'
      hasSearched.value = false
      posts.value = []
      
      router.push({ path: '/search' })
    }

    // Remove a specific filter
    const removeFilter = (key) => {
      switch (key) {
        case 'keyword':
          searchKeyword.value = ''
          break
        case 'author':
          searchAuthor.value = ''
          break
        case 'title':
          searchTitle.value = ''
          break
        case 'tag':
          searchTag.value = ''
          break
      }
      handleSearch()
    }

    // Handle pagination
    const handlePageChange = (page) => {
      currentPage.value = page
      
      router.push({
        path: '/search',
        query: {
          ...route.query,
          page: page
        }
      })
      
      loadSearchResults(page)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // Handle like changes
    const handleLikeChanged = ({ postId, likeCount, isLiked }) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        post.likeCount = likeCount
        post.isLiked = isLiked
      }
    }

    // Initialize from URL query parameters
    const initFromQuery = () => {
      searchKeyword.value = route.query.keyword || ''
      searchAuthor.value = route.query.author || ''
      searchTitle.value = route.query.title || ''
      searchTag.value = route.query.tag || ''
      selectedSort.value = route.query.sort || 'time_desc'
      currentPage.value = parseInt(route.query.page) || 1

      // If there are search parameters, execute search
      if (searchKeyword.value || searchAuthor.value || searchTitle.value || searchTag.value) {
        loadSearchResults(currentPage.value)
      }
    }

    // Watch for route changes
    watch(() => route.query, () => {
      initFromQuery()
    }, { deep: true })

    onMounted(() => {
      initFromQuery()
    })

    return {
      loading,
      posts,
      currentPage,
      totalPages,
      totalElements,
      hasSearched,
      searchKeyword,
      searchAuthor,
      searchTitle,
      searchTag,
      selectedSort,
      activeFilters,
      handleSearch,
      clearSearch,
      removeFilter,
      handlePageChange,
      handleLikeChanged
    }
  }
}
</script>
