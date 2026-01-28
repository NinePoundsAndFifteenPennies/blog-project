<template>
  <div class="card p-6">
    <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
      <svg class="w-5 h-5 mr-2 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      社区统计
    </h3>
    
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 4" :key="i" class="animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-3/4"></div>
      </div>
    </div>
    
    <div v-else class="space-y-3">
      <!-- Total Users -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600 flex items-center">
          <svg class="w-4 h-4 mr-2 text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
          用户总数
        </span>
        <span class="text-sm font-semibold text-gray-900">{{ stats.totalUsers || 0 }}</span>
      </div>
      
      <!-- Total Articles -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600 flex items-center">
          <svg class="w-4 h-4 mr-2 text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          文章总数
        </span>
        <span class="text-sm font-semibold text-gray-900">{{ stats.totalPosts || 0 }}</span>
      </div>
      
      <!-- Online Users -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600 flex items-center">
          <svg class="w-4 h-4 mr-2 text-green-500" fill="currentColor" viewBox="0 0 20 20">
            <circle cx="10" cy="10" r="3" />
          </svg>
          在线用户
        </span>
        <span class="text-sm font-semibold text-green-600">{{ stats.onlineUsers || 0 }}</span>
      </div>
      
      <!-- Today's Visits -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600 flex items-center">
          <svg class="w-4 h-4 mr-2 text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
          今日访问
        </span>
        <span class="text-sm font-semibold text-gray-900">{{ stats.todayVisits || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

export default {
  name: 'CommunityStats',
  setup() {
    const loading = ref(true)
    const stats = ref({
      totalUsers: 0,
      totalPosts: 0,
      onlineUsers: 0,
      todayVisits: 0
    })

    let refreshInterval = null

    const loadStats = async () => {
      try {
        const response = await axios.get('/api/statistics')
        if (response.data) {
          stats.value = response.data
        }
      } catch (error) {
        console.error('Failed to load statistics:', error)
        // Keep current values on error, don't reset to 0
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadStats()
      
      // Refresh statistics every 30 seconds
      refreshInterval = setInterval(() => {
        loadStats()
      }, 30000)
    })

    onBeforeUnmount(() => {
      if (refreshInterval) {
        clearInterval(refreshInterval)
      }
    })

    return {
      loading,
      stats
    }
  }
}
</script>
