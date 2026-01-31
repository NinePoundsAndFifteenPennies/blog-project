<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <!-- Loading State -->
          <div v-if="loading && !userProfile" class="card p-16 text-center backdrop-blur-sm bg-white/90">
            <div class="spinner w-16 h-16 mx-auto"></div>
            <p class="text-gray-600 mt-4">加载中...</p>
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="card p-16 text-center backdrop-blur-sm bg-white/90">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-red-100 mb-6">
              <svg class="w-12 h-12 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-700 mb-3">{{ error }}</h3>
            <router-link to="/" class="btn-primary inline-flex items-center space-x-2 mt-4">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              <span>返回首页</span>
            </router-link>
          </div>

          <!-- Profile Content -->
          <template v-else-if="userProfile">
            <!-- Profile Header -->
            <div class="card p-8 mb-8 backdrop-blur-sm bg-white/90 animate-fade-in">
              <div class="flex flex-col md:flex-row items-center md:items-start space-y-6 md:space-y-0 md:space-x-8">
                <!-- Avatar -->
                <div class="relative">
                  <div 
                    class="w-32 h-32 rounded-full flex items-center justify-center text-white text-5xl font-bold shadow-md ring-4 ring-white overflow-hidden bg-primary-600"
                  >
                    <img 
                      v-if="userAvatarUrl && !avatarLoadError" 
                      :src="userAvatarUrl" 
                      :alt="userProfile.username"
                      class="w-full h-full object-cover"
                      @error="avatarLoadError = true"
                    />
                    <span v-else>{{ userInitial }}</span>
                  </div>
                </div>

                <!-- User Info -->
                <div class="flex-1 text-center md:text-left">
                  <h1 class="text-4xl font-bold text-gray-900 mb-2">{{ displayName }}</h1>
                  <p class="text-gray-500 text-base mb-4">@{{ userProfile.username }}</p>
                  
                  <!-- Bio -->
                  <p v-if="userProfile.bio" class="text-gray-700 mb-4 max-w-2xl">
                    {{ userProfile.bio }}
                  </p>
                  <p v-else class="text-gray-400 italic mb-4">暂无个人简介</p>

                  <!-- Additional Info -->
                  <div v-if="hasAdditionalInfo" class="flex flex-wrap items-center gap-4 text-sm text-gray-600 mb-6">
                    <div v-if="userProfile.location" class="flex items-center space-x-1">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      <span>{{ userProfile.location }}</span>
                    </div>
                    <div v-if="userProfile.socialLink" class="flex items-center space-x-1">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                      </svg>
                      <a 
                        :href="userProfile.socialLink" 
                        target="_blank" 
                        rel="noopener noreferrer"
                        class="text-primary-600 hover:text-primary-700 hover:underline"
                      >
                        个人链接
                      </a>
                    </div>
                  </div>

                  <!-- Stats -->
                  <div class="flex items-center justify-center md:justify-start space-x-8 mb-6">
                    <div class="text-center">
                      <div class="text-3xl font-bold text-primary-600 mb-1">{{ totalPosts }}</div>
                      <div class="text-sm text-gray-600 font-medium">文章</div>
                    </div>
                  </div>

                  <!-- Follow Stats -->
                  <div v-if="userProfile.id" class="mb-6">
                    <FollowStats 
                      ref="followStatsRef"
                      :userId="userProfile.id" 
                      @click="handleStatsClick"
                      @stats-loaded="handleStatsLoaded"
                    />
                  </div>

                  <!-- Follow Button -->
                  <div v-if="userProfile.id && !isOwnProfile" class="flex items-center space-x-3">
                    <FollowButton
                      :userId="userProfile.id"
                      :initialFollowing="followStatus.isFollowing"
                      :initialFriend="followStatus.isFriend"
                      @follow-change="handleFollowChange"
                    />
                    <button
                      @click="startMessage"
                      class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-lg text-gray-700 bg-white hover:bg-gray-50 transition-colors"
                    >
                      <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                      </svg>
                      私信
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Posts Tab Header -->
            <div class="card mb-8 backdrop-blur-sm bg-white/90 animate-slide-up relative z-20" style="animation-delay: 0.1s; overflow: visible;">
              <div class="p-6" style="overflow: visible;">
                <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-4">
                  <div class="flex items-center">
                    <span class="text-xl font-semibold text-gray-900">TA的文章</span>
                    <span class="ml-2 px-2.5 py-0.5 text-xs font-bold rounded-full bg-primary-100 text-primary-700">
                      {{ totalPosts }}
                    </span>
                  </div>
                  
                  <!-- Search Box -->
                  <div v-if="totalPosts > 0" class="w-full md:w-96 relative z-30">
                    <SearchPreview
                      :search-function="searchUserPosts"
                      :placeholder="`搜索 ${displayName} 的文章...`"
                      @select="handleSearchSelect"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Loading Posts -->
            <div v-if="loadingPosts" class="text-center py-20">
              <div class="spinner w-16 h-16 mx-auto"></div>
              <p class="text-gray-600 mt-4">加载中...</p>
            </div>

            <!-- Posts List -->
            <div v-else-if="posts.length" class="space-y-4 animate-slide-up relative z-10" style="animation-delay: 0.2s;">
              <div
                  v-for="post in posts"
                  :key="post.id"
                  class="card p-6 hover:shadow-glow transition-all duration-300 group backdrop-blur-sm bg-white/90"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <div class="flex items-center space-x-3 mb-3">
                      <router-link
                          :to="`/post/${post.id}`"
                          class="text-xl font-bold text-gray-900 group-hover:text-primary-600 transition-colors duration-200"
                      >
                        {{ post.title }}
                      </router-link>
                    </div>

                    <p class="text-gray-600 mt-2 line-clamp-2 text-sm">{{ post.summary }}</p>

                    <div class="flex items-center space-x-6 mt-4 text-sm text-gray-500">
                      <span v-if="post.publishedAt" class="flex items-center">
                        <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        {{ formatDate(post.publishedAt) }}
                      </span>
                    </div>

                    <!-- Stats -->
                    <div class="flex items-center space-x-6 mt-3 text-sm">
                      <span class="flex items-center text-red-500" :title="`${post.likeCount || 0} 个点赞`">
                        <svg class="w-4 h-4 mr-1.5" :fill="post.isLiked ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                        </svg>
                        {{ post.likeCount || 0 }}
                      </span>
                      <span class="flex items-center text-primary-500" :title="`${post.commentCount || 0} 条评论`">
                        <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                        </svg>
                        {{ post.commentCount || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Empty State -->
            <div v-else class="card p-16 text-center backdrop-blur-sm bg-white/90 animate-scale-in">
              <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-primary-100 mb-6">
                <svg class="w-12 h-12 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 class="text-2xl font-bold text-gray-700 mb-3">还没有发布文章</h3>
              <p class="text-gray-500 mb-8 text-lg">该用户暂未发布任何文章</p>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="mt-8">
              <Pagination
                  :current-page="currentPage"
                  :total-pages="totalPages"
                  @page-change="handlePageChange"
              />
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import Header from '@/components/Header.vue'
import Pagination from '@/components/Pagination.vue'
import SearchPreview from '@/components/SearchPreview.vue'
import FollowButton from '@/components/FollowButton.vue'
import FollowStats from '@/components/FollowStats.vue'
import { getPublicUserProfile } from '@/api/auth'
import { getPostsByUsername, searchPosts } from '@/api/posts'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'UserProfile',
  components: { Header, Pagination, SearchPreview, FollowButton, FollowStats },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const loading = ref(true)
    const loadingPosts = ref(false)
    const error = ref(null)
    const userProfile = ref(null)
    const avatarLoadError = ref(false)
    const posts = ref([])
    const currentPage = ref(1)
    const totalPages = ref(1)
    const totalPosts = ref(0)
    const pageSize = 10
    const followStatsRef = ref(null)
    const followStatus = ref({
      isFollowing: false,
      isFriend: false
    })

    const currentUser = computed(() => store.getters.currentUser)
    const username = computed(() => route.params.username)

    const isOwnProfile = computed(() => {
      return currentUser.value && userProfile.value && 
             currentUser.value.id === userProfile.value.id
    })

    const displayName = computed(() => 
      userProfile.value?.nickname || userProfile.value?.username || '用户'
    )

    const userInitial = computed(() => 
      displayName.value.charAt(0).toUpperCase() || 'U'
    )

    const userAvatarUrl = computed(() => 
      getFullAvatarUrl(userProfile.value?.avatarUrl)
    )

    const hasAdditionalInfo = computed(() => {
      return userProfile.value?.location || userProfile.value?.socialLink
    })

    const formatDate = (str) => {
      if (!str) return ''
      const date = new Date(str)
      const now = new Date()
      const diff = now - date
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (days === 0) return '今天'
      if (days === 1) return '昨天'
      if (days < 7) return `${days}天前`
      if (days < 30) return `${Math.floor(days / 7)}周前`
      if (days < 365) return `${Math.floor(days / 30)}个月前`

      return date.toLocaleDateString('zh-CN')
    }

    const loadProfile = async () => {
      loading.value = true
      error.value = null

      // Don't redirect for own profile - allow search on own profile too
      // Removed: if (currentUser.value && username.value === currentUser.value.username)

      try {
        userProfile.value = await getPublicUserProfile(username.value)
        // Load posts after profile is loaded
        await loadPosts()
      } catch (e) {
        if (e.response?.status === 404) {
          error.value = '用户不存在'
        } else {
          error.value = '加载用户信息失败'
        }
      } finally {
        loading.value = false
      }
    }

    const loadPosts = async () => {
      loadingPosts.value = true
      try {
        const res = await getPostsByUsername(username.value, {
          page: currentPage.value - 1,
          size: pageSize
        })
        
        const mappedPosts = (res.content || []).map(p => ({
          ...p,
          summary: p.content?.replace(/[#*`\n]/g, '').slice(0, 100) || ''
        }))
        
        posts.value = mappedPosts
        
        totalPages.value = res.totalPages || 1
        totalPosts.value = res.totalElements || 0
      } catch (e) {
        console.error('加载文章失败:', e)
      } finally {
        loadingPosts.value = false
      }
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      loadPosts()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // Search function for SearchPreview component
    const searchUserPosts = async (keyword) => {
      try {
        const res = await searchPosts({
          keyword,
          author: username.value,
          sortBy: 'hotness',
          size: 8
        })
        return res.content || []
      } catch (error) {
        console.error('Search failed:', error)
        return []
      }
    }

    // Handle search result selection
    const handleSearchSelect = (post) => {
      router.push(`/post/${post.id}`)
    }

    // Handle click on follow stats
    const handleStatsClick = (type) => {
      if (userProfile.value) {
        router.push({
          name: 'FollowList',
          params: { 
            userId: userProfile.value.id, 
            type: type 
          },
          query: { 
            username: userProfile.value.username 
          }
        })
      }
    }

    // Handle follow/unfollow action
    const handleFollowChange = (event) => {
      followStatus.value.isFollowing = event.following
      followStatus.value.isFriend = event.friend
      // Refresh follow stats
      if (followStatsRef.value) {
        followStatsRef.value.refresh()
      }
    }

    // Handle stats loaded from FollowStats component
    const handleStatsLoaded = (data) => {
      followStatus.value.isFollowing = data.isFollowing === true
      followStatus.value.isFriend = data.isFriend === true
    }

    // Start a message conversation with this user
    const startMessage = () => {
      if (!userProfile.value) return
      
      router.push({
        path: '/messages',
        query: {
          userId: userProfile.value.id,
          username: userProfile.value.username,
          nickname: userProfile.value.nickname,
          avatar: userProfile.value.avatarUrl,
          friend: followStatus.value.isFriend ? 'true' : 'false'
        }
      })
    }

    // Watch for username changes
    watch(username, () => {
      if (username.value) {
        avatarLoadError.value = false
        currentPage.value = 1
        followStatus.value = { isFollowing: false, isFriend: false }
        loadProfile()
      }
    })

    onMounted(() => {
      loadProfile()
    })

    return {
      loading,
      loadingPosts,
      error,
      userProfile,
      displayName,
      userInitial,
      userAvatarUrl,
      avatarLoadError,
      hasAdditionalInfo,
      posts,
      totalPosts,
      currentPage,
      totalPages,
      formatDate,
      searchUserPosts,
      handleSearchSelect,
      handlePageChange,
      isOwnProfile,
      followStatsRef,
      followStatus,
      handleStatsClick,
      handleFollowChange,
      handleStatsLoaded,
      startMessage
    }
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
