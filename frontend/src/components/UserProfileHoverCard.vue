<template>
  <div class="relative inline-block" @mouseenter="showCard" @mouseleave="hideCard">
    <!-- Avatar Trigger -->
    <div 
      class="cursor-pointer"
      @click="goToProfile"
    >
      <slot></slot>
    </div>

    <!-- Hover Card -->
    <transition name="fade">
      <div 
        v-if="isVisible && userInfo"
        class="absolute z-50 w-72 bg-white rounded-lg shadow-xl border border-gray-200 p-4 mt-2"
        :class="cardPosition"
        @mouseenter="showCard"
        @mouseleave="hideCard"
      >
        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-4">
          <div class="spinner w-6 h-6"></div>
        </div>

        <!-- User Info -->
        <div v-else class="space-y-3">
          <!-- Avatar and Name -->
          <div class="flex items-center space-x-3">
            <div 
              class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold shadow-sm overflow-hidden bg-primary-600"
            >
              <img 
                v-if="userInfo.avatarUrl && !avatarError" 
                :src="userInfo.avatarUrl" 
                :alt="displayName"
                class="w-full h-full object-cover"
                @error="avatarError = true"
              />
              <span v-else>{{ userInitial }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-base font-bold text-gray-900 truncate">{{ displayName }}</h3>
              <p class="text-xs text-gray-500 truncate">@{{ userInfo.username }}</p>
            </div>
          </div>

          <!-- Bio -->
          <p v-if="userInfo.bio" class="text-sm text-gray-600 line-clamp-2">
            {{ userInfo.bio }}
          </p>
          <p v-else class="text-sm text-gray-400 italic">暂无个人简介</p>

          <!-- Stats -->
          <div class="flex items-center space-x-4 text-xs text-gray-500 border-t pt-3">
            <div v-if="userInfo.location" class="flex items-center space-x-1">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span class="truncate">{{ userInfo.location }}</span>
            </div>
            <div v-if="userInfo.socialLink" class="flex items-center space-x-1 truncate">
              <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
              </svg>
              <a 
                :href="userInfo.socialLink" 
                target="_blank" 
                rel="noopener noreferrer"
                class="text-primary-600 hover:text-primary-700 truncate"
                @click.stop
              >
                链接
              </a>
            </div>
          </div>

          <!-- View Profile Button -->
          <button
            @click="goToProfile"
            class="w-full mt-2 py-2 px-3 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-colors"
          >
            查看详细资料
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser } from '@/api/auth'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'UserProfileHoverCard',
  props: {
    username: {
      type: String,
      required: true
    },
    // Optional: if you already have user data, pass it to avoid API call
    userData: {
      type: Object,
      default: null
    },
    position: {
      type: String,
      default: 'left',
      validator: (value) => ['left', 'right'].includes(value)
    }
  },
  setup(props) {
    const router = useRouter()
    const isVisible = ref(false)
    const loading = ref(false)
    const userInfo = ref(props.userData)
    const avatarError = ref(false)
    let hideTimeout = null

    const cardPosition = computed(() => {
      return props.position === 'right' ? 'left-0' : 'right-0'
    })

    const displayName = computed(() => {
      return userInfo.value?.nickname || userInfo.value?.username || '用户'
    })

    const userInitial = computed(() => {
      const name = displayName.value
      return name ? name.charAt(0).toUpperCase() : 'U'
    })

    const showCard = async () => {
      if (hideTimeout) {
        clearTimeout(hideTimeout)
        hideTimeout = null
      }

      isVisible.value = true

      // If we don't have user data yet, fetch it
      if (!userInfo.value) {
        loading.value = true
        try {
          // For now, we'll use the current user API since we don't have a public user profile API yet
          // TODO: Update this when backend provides /api/users/{username} endpoint
          const data = await getCurrentUser()
          if (data.username === props.username) {
            userInfo.value = {
              ...data,
              avatarUrl: getFullAvatarUrl(data.avatarUrl)
            }
          }
        } catch (error) {
          console.error('Failed to load user info:', error)
        } finally {
          loading.value = false
        }
      } else {
        // Process avatar URL if not already processed
        if (userInfo.value.avatarUrl && !userInfo.value.avatarUrl.startsWith('http')) {
          userInfo.value.avatarUrl = getFullAvatarUrl(userInfo.value.avatarUrl)
        }
      }
    }

    const hideCard = () => {
      hideTimeout = setTimeout(() => {
        isVisible.value = false
      }, 200)
    }

    const goToProfile = () => {
      isVisible.value = false
      // For now, go to the user's own profile if it matches current user
      // TODO: Update to go to public user profile when that route exists
      router.push(`/profile`)
    }

    return {
      isVisible,
      loading,
      userInfo,
      avatarError,
      cardPosition,
      displayName,
      userInitial,
      showCard,
      hideCard,
      goToProfile
    }
  }
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
