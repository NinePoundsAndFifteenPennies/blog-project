<template>
  <div class="relative inline-block">
    <!-- Avatar Trigger (clickable to go to profile) -->
    <div 
      @mouseenter="handleMouseEnter" 
      @mouseleave="hideCard"
      @click.stop="goToProfile"
      class="cursor-pointer"
    >
      <slot></slot>
    </div>

    <!-- Hover Card - Teleported to body to avoid CSS isolation issues -->
    <Teleport to="body">
      <transition name="fade">
        <div 
          v-if="isVisible && userInfo"
          class="fixed z-[9999] w-72 bg-white rounded-lg shadow-2xl border border-gray-200 p-4"
          :style="cardStyle"
          @mouseenter="cancelHide"
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
                class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold shadow-sm overflow-hidden bg-primary-600 hover:ring-2 hover:ring-primary-300 transition-all cursor-pointer"
                @click="goToProfile"
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
                <h3 class="text-base font-bold text-gray-900 truncate hover:text-primary-600 transition-colors cursor-pointer" @click="goToProfile">{{ displayName }}</h3>
                <p class="text-xs text-gray-500 truncate">@{{ userInfo.username }}</p>
              </div>
            </div>

            <!-- Bio -->
            <p v-if="userInfo.bio" class="text-sm text-gray-600 line-clamp-2">
              {{ userInfo.bio }}
            </p>
            <p v-else class="text-sm text-gray-400 italic">暂无个人简介</p>

            <!-- Follow Button -->
            <FollowButton 
              v-if="showFollowButton && userInfo.id"
              :user-id="userInfo.id"
              :initial-following="isFollowing"
              :initial-friend="isFriend"
              class="w-full justify-center"
              @click.stop
            />

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
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getFullAvatarUrl } from '@/utils/avatar'
import { getPublicUserProfile } from '@/api/auth'
import { getFollowStats } from '@/api/follow'
import FollowButton from '@/components/FollowButton.vue'

