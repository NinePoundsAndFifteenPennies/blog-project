<template>
  <button 
    v-if="!isOwnProfile"
    @click="toggleFollow" 
    :disabled="loading"
    :class="buttonClass"
    :title="hoverText"
    class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2"
  >
    <span v-if="loading" class="flex items-center">
      <svg class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      处理中...
    </span>
    <template v-else>
      <svg v-if="isFriend" class="w-4 h-4 mr-1.5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
      <svg v-else-if="isFollowing" class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
      </svg>
      <svg v-else class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      {{ buttonText }}
    </template>
  </button>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { followUser, unfollowUser } from '@/api/follow'

export default {
  name: 'FollowButton',
  props: {
    userId: {
      type: Number,
      required: true
    },
    initialFollowing: {
      type: Boolean,
      default: false
    },
    initialFriend: {
      type: Boolean,
      default: false
    }
  },
  emits: ['follow-change'],
  setup(props, { emit }) {
    const store = useStore()
    const router = useRouter()
    const loading = ref(false)
    const isFollowing = ref(props.initialFollowing)
    const isFriend = ref(props.initialFriend)

    const currentUser = computed(() => store.getters.currentUser)
    const isLoggedIn = computed(() => store.getters.isLoggedIn)

    const isOwnProfile = computed(() => {
      return currentUser.value && currentUser.value.id === props.userId
    })

    const buttonText = computed(() => {
      if (isFriend.value) return '互相关注'
      if (isFollowing.value) return '已关注'
      return '关注'
    })

    const hoverText = computed(() => {
      if (isFriend.value || isFollowing.value) return '点击取消关注'
      return '点击关注'
    })

    const buttonClass = computed(() => {
      if (isFriend.value) {
        return 'bg-green-500 hover:bg-green-600 text-white focus:ring-green-500'
      }
      if (isFollowing.value) {
        return 'bg-gray-200 hover:bg-gray-300 text-gray-700 focus:ring-gray-400'
      }
      return 'bg-primary-600 hover:bg-primary-700 text-white focus:ring-primary-500'
    })

    const toggleFollow = async () => {
      if (!isLoggedIn.value) {
        // 未登录，跳转到登录页
        router.push({ 
          path: '/login', 
          query: { redirect: router.currentRoute.value.fullPath } 
        })
        return
      }

      loading.value = true
      
      // 乐观更新
      const wasFollowing = isFollowing.value
      const wasFriend = isFriend.value
      isFollowing.value = !wasFollowing
      if (!isFollowing.value) {
        isFriend.value = false
      }

      try {
        let result
        if (wasFollowing) {
          result = await unfollowUser(props.userId)
        } else {
          result = await followUser(props.userId)
        }
        
        // 更新状态
        isFollowing.value = result.following
        isFriend.value = result.friend
        
        // 触发事件通知父组件
        emit('follow-change', {
          following: result.following,
          friend: result.friend
        })
      } catch (error) {
        // 回滚
        isFollowing.value = wasFollowing
        isFriend.value = wasFriend
        
        const message = error.response?.data?.message || '操作失败，请稍后重试'
        alert(message)
      } finally {
        loading.value = false
      }
    }

    // 使用 Composition API watch 监听 props 变化
    watch(() => props.initialFollowing, (newVal) => {
      isFollowing.value = newVal
    })

    watch(() => props.initialFriend, (newVal) => {
      isFriend.value = newVal
    })

    return {
      loading,
      isFollowing,
      isFriend,
      isOwnProfile,
      buttonText,
      hoverText,
      buttonClass,
      toggleFollow
    }
  }
}
</script>
