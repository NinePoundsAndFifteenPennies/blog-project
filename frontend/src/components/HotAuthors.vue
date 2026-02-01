<template>
  <div class="card p-6">
    <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
      <svg class="w-5 h-5 mr-2 text-orange-500" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12.71 2.29a1 1 0 00-1.42 0l-9 9a1 1 0 000 1.42A1 1 0 003 13h1v7c0 1.1.9 2 2 2h12a2 2 0 002-2v-7h1a1 1 0 00.71-1.71l-9-9zM12 4.41L19.59 12H18c-.55 0-1 .45-1 1v6H7v-6c0-.55-.45-1-1-1H4.41L12 4.41z"/>
      </svg>
      🏆 热门创作者榜单
    </h3>

    <!-- Loading state -->
    <div v-if="loading" class="space-y-4">
      <div class="flex flex-col items-center animate-pulse">
        <div class="w-20 h-20 bg-gray-200 rounded-full mb-2"></div>
        <div class="h-4 bg-gray-200 rounded w-20 mb-1"></div>
        <div class="h-3 bg-gray-200 rounded w-16"></div>
      </div>
      <div class="flex justify-center space-x-8 animate-pulse">
        <div class="flex flex-col items-center">
          <div class="w-14 h-14 bg-gray-200 rounded-full mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-12"></div>
        </div>
        <div class="flex flex-col items-center">
          <div class="w-14 h-14 bg-gray-200 rounded-full mb-2"></div>
          <div class="h-3 bg-gray-200 rounded w-12"></div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!authors.length" class="text-center py-4 text-gray-500 text-sm">
      暂无热门作者
    </div>

    <!-- Authors list -->
    <div v-else class="space-y-4">
      <!-- Top 3 - "品" layout -->
      <div v-if="topAuthors.length > 0" class="pb-4 border-b border-gray-100">
        <!-- Top 1 - Center, largest -->
        <div v-if="topAuthors[0]" class="flex flex-col items-center mb-4">
          <div class="relative cursor-pointer" @click="goToProfile(topAuthors[0])">
            <!-- Crown for #1 -->
            <div class="absolute -top-3 left-1/2 transform -translate-x-1/2 text-xl z-10">👑</div>
            <div 
              class="w-20 h-20 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg overflow-hidden ring-4 ring-yellow-400 hover:ring-yellow-500 transition-all"
              :style="{ backgroundColor: getAvatarColor(topAuthors[0].username) }"
            >
              <img 
                v-if="topAuthors[0].avatarUrl && !avatarErrors[topAuthors[0].id]" 
                :src="getFullAvatarUrl(topAuthors[0].avatarUrl)" 
                :alt="getDisplayName(topAuthors[0])"
                class="w-full h-full object-cover"
                @error="avatarErrors[topAuthors[0].id] = true"
              />
              <span v-else>{{ getInitial(topAuthors[0]) }}</span>
            </div>
          </div>
          <div class="mt-2 text-center">
            <h4 
              class="font-bold text-gray-900 cursor-pointer hover:text-primary-600 transition-colors"
              @click="goToProfile(topAuthors[0])"
            >
              {{ getDisplayName(topAuthors[0]) }}
            </h4>
            <div class="flex items-center justify-center text-orange-500 font-medium text-sm">
              🔥 {{ formatHeatScore(topAuthors[0].heatScore) }}°C
            </div>
            <FollowButton 
              v-if="currentUser && currentUser.id !== topAuthors[0].id"
              :user-id="topAuthors[0].id"
              :initial-following="false"
              :initial-friend="false"
              class="mt-2 text-xs px-3 py-1"
            />
          </div>
        </div>

        <!-- Top 2 & 3 - Side by side -->
        <div class="flex justify-center space-x-8">
          <div 
            v-for="(author, index) in topAuthors.slice(1, 3)" 
            :key="author.id"
            class="flex flex-col items-center"
          >
            <div class="relative cursor-pointer" @click="goToProfile(author)">
              <!-- Medal for #2 and #3 -->
              <div class="absolute -top-2 left-1/2 transform -translate-x-1/2 text-sm z-10">
                {{ index === 0 ? '🥈' : '🥉' }}
              </div>
              <div 
                class="w-14 h-14 rounded-full flex items-center justify-center text-white text-lg font-bold shadow-md overflow-hidden ring-2 hover:ring-3 transition-all"
                :class="index === 0 ? 'ring-gray-400 hover:ring-gray-500' : 'ring-amber-600 hover:ring-amber-700'"
                :style="{ backgroundColor: getAvatarColor(author.username) }"
              >
                <img 
                  v-if="author.avatarUrl && !avatarErrors[author.id]" 
                  :src="getFullAvatarUrl(author.avatarUrl)" 
                  :alt="getDisplayName(author)"
                  class="w-full h-full object-cover"
                  @error="avatarErrors[author.id] = true"
                />
                <span v-else>{{ getInitial(author) }}</span>
              </div>
            </div>
            <div class="mt-2 text-center">
              <h4 
                class="font-semibold text-gray-900 text-sm cursor-pointer hover:text-primary-600 transition-colors"
                @click="goToProfile(author)"
              >
                {{ getDisplayName(author) }}
              </h4>
              <div class="flex items-center justify-center text-orange-500 text-xs">
                🔥 {{ formatHeatScore(author.heatScore) }}°C
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rest of the list (4-30) -->
      <div v-if="restAuthors.length > 0" class="space-y-2 max-h-64 overflow-y-auto">
        <div 
          v-for="(author, index) in restAuthors" 
          :key="author.id"
          class="flex items-center justify-between py-2 px-2 hover:bg-gray-50 rounded-lg transition-colors"
        >
          <div class="flex items-center space-x-3">
            <!-- Rank number -->
            <span class="text-gray-400 text-sm font-medium w-5 text-right">{{ index + 4 }}</span>
            <!-- Avatar -->
            <div 
              class="w-10 h-10 rounded-full flex items-center justify-center text-white text-sm font-bold shadow-sm overflow-hidden cursor-pointer hover:ring-2 hover:ring-primary-300 transition-all"
              :style="{ backgroundColor: getAvatarColor(author.username) }"
              @click="goToProfile(author)"
            >
              <img 
                v-if="author.avatarUrl && !avatarErrors[author.id]" 
                :src="getFullAvatarUrl(author.avatarUrl)" 
                :alt="getDisplayName(author)"
                class="w-full h-full object-cover"
                @error="avatarErrors[author.id] = true"
              />
              <span v-else>{{ getInitial(author) }}</span>
            </div>
            <!-- Name and followers -->
            <div class="min-w-0">
              <h4 
                class="font-medium text-gray-900 text-sm truncate cursor-pointer hover:text-primary-600 transition-colors"
                @click="goToProfile(author)"
              >
                {{ getDisplayName(author) }}
              </h4>
              <span class="text-xs text-gray-500">粉丝 {{ formatCount(author.followerCount) }}</span>
            </div>
          </div>
          <!-- Heat score -->
          <div class="flex items-center text-orange-500 text-xs font-medium">
            🔥{{ Math.round(author.heatScore) }}°
          </div>
        </div>
      </div>

      <!-- View full list link -->
      <div v-if="authors.length >= 7" class="text-center pt-2 border-t border-gray-100">
        <span class="text-sm text-gray-500">
          查看完整榜单 &gt;
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getHotAuthors } from '@/api/authors'
import { getFullAvatarUrl } from '@/utils/avatar'
import FollowButton from '@/components/FollowButton.vue'

