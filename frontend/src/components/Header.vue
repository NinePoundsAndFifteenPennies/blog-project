<template>
  <header
      class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
      :class="[scrolled ? 'bg-white/90 backdrop-blur-md shadow-md' : 'bg-transparent']"
  >
    <nav class="container mx-auto px-4 py-4">
      <div class="flex items-center justify-between">
        <!-- Logo -->
        <router-link to="/" class="flex items-center space-x-2 group">
          <div class="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center transform group-hover:rotate-12 transition-all duration-300">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
          <span class="text-xl font-bold text-gray-900">
            博客系统
          </span>
        </router-link>

        <!-- Desktop Navigation -->
        <div class="hidden md:flex items-center space-x-8">
          <router-link
              to="/"
              class="text-gray-700 hover:text-primary-600 font-medium transition-colors duration-200"
              active-class="text-primary-600"
          >
            首页
          </router-link>

          <router-link
              v-if="isLoggedIn"
              to="/post/create"
              class="text-gray-700 hover:text-primary-600 font-medium transition-colors duration-200"
              active-class="text-primary-600"
          >
            写文章
          </router-link>
        </div>

        <!-- Right Section -->
        <div class="hidden md:flex items-center space-x-4">
          <!-- Search Bar -->
          <div class="w-64">
            <SearchPreview
              :search-function="searchGlobal"
              :initial-value="currentKeyword"
              placeholder="搜索文章..."
              @select="handleSearchSelect"
              @search="handleSearch"
            />
          </div>

          <!-- Notification Bell (only when logged in) -->
          <div v-if="isLoggedIn" class="relative">
            <button
              @click="showNotificationDropdown = !showNotificationDropdown"
              class="relative p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-100 rounded-lg transition-colors duration-200"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <!-- Badge -->
              <span 
                v-if="unreadNotificationCount > 0" 
                class="absolute -top-1 -right-1 px-1.5 py-0.5 text-xs font-bold bg-red-500 text-white rounded-full min-w-[18px] text-center"
              >
                {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
              </span>
            </button>

            <!-- Notification Dropdown -->
            <transition name="fade">
              <div
                v-if="showNotificationDropdown"
                class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg py-2 border border-gray-100 z-50"
              >
                <div class="px-4 py-2 border-b border-gray-100 flex items-center justify-between">
                  <h3 class="font-semibold text-gray-900">通知</h3>
                  <router-link 
                    to="/notifications" 
                    class="text-sm text-primary-600 hover:text-primary-700"
                    @click="showNotificationDropdown = false"
                  >
                    查看全部
                  </router-link>
                </div>
                
                <div v-if="loadingNotifications" class="p-4 text-center">
                  <div class="spinner w-6 h-6 mx-auto"></div>
                </div>
                
                <div v-else-if="recentNotifications.length === 0" class="p-4 text-center text-gray-500 text-sm">
                  暂无通知
                </div>
                
                <div v-else class="max-h-80 overflow-y-auto">
                  <div
                    v-for="notification in recentNotifications"
                    :key="notification.id"
                    @click="handleNotificationClick(notification)"
                    :class="[
                      'px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors duration-200 flex items-start space-x-3',
                      !notification.read ? 'bg-blue-50/50' : ''
                    ]"
                  >
                    <!-- Type Icon -->
                    <div :class="['w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0', getNotificationTypeColor(notification.type)]">
                      <span v-html="getNotificationTypeIcon(notification.type)"></span>
                    </div>
                    
                    <div class="flex-1 min-w-0">
                      <p class="text-sm text-gray-900 line-clamp-2">
                        <span class="font-medium">{{ notification.actorNickname || notification.actorUsername }}</span>
                        <span class="text-gray-600"> {{ getNotificationText(notification) }}</span>
                      </p>
                      <p class="text-xs text-gray-400 mt-1">{{ formatNotificationTime(notification.createdAt) }}</p>
                    </div>
                    
                    <div v-if="!notification.read" class="w-2 h-2 bg-primary-600 rounded-full flex-shrink-0 mt-2"></div>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <!-- User Menu -->
          <div v-if="isLoggedIn" class="relative">
            <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center space-x-2 hover:opacity-80 transition-opacity duration-200"
            >
              <div 
                class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold overflow-hidden bg-primary-600"
              >
                <img 
                  v-if="userAvatarUrl && !avatarLoadError" 
                  :src="userAvatarUrl" 
                  :alt="currentUser.username"
                  :key="userAvatarUrl"
                  class="w-full h-full object-cover"
                  @error="handleAvatarError"
                  @load="handleAvatarLoad"
                />
                <span v-else>{{ userInitial }}</span>
              </div>
              <svg class="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <!-- Dropdown Menu -->
            <transition name="fade">
              <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-2 border border-gray-100"
              >
                <router-link
                    to="/profile"
                    class="block px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                    @click="showUserMenu = false"
                >
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span>个人中心</span>
                  </div>
                </router-link>
                
                <!-- Settings Link -->
                <router-link
                    to="/settings"
                    class="block px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                    @click="showUserMenu = false"
                >
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <span>设置</span>
                  </div>
                </router-link>
                
                <button
                    @click="handleSwitchAccount"
                    class="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                >
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                    </svg>
                    <span>切换账号</span>
                  </div>
                </button>
                <button
                    @click="handleLogout"
                    class="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                >
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    <span>退出登录</span>
                  </div>
                </button>
              </div>
            </transition>
          </div>

          <!-- Login/Register Buttons -->
          <div v-else class="flex items-center space-x-3">
            <router-link to="/login" class="btn-ghost">
              登录
            </router-link>
            <router-link to="/register" class="btn-primary">
              注册
            </router-link>
          </div>
        </div>

        <!-- Mobile Menu Button -->
        <button
            @click="showMobileMenu = !showMobileMenu"
            class="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors duration-200"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path v-if="!showMobileMenu" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Mobile Menu -->
      <transition name="slide-up">
        <div v-if="showMobileMenu" class="md:hidden mt-4 pb-4 border-t border-gray-200 pt-4">
          <div class="space-y-3">
            <router-link
                to="/"
                class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                @click="showMobileMenu = false"
            >
              首页
            </router-link>
            <router-link
                v-if="isLoggedIn"
                to="/post/create"
                class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                @click="showMobileMenu = false"
            >
              写文章
            </router-link>

            <div v-if="isLoggedIn" class="border-t border-gray-200 pt-3 mt-3">
              <router-link
                  to="/profile"
                  class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                  @click="showMobileMenu = false"
              >
                个人中心
              </router-link>
              <router-link
                  to="/notifications"
                  class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                  @click="showMobileMenu = false"
              >
                <div class="flex items-center justify-between">
                  <span>通知</span>
                  <span 
                    v-if="unreadNotificationCount > 0" 
                    class="px-2 py-0.5 text-xs font-bold bg-red-500 text-white rounded-full"
                  >
                    {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
                  </span>
                </div>
              </router-link>
              <router-link
                  to="/settings"
                  class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                  @click="showMobileMenu = false"
              >
                <div class="flex items-center space-x-2">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  <span>设置</span>
                </div>
              </router-link>
              
              <div class="border-t border-gray-100 my-2"></div>
              <button
                  @click="handleSwitchAccount"
                  class="w-full text-left px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              >
                切换账号
              </button>
              <button
                  @click="handleLogout"
                  class="w-full text-left px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              >
                退出登录
              </button>
            </div>

            <div v-else class="flex flex-col space-y-2">
              <router-link to="/login" class="btn-ghost text-center" @click="showMobileMenu = false">
                登录
              </router-link>
              <router-link to="/register" class="btn-primary text-center" @click="showMobileMenu = false">
                注册
              </router-link>
            </div>
          </div>
        </div>
      </transition>
    </nav>
  </header>
</template>

<script>
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { getFullAvatarUrl } from '@/utils/avatar'
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import SearchPreview from '@/components/SearchPreview.vue'
import { searchPosts } from '@/api/posts'
import { getUnreadCount, getUnreadCountFromUser } from '@/api/messages'
import { getNotificationUnreadCount, getRecentNotifications, markNotificationAsRead } from '@/api/notifications'

export default {
  name: 'Header',
  components: { SearchPreview },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    const scrolled = ref(false)
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const showNotificationDropdown = ref(false)
    const avatarLoadError = ref(false)
    const unreadMessageCount = ref(0)
    const unreadNotificationCount = ref(0)
    const recentNotifications = ref([])
    const loadingNotifications = ref(false)
    
    // Track which user's messages have been marked as read to prevent duplicate deductions
    // Using an object for Vue reactivity instead of Set
    const readUserIds = ref({})
    
    // Polling interval for unread messages and notifications
    let messagePollingInterval = null
    const MESSAGE_POLLING_INTERVAL = 10000 // 10 seconds

    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    const currentUser = computed(() => store.getters.currentUser)
    const userInitial = computed(() => {
      return currentUser.value?.username?.charAt(0).toUpperCase() || 'U'
    })
    const userAvatarUrl = computed(() => getFullAvatarUrl(currentUser.value?.avatarUrl))

    // 监听头像 URL 变化，重置错误状态
    watch(userAvatarUrl, () => {
      avatarLoadError.value = false
    })

    // 当前搜索关键词（从路由获取，用于保持搜索栏状态）
    const currentKeyword = computed(() => {
      if (route.path === '/search' && route.query.keyword) {
        return route.query.keyword
      }
      return ''
    })

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

    // 处理头像加载错误
    const handleAvatarError = () => {
      avatarLoadError.value = true
    }

    const handleAvatarLoad = () => {
      avatarLoadError.value = false
    }

    // 处理登出
    const handleLogout = () => {
      store.dispatch('logout')
      showUserMenu.value = false
      showMobileMenu.value = false
      stopMessagePolling()
      unreadMessageCount.value = 0
      router.push('/login')
    }

    // 处理切换账号（与登出相同，重定向到登录页面）
    const handleSwitchAccount = () => {
      handleLogout()
    }

    // 监听滚动
    const handleScroll = () => {
      scrolled.value = window.scrollY > 20
    }

    // 点击外部关闭菜单
    const handleClickOutside = (e) => {
      if (!e.target.closest('.relative')) {
        showUserMenu.value = false
        showNotificationDropdown.value = false
      }
    }
    
    // Fetch unread message count
    const fetchUnreadMessageCount = async () => {
      if (!isLoggedIn.value) return
      
      try {
        const res = await getUnreadCount()
        unreadMessageCount.value = res.count || 0
      } catch (error) {
        console.error('Failed to fetch unread message count:', error)
      }
    }
    
    // Fetch unread notification count
    const fetchUnreadNotificationCount = async () => {
      if (!isLoggedIn.value) return
      
      try {
        const res = await getNotificationUnreadCount()
        unreadNotificationCount.value = res.count || 0
      } catch (error) {
        console.error('Failed to fetch unread notification count:', error)
      }
    }
    
    // Fetch recent notifications
    const fetchRecentNotifications = async () => {
      if (!isLoggedIn.value) return
      
      loadingNotifications.value = true
      try {
        recentNotifications.value = await getRecentNotifications()
      } catch (error) {
        console.error('Failed to fetch recent notifications:', error)
      } finally {
        loadingNotifications.value = false
      }
    }
    
    // Start polling for unread messages and notifications
    const startMessagePolling = () => {
      stopMessagePolling()
      fetchUnreadMessageCount() // Fetch immediately
      fetchUnreadNotificationCount() // Fetch notifications immediately
      messagePollingInterval = setInterval(() => {
        fetchUnreadMessageCount()
        fetchUnreadNotificationCount()
      }, MESSAGE_POLLING_INTERVAL)
    }
    
    // Stop polling
    const stopMessagePolling = () => {
      if (messagePollingInterval) {
        clearInterval(messagePollingInterval)
        messagePollingInterval = null
      }
    }
    
    // Deduct unread count when user enters a specific conversation
    const deductUnreadFromUser = async (userId) => {
      if (!isLoggedIn.value || !userId) return
      
      // Skip if already processed for this user in this session
      const userIdNum = parseInt(userId)
      if (readUserIds.value[userIdNum]) return
      
      try {
        const res = await getUnreadCountFromUser(userIdNum)
        const userUnread = res.count || 0
        if (userUnread > 0) {
          // Mark as processed to prevent duplicate deductions
          readUserIds.value[userIdNum] = true
          // Deduct from total (but don't go below 0)
          unreadMessageCount.value = Math.max(0, unreadMessageCount.value - userUnread)
        }
      } catch (error) {
        console.error('Failed to get unread count from user:', error)
      }
    }
    
    // Watch for route changes to messages page with userId
    watch(() => route.query.userId, (newUserId) => {
      if (route.path === '/messages' && newUserId) {
        deductUnreadFromUser(newUserId)
      }
    }, { immediate: true })
    
    // Reset read user tracking when total count is refreshed from server
    watch(unreadMessageCount, (newValue, oldValue) => {
      // If count increased (new messages), or full refresh happened
      if (newValue > oldValue) {
        // Clear the tracking object to allow fresh deductions
        readUserIds.value = {}
      }
    })
    
    // Watch for login state changes
    watch(isLoggedIn, (newValue) => {
      if (newValue) {
        startMessagePolling()
      } else {
        stopMessagePolling()
        unreadMessageCount.value = 0
        unreadNotificationCount.value = 0
        recentNotifications.value = []
        readUserIds.value = {}
      }
    })
    
    // Watch for notification dropdown to open - fetch recent notifications
    watch(showNotificationDropdown, (newValue) => {
      if (newValue) {
        fetchRecentNotifications()
      }
    })
    
    // Notification helper functions
    const getNotificationTypeColor = (type) => {
      switch (type) {
        case 'POST_LIKED':
        case 'COMMENT_LIKED':
          return 'bg-red-100'
        case 'POST_COMMENTED':
        case 'COMMENT_REPLIED':
          return 'bg-blue-100'
        case 'FOLLOWED':
          return 'bg-green-100'
        case 'MESSAGE_RECEIVED':
          return 'bg-purple-100'
        default:
          return 'bg-gray-100'
      }
    }
    
    const getNotificationTypeIcon = (type) => {
      switch (type) {
        case 'POST_LIKED':
        case 'COMMENT_LIKED':
          return '<svg class="w-4 h-4 text-red-500" fill="currentColor" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>'
        case 'POST_COMMENTED':
        case 'COMMENT_REPLIED':
          return '<svg class="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>'
        case 'FOLLOWED':
          return '<svg class="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" /></svg>'
        case 'MESSAGE_RECEIVED':
          return '<svg class="w-4 h-4 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>'
        default:
          return '<svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>'
      }
    }
    
    const getNotificationText = (notification) => {
      switch (notification.type) {
        case 'POST_LIKED':
          return '赞了你的文章'
        case 'POST_COMMENTED':
          return '评论了你的文章'
        case 'COMMENT_LIKED':
          return '赞了你的评论'
        case 'COMMENT_REPLIED':
          return '回复了你的评论'
        case 'FOLLOWED':
          return '关注了你'
        case 'MESSAGE_RECEIVED':
          return '给你发送了私信'
        default:
          return ''
      }
    }
    
    const formatNotificationTime = (timeStr) => {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMs / 3600000)
      const diffDays = Math.floor(diffMs / 86400000)

      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24) return `${diffHours}小时前`
      if (diffDays < 7) return `${diffDays}天前`
      
      return date.toLocaleDateString('zh-CN')
    }
    
    const handleNotificationClick = async (notification) => {
      showNotificationDropdown.value = false
      
      // Mark as read
      if (!notification.read) {
        try {
          await markNotificationAsRead(notification.id)
          notification.read = true
          unreadNotificationCount.value = Math.max(0, unreadNotificationCount.value - 1)
        } catch (error) {
          console.error('Failed to mark notification as read:', error)
        }
      }

      // Navigate based on type
      switch (notification.type) {
        case 'POST_LIKED':
          if (notification.postId) {
            router.push(`/post/${notification.postId}`)
          }
          break
        case 'POST_COMMENTED':
          if (notification.postId) {
            // Navigate to post with comment anchor (the comment that was made)
            const commentAnchor = notification.commentId ? `#comment-${notification.commentId}` : ''
            router.push(`/post/${notification.postId}${commentAnchor}`)
          }
          break
        case 'COMMENT_LIKED':
        case 'COMMENT_REPLIED':
          if (notification.postId) {
            // Navigate to post with comment anchor
            const commentAnchor = notification.commentId ? `#comment-${notification.commentId}` : ''
            router.push(`/post/${notification.postId}${commentAnchor}`)
          }
          break
        case 'FOLLOWED':
          if (notification.actorUsername) {
            router.push(`/user/${notification.actorUsername}`)
          }
          break
        case 'MESSAGE_RECEIVED':
          router.push({
            path: '/messages',
            query: {
              userId: notification.actorId,
              username: notification.actorUsername,
              nickname: notification.actorNickname,
              avatar: notification.actorAvatarUrl
            }
          })
          break
      }
    }

    onMounted(() => {
      window.addEventListener('scroll', handleScroll)
      document.addEventListener('click', handleClickOutside)
      
      // Start polling if logged in
      if (isLoggedIn.value) {
        startMessagePolling()
      }
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
      document.removeEventListener('click', handleClickOutside)
      stopMessagePolling()
    })

    return {
      scrolled,
      showUserMenu,
      showMobileMenu,
      showNotificationDropdown,
      isLoggedIn,
      currentUser,
      userInitial,
      userAvatarUrl,
      avatarLoadError,
      unreadMessageCount,
      unreadNotificationCount,
      recentNotifications,
      loadingNotifications,
      currentKeyword,
      searchGlobal,
      handleSearchSelect,
      handleSearch,
      handleLogout,
      handleSwitchAccount,
      handleAvatarError,
      handleAvatarLoad,
      getNotificationTypeColor,
      getNotificationTypeIcon,
      getNotificationText,
      formatNotificationTime,
      handleNotificationClick
    }
  }
}
</script>