export default {
  name: 'UserProfileHoverCard',
  components: {
    FollowButton
  },
  props: {
    username: {
      type: String,
      required: true
    },
    // Optional: if you already have user data, pass it to avoid API call
    userData: {
      type: Object,
      default: null
    }
  },
  setup(props) {
    const store = useStore()
    const router = useRouter()
    const isVisible = ref(false)
    const loading = ref(false)
    const userInfo = ref(props.userData)
    const avatarError = ref(false)
    const cardStyle = ref({})
    const isFollowing = ref(false)
    const isFriend = ref(false)
    let hideTimeout = null

    const currentUser = computed(() => store.getters.currentUser)
    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    
    // Show follow button if not viewing own profile (show for both logged-in and non-logged-in users)
    const showFollowButton = computed(() => {
      return userInfo.value && 
             (!currentUser.value || currentUser.value.username !== props.username)
    })

    // Watch for userData prop changes (when hovering over different users)
    watch(() => props.userData, (newData) => {
      userInfo.value = newData
      // Reset follow status when user changes
      isFollowing.value = false
      isFriend.value = false
    })

    const displayName = computed(() => {
      return userInfo.value?.nickname || userInfo.value?.username || '用户'
    })

    const userInitial = computed(() => {
      const name = displayName.value
      return name ? name.charAt(0).toUpperCase() : 'U'
    })

    const calculateCardPosition = (event) => {
      const target = event.currentTarget
      const rect = target.getBoundingClientRect()
      const cardWidth = 288 // 72 * 4 = 288px (w-72)
      const cardHeight = 200 // Approximate height
      const padding = 8

      let top = rect.bottom + padding
      let left = rect.left

      // Check if card would go off right edge of screen
      if (left + cardWidth > window.innerWidth) {
        left = window.innerWidth - cardWidth - padding
      }

      // Check if card would go off bottom edge of screen
      if (top + cardHeight > window.innerHeight) {
        top = rect.top - cardHeight - padding
      }

      // Make sure we don't go off left edge
      if (left < padding) {
        left = padding
      }

      // Make sure we don't go off top edge
      if (top < padding) {
        top = rect.bottom + padding
      }

      cardStyle.value = {
        top: `${top}px`,
        left: `${left}px`
      }
    }

    const handleMouseEnter = (event) => {
      if (hideTimeout) {
        clearTimeout(hideTimeout)
        hideTimeout = null
      }

      calculateCardPosition(event)
      showCard()
    }

    const showCard = async () => {
      isVisible.value = true

      // Check if user is current user
      if (props.username === currentUser.value?.username) {
        // Show current user's info from store
        // Merge data from currentUser if userInfo doesn't exist or is missing bio field
        if (!userInfo.value || userInfo.value.bio === undefined) {
          userInfo.value = {
            ...userInfo.value, // Keep existing data (like avatar from props)
            ...currentUser.value, // Merge complete user info including bio
            avatarUrl: getFullAvatarUrl(currentUser.value?.avatarUrl || userInfo.value?.avatarUrl)
          }
        } else if (userInfo.value.avatarUrl && !userInfo.value.avatarUrl.startsWith('http')) {
          userInfo.value.avatarUrl = getFullAvatarUrl(userInfo.value.avatarUrl)
        }
      } else {
        // Fetch public profile for other users
        // Check if we need to fetch: no data, different user, or missing bio field (incomplete data)
        if (!userInfo.value || 
            userInfo.value.username !== props.username || 
            userInfo.value.bio === undefined) {
          loading.value = true
          try {
            const publicProfile = await getPublicUserProfile(props.username)
            // Merge existing data (like avatar) with newly fetched data to prevent flicker
            userInfo.value = {
              ...userInfo.value, // Keep existing data like avatar
              ...publicProfile,
              avatarUrl: getFullAvatarUrl(publicProfile.avatarUrl)
            }
            
            // Fetch follow status if user is logged in and has user id
            if (isLoggedIn.value && userInfo.value.id) {
              try {
                const followStats = await getFollowStats(userInfo.value.id)
                isFollowing.value = followStats.isFollowing || false
                isFriend.value = followStats.isFriend || false
              } catch (followError) {
                console.error('Failed to load follow status:', followError)
                // Don't block the card from showing
              }
            }
          } catch (error) {
            console.error('Failed to load public profile:', error)
            // If fetch fails but we have basic data (name/avatar), don't hide the card
            if (!userInfo.value) {
              isVisible.value = false
            }
          } finally {
            loading.value = false
          }
        } else if (isLoggedIn.value && userInfo.value.id) {
          // User data exists but might need to fetch follow status
          try {
            const followStats = await getFollowStats(userInfo.value.id)
            isFollowing.value = followStats.isFollowing || false
            isFriend.value = followStats.isFriend || false
          } catch (followError) {
            console.error('Failed to load follow status:', followError)
          }
        }
      }
    }

    const cancelHide = () => {
      if (hideTimeout) {
        clearTimeout(hideTimeout)
        hideTimeout = null
      }
    }

    const hideCard = () => {
      hideTimeout = setTimeout(() => {
        isVisible.value = false
      }, 200)
    }

    const goToProfile = () => {
      // Navigate to user's profile page
      // If it's the current user, go to /profile, otherwise go to /user/:username
      if (props.username === currentUser.value?.username) {
        router.push('/profile')
      } else {
        router.push(`/user/${props.username}`)
      }
      // Hide the card after navigation
      isVisible.value = false
    }

    onMounted(() => {
      // Listen for scroll events to hide card
      window.addEventListener('scroll', hideCard, true)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('scroll', hideCard, true)
      if (hideTimeout) {
        clearTimeout(hideTimeout)
      }
    })

    return {
      isVisible,
      loading,
      userInfo,
      avatarError,
      cardStyle,
      displayName,
      userInitial,
      showFollowButton,
      isFollowing,
      isFriend,
      handleMouseEnter,
      showCard,
      cancelHide,
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
