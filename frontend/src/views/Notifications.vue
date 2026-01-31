<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Page Header -->
          <div class="card p-6 mb-6 backdrop-blur-sm bg-white/90">
            <div class="flex items-center justify-between">
              <h1 class="text-2xl font-bold text-gray-900">我的通知</h1>
              <button
                v-if="unreadCount > 0"
                @click="handleMarkAllAsRead"
                :disabled="markingAllRead"
                class="px-4 py-2 text-sm text-primary-600 hover:bg-primary-50 rounded-lg transition-colors disabled:text-gray-400"
              >
                {{ markingAllRead ? '处理中...' : '全部标为已读' }}
              </button>
            </div>
          </div>

          <!-- Filter Tabs -->
          <div class="card mb-6 backdrop-blur-sm bg-white/90">
            <div class="flex border-b border-gray-200">
              <button
                v-for="tab in tabs"
                :key="tab.value"
                @click="currentFilter = tab.value"
                :class="[
                  'flex-1 py-4 px-4 text-center font-medium transition-colors duration-200',
                  currentFilter === tab.value
                    ? 'text-primary-600 border-b-2 border-primary-600'
                    : 'text-gray-500 hover:text-gray-700'
                ]"
              >
                <span class="flex items-center justify-center space-x-2">
                  <span v-html="tab.icon"></span>
                  <span>{{ tab.label }}</span>
                </span>
              </button>
            </div>
          </div>

          <!-- Notifications List -->
          <div v-if="loading" class="card p-8 text-center backdrop-blur-sm bg-white/90">
            <div class="spinner w-12 h-12 mx-auto"></div>
            <p class="text-gray-600 mt-4">加载中...</p>
          </div>

          <div v-else-if="notifications.length === 0" class="card p-16 text-center backdrop-blur-sm bg-white/90">
            <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 mb-6">
              <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-700 mb-2">暂无通知</h3>
            <p class="text-gray-500">{{ getEmptyMessage() }}</p>
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              @click="handleNotificationClick(notification)"
              :class="[
                'card p-4 backdrop-blur-sm cursor-pointer hover:shadow-md transition-all duration-200',
                notification.read ? 'bg-gray-50/90' : 'bg-white/90'
              ]"
            >
              <div class="flex items-start space-x-4">
                <!-- Type Icon -->
                <div
                  :class="[
                    'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0',
                    getTypeStyle(notification.type).bgColor
                  ]"
                >
                  <span v-html="getTypeStyle(notification.type).icon"></span>
                </div>

                <!-- Avatar -->
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600 flex-shrink-0"
                >
                  <img
                    v-if="notification.actorAvatarUrl"
                    :src="getAvatarUrl(notification.actorAvatarUrl)"
                    :alt="notification.actorNickname || notification.actorUsername"
                    class="w-full h-full object-cover"
                  />
                  <span v-else>{{ getInitial(notification.actorNickname || notification.actorUsername) }}</span>
                </div>

                <!-- Content -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1">
                    <p class="text-sm text-gray-900">
                      <span class="font-semibold">{{ notification.actorNickname || notification.actorUsername }}</span>
                      <span class="text-gray-600"> {{ getNotificationText(notification) }}</span>
                    </p>
                    <span class="text-xs text-gray-400 flex-shrink-0 ml-4">{{ formatTime(notification.createdAt) }}</span>
                  </div>
                  
                  <!-- Additional content preview -->
                  <p v-if="notification.content" class="text-sm text-gray-500 truncate">
                    "{{ notification.content }}"
                  </p>
                  <p v-if="notification.postTitle" class="text-sm text-gray-500 truncate">
                    📄 {{ notification.postTitle }}
                  </p>
                </div>

                <!-- Unread indicator -->
                <div v-if="!notification.read" class="w-2 h-2 bg-primary-600 rounded-full flex-shrink-0"></div>
              </div>
            </div>

            <!-- Load More -->
            <div v-if="hasMore" class="text-center py-4">
              <button
                @click="loadMore"
                :disabled="loadingMore"
                class="px-6 py-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors disabled:text-gray-400"
              >
                {{ loadingMore ? '加载中...' : '加载更多' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import { 
  getNotifications, 
  getNotificationUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from '@/api/notifications'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Notifications',
  components: { Header },
  setup() {
    const router = useRouter()
    
    const loading = ref(true)
    const loadingMore = ref(false)
    const markingAllRead = ref(false)
    const notifications = ref([])
    const unreadCount = ref(0)
    const currentPage = ref(0)
    const totalPages = ref(1)
    const currentFilter = ref('all')
    const PAGE_SIZE = 20

    const hasMore = computed(() => currentPage.value < totalPages.value - 1)

    const tabs = [
      { 
        value: 'all', 
        label: '全部',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>'
      },
      { 
        value: 'comments', 
        label: '评论',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>'
      },
      { 
        value: 'likes', 
        label: '点赞',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>'
      },
      { 
        value: 'follows', 
        label: '关注',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>'
      }
    ]

    const getAvatarUrl = (url) => getFullAvatarUrl(url)

    const getInitial = (name) => (name || 'U').charAt(0).toUpperCase()

    const formatTime = (timeStr) => {
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

    const getTypeStyle = (type) => {
      switch (type) {
        case 'POST_LIKED':
        case 'COMMENT_LIKED':
          return { 
            bgColor: 'bg-red-100', 
            icon: '<svg class="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>'
          }
        case 'POST_COMMENTED':
        case 'COMMENT_REPLIED':
          return { 
            bgColor: 'bg-blue-100', 
            icon: '<svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>'
          }
        case 'FOLLOWED':
          return { 
            bgColor: 'bg-green-100', 
            icon: '<svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" /></svg>'
          }
        case 'MESSAGE_RECEIVED':
          return { 
            bgColor: 'bg-purple-100', 
            icon: '<svg class="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>'
          }
        default:
          return { 
            bgColor: 'bg-gray-100', 
            icon: '<svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>'
          }
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
          return '给你发送了一条私信'
        default:
          return ''
      }
    }

    const getEmptyMessage = () => {
      switch (currentFilter.value) {
        case 'comments':
          return '暂无评论相关的通知'
        case 'likes':
          return '暂无点赞相关的通知'
        case 'follows':
          return '暂无关注相关的通知'
        default:
          return '当有人与你互动时，通知将会显示在这里'
      }
    }

    const loadNotifications = async (append = false) => {
      if (append) {
        loadingMore.value = true
      } else {
        loading.value = true
        currentPage.value = 0
      }

      try {
        const res = await getNotifications({
          page: currentPage.value,
          size: PAGE_SIZE,
          filter: currentFilter.value
        })

        if (append) {
          notifications.value = [...notifications.value, ...(res.content || [])]
        } else {
          notifications.value = res.content || []
        }
        totalPages.value = res.totalPages || 1

        // Also update unread count
        const countRes = await getNotificationUnreadCount()
        unreadCount.value = countRes.count || 0
      } catch (error) {
        console.error('加载通知失败:', error)
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    const loadMore = async () => {
      if (loadingMore.value || !hasMore.value) return
      currentPage.value++
      await loadNotifications(true)
    }

    const handleNotificationClick = async (notification) => {
      // Mark as read
      if (!notification.read) {
        try {
          await markNotificationAsRead(notification.id)
          notification.read = true
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }

      // Navigate based on type
      switch (notification.type) {
        case 'POST_LIKED':
        case 'POST_COMMENTED':
          if (notification.postId) {
            router.push(`/post/${notification.postId}`)
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

    const handleMarkAllAsRead = async () => {
      markingAllRead.value = true
      try {
        await markAllNotificationsAsRead(currentFilter.value)
        // Update local state
        notifications.value = notifications.value.map(n => ({ ...n, read: true }))
        unreadCount.value = 0
      } catch (error) {
        console.error('标记全部已读失败:', error)
      } finally {
        markingAllRead.value = false
      }
    }

    // Watch for filter changes
    watch(currentFilter, () => {
      loadNotifications()
    })

    onMounted(() => {
      loadNotifications()
    })

    return {
      loading,
      loadingMore,
      markingAllRead,
      notifications,
      unreadCount,
      currentFilter,
      hasMore,
      tabs,
      getAvatarUrl,
      getInitial,
      formatTime,
      getTypeStyle,
      getNotificationText,
      getEmptyMessage,
      loadMore,
      handleNotificationClick,
      handleMarkAllAsRead
    }
  }
}
</script>
