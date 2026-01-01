<template>
  <div 
    class="group bg-white rounded-2xl p-6 shadow-soft hover:shadow-soft-lg border border-transparent hover:border-gray-100 transition-all duration-300 cursor-pointer flex flex-col h-full"
    @click="goToDetail"
  >
    <!-- Top section: Date and first tag -->
    <div class="flex items-center justify-between text-xs text-gray-400 mb-4">
      <span class="flex items-center gap-1.5 bg-gray-50 px-2.5 py-1 rounded-lg">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        {{ displayDate }}
      </span>
      <span v-if="post.tags && post.tags.length" class="text-primary-600 font-medium">
        #{{ post.tags[0].name }}
      </span>
    </div>

    <!-- Title -->
    <h3 class="text-xl font-bold text-gray-800 mb-3 group-hover:text-primary-600 transition-colors line-clamp-2 leading-snug">
      {{ post.title }}
    </h3>

    <!-- Summary -->
    <p class="text-gray-500 text-sm leading-relaxed mb-6 flex-grow line-clamp-3">
      {{ post.summary || '暂无摘要...' }}
    </p>

    <!-- Footer section -->
    <div class="flex items-center justify-between pt-4 border-t border-gray-50 mt-auto">
      <!-- Author info -->
      <div class="flex items-center space-x-2.5">
        <UserProfileHoverCard 
          v-if="post.authorUsername"
          :username="post.authorUsername"
          :user-data="authorData"
        >
          <div 
            class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-semibold shadow-sm overflow-hidden bg-gradient-to-br from-primary-500 to-primary-700"
          >
            <img 
              v-if="displayAvatarUrl && !avatarLoadError" 
              :src="displayAvatarUrl" 
              :alt="authorDisplayName"
              :key="displayAvatarUrl"
              class="w-full h-full object-cover"
              @error="handleAvatarError"
              @load="handleAvatarLoad"
            />
            <span v-else>{{ authorInitial }}</span>
          </div>
        </UserProfileHoverCard>
        <div 
          v-else
          class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-semibold shadow-sm overflow-hidden bg-gradient-to-br from-primary-500 to-primary-700"
        >
          <span>{{ authorInitial }}</span>
        </div>
        <span class="text-sm text-gray-600 font-medium">{{ authorDisplayName }}</span>
      </div>
      
      <!-- Stats -->
      <div class="flex items-center space-x-4 text-gray-400 text-sm">
        <button 
          @click.stop="handleLike"
          class="flex items-center space-x-1 hover:text-red-500 transition-colors"
          :class="{ 'text-red-500': post.isLiked }"
          :title="post.isLiked ? '取消点赞' : '点赞'"
        >
          <svg 
            class="w-4 h-4" 
            :fill="post.isLiked ? 'currentColor' : 'none'" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
          <span>{{ post.likeCount || 0 }}</span>
        </button>
        <div class="flex items-center space-x-1 hover:text-primary-500 transition-colors" :title="`${post.commentCount || 0} 条评论`">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
          <span>{{ post.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { likePost, unlikePost } from '@/api/likes'
import { ref, computed, watch } from 'vue'
import { getFullAvatarUrl } from '@/utils/avatar'
import UserProfileHoverCard from '@/components/UserProfileHoverCard.vue'

export default {
  name: 'PostCard',
  components: {
    UserProfileHoverCard
  },
  props: {
    post: {
      type: Object,
      required: true
    }
  },
  emits: ['like-changed'],
  setup(props, { emit }) {
    const router = useRouter()
    const store = useStore()
    const avatarLoadError = ref(false)
    
    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    const currentUser = computed(() => store.getters.currentUser)
    
    // Check if the post author is the current user
    const isCurrentUser = computed(() => {
      return currentUser.value?.username === props.post.authorUsername
    })
    
    // Use current user's avatar if author is current user, otherwise use post author's avatar
    const displayAvatarUrl = computed(() => {
      if (isCurrentUser.value && currentUser.value?.avatarUrl) {
        return getFullAvatarUrl(currentUser.value.avatarUrl)
      }
      return props.post.authorAvatarUrl
    })
    
    // Reset avatar error when avatar URL changes
    watch(displayAvatarUrl, () => {
      avatarLoadError.value = false
    })

    const authorInitial = computed(() => {
      // Backend returns authorNickname as flat field, not nested
      const name = props.post.authorNickname || props.post.authorUsername || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
    })

    const authorDisplayName = computed(() => {
      // Backend returns authorNickname as flat field on post object
      return props.post.authorNickname || props.post.authorUsername || '匿名'
    })

    const authorData = computed(() => {
      // Backend returns flat fields on post object
      return {
        username: props.post.authorUsername,
        nickname: props.post.authorNickname,
        avatarUrl: displayAvatarUrl.value
      }
    })

    const handleAvatarError = () => {
      avatarLoadError.value = true
    }

    const handleAvatarLoad = () => {
      avatarLoadError.value = false
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
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

    const displayDate = computed(() => {
      // Show updatedAt if exists and different from publishedAt/createdAt, otherwise show publishedAt or createdAt
      if (props.post?.updatedAt) {
        return formatDate(props.post.updatedAt)
      }
      return formatDate(props.post?.publishedAt || props.post?.createdAt)
    })

    const goToDetail = () => {
      router.push(`/post/${props.post.id}`)
    }

    // 点赞功能
    const handleLike = async () => {
      // 检查是否登录
      if (!isLoggedIn.value) {
        // 未登录，跳转到登录页面
        router.push({
          path: '/login',
          query: {
            redirect: `/post/${props.post.id}`,
            message: '请先登录后再点赞'
          }
        })
        return
      }

      try {
        let response
        if (props.post.isLiked) {
          // 已点赞，取消点赞
          response = await unlikePost(props.post.id)
        } else {
          // 未点赞，点赞
          response = await likePost(props.post.id)
        }

        // 更新本地状态
        emit('like-changed', {
          postId: props.post.id,
          likeCount: response.likeCount,
          isLiked: response.liked
        })
      } catch (error) {
        console.error('点赞操作失败:', error)
        // 可以添加用户提示
      }
    }

    return {
      authorInitial,
      authorDisplayName,
      authorData,
      avatarLoadError,
      displayAvatarUrl,
      displayDate,
      goToDetail,
      handleAvatarError,
      handleAvatarLoad,
      handleLike
    }
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
