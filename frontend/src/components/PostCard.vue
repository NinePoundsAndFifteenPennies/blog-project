<template>
  <div class="card card-hover cursor-pointer group" @click="goToDetail">
    <!-- 封面图with gradient overlay -->
    <div class="relative h-52 bg-gradient-to-br from-primary-400 via-purple-500 to-pink-500 overflow-hidden">
      <!-- Animated gradient overlay -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent group-hover:from-black/60 transition-all duration-300"></div>
      
      <!-- Decorative shapes -->
      <div class="absolute top-4 right-4 w-20 h-20 bg-white/10 rounded-full blur-xl"></div>
      <div class="absolute bottom-4 left-4 w-16 h-16 bg-white/10 rounded-full blur-lg"></div>
      
      <!-- Category badge (if available) -->
      <div class="absolute top-4 left-4">
        <span class="px-3 py-1 bg-white/20 backdrop-blur-sm text-white text-xs font-semibold rounded-full border border-white/30">
          技术分享
        </span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="p-6">
      <!-- 标题 -->
      <h3 class="text-xl font-bold text-gray-900 mb-3 line-clamp-2 group-hover:text-primary-600 transition-colors duration-200 leading-tight">
        {{ post.title }}
      </h3>

      <!-- 摘要 -->
      <p class="text-gray-600 mb-4 line-clamp-3 leading-relaxed text-sm">
        {{ post.summary || '暂无摘要' }}
      </p>

      <!-- 底部信息栏 -->
      <div class="flex items-center justify-between pt-4 border-t border-gray-100">
        <!-- 作者信息 -->
        <div class="flex items-center space-x-3">
          <div 
            class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-gradient-primary"
          >
            <img 
              v-if="post.author?.avatarUrl && !avatarLoadError" 
              :src="post.author.avatarUrl" 
              :alt="post.author.username"
              class="w-full h-full object-cover"
              @error="handleAvatarError"
              @load="handleAvatarLoad"
            />
            <span v-else>{{ authorInitial }}</span>
          </div>
          <div>
            <p class="text-sm font-semibold text-gray-900">{{ post.author?.username || '匿名' }}</p>
            <p class="text-xs text-gray-500" :title="titleAttr">
              {{ dateLabel }} {{ displayDate }}
            </p>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="flex items-center space-x-3 text-sm text-gray-400">
          <!-- 浏览量图标 -->
          <div class="flex items-center space-x-1 hover:text-primary-500 transition-colors" title="浏览量(功能开发中)">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
          </div>

          <!-- 点赞数 -->
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

          <!-- 评论数图标 -->
          <div class="flex items-center space-x-1 hover:text-primary-500 transition-colors" :title="`${post.commentCount || 0} 条评论`">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
            </svg>
            <span>{{ post.commentCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { likePost, unlikePost } from '@/api/likes'
import { ref, reactive, computed, onMounted, watch } from 'vue' // 1. 导入 watch

export default {
  name: 'PostCard',
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
    watch(() => props.post.author?.avatarUrl, () => {
      avatarLoadError.value = false
    })
    const isLoggedIn = computed(() => store.getters.isLoggedIn)

    const authorInitial = computed(() => {
      const name = props.post.author?.username || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
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

    const formatFullDate = (dateString) => {
      if (!dateString) return ''
      try {
        return new Date(dateString).toLocaleString('zh-CN')
      } catch (e) {
        return dateString
      }
    }

    const displayDate = computed(() => {
      // Show updatedAt if exists and different from publishedAt/createdAt, otherwise show publishedAt or createdAt
      if (props.post?.updatedAt) {
        return formatDate(props.post.updatedAt)
      }
      return formatDate(props.post?.publishedAt || props.post?.createdAt)
    })

    const dateLabel = computed(() => {
      // If updatedAt exists and is different, show "更新于"
      if (props.post?.updatedAt) {
        return '更新于'
      } else if (props.post?.publishedAt) {
        return '发布于'
      }
      return '创建于'
    })

    const titleAttr = computed(() => {
      const created = formatFullDate(props.post?.createdAt)
      const published = formatFullDate(props.post?.publishedAt)
      const updated = formatFullDate(props.post?.updatedAt)
      
      let tooltip = `创建：${created}`
      if (props.post?.publishedAt && props.post.publishedAt !== props.post.createdAt) {
        tooltip += `\n发布：${published}`
      }
      if (props.post?.updatedAt) {
        tooltip += `\n更新：${updated}`
      }
      return tooltip
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
      avatarLoadError,
      formatDate,
      displayDate,
      dateLabel,
      titleAttr,
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