export default {
  name: 'HotAuthors',
  components: {
    FollowButton
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const loading = ref(true)
    const authors = ref([])
    const avatarErrors = reactive({})

    const currentUser = computed(() => store.getters.currentUser)

    // Top 3 authors for special display
    const topAuthors = computed(() => authors.value.slice(0, 3))
    
    // Rest of the authors (4-30)
    const restAuthors = computed(() => authors.value.slice(3))

    const loadHotAuthors = async () => {
      loading.value = true
      try {
        const response = await getHotAuthors({ limit: 30 })
        authors.value = response || []
      } catch (error) {
        console.error('加载热门作者失败:', error)
        authors.value = []
      } finally {
        loading.value = false
      }
    }

    // Get display name (nickname or username)
    const getDisplayName = (author) => {
      return author.nickname || author.username || '用户'
    }

    // Get initial for avatar fallback
    const getInitial = (author) => {
      const name = getDisplayName(author)
      return name ? name.charAt(0).toUpperCase() : 'U'
    }

    // Generate consistent color for avatar based on username
    const getAvatarColor = (username) => {
      if (!username) return '#6366f1'
      const colors = [
        '#6366f1', '#8b5cf6', '#d946ef', '#ec4899', '#f43f5e',
        '#ef4444', '#f97316', '#f59e0b', '#eab308', '#84cc16',
        '#22c55e', '#10b981', '#14b8a6', '#06b6d4', '#0ea5e9',
        '#3b82f6', '#6366f1'
      ]
      let hash = 0
      for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash)
      }
      return colors[Math.abs(hash) % colors.length]
    }

    // Format heat score for display
    const formatHeatScore = (score) => {
      if (!score) return '0'
      // Normalize to a "temperature" display (max ~100°C)
      // Using logarithmic scaling for better display
      const normalized = Math.min(Math.round(score / 10), 100)
      return normalized
    }

    // Format count for display (e.g., 1234 -> 1.2k)
    const formatCount = (count) => {
      if (!count) return '0'
      if (count >= 10000) {
        return (count / 10000).toFixed(1) + 'w'
      }
      if (count >= 1000) {
        return (count / 1000).toFixed(1) + 'k'
      }
      return count.toString()
    }

    // Navigate to user profile
    const goToProfile = (author) => {
      router.push(`/user/${author.username}`)
    }

    onMounted(() => {
      loadHotAuthors()
    })

    return {
      loading,
      authors,
      topAuthors,
      restAuthors,
      avatarErrors,
      currentUser,
      getDisplayName,
      getInitial,
      getAvatarColor,
      getFullAvatarUrl,
      formatHeatScore,
      formatCount,
      goToProfile
    }
  }
}
</script>

<style scoped>
/* Custom scrollbar for the author list */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
