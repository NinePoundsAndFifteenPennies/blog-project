<template>
  <div class="card card-hover cursor-pointer" @click="goToDetail">
    <!-- 封面图 -->
    <div
        class="h-48 bg-gradient-to-br from-primary-400 to-secondary-500 relative overflow-hidden"
    >
      <div class="absolute inset-0 bg-gradient-to-t from-black/30 to-transparent"></div>
    </div>

    <!-- 内容区域 -->
    <div class="p-6">
      <!-- 标题 -->
      <h3 class="text-xl font-bold text-gray-900 mb-3 line-clamp-2 hover:text-primary-600 transition-colors duration-200">
        {{ post.title }}
      </h3>

      <!-- 摘要 -->
      <p class="text-gray-600 mb-4 line-clamp-3 leading-relaxed">
        {{ post.summary || '暂无摘要' }}
      </p>

      <!-- 底部信息栏 -->
      <div class="flex items-center justify-between pt-4 border-t border-gray-100">
        <!-- 作者信息 -->
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full bg-gradient-primary flex items-center justify-center text-white font-semibold">
            {{ authorInitial }}
          </div>
          <div>
            <p class="text-sm font-medium text-gray-900">{{ post.author?.username || '匿名' }}</p>
            <p class="text-xs text-gray-500">{{ formatDate(post.createdAt) }}</p>
          </div>
        </div>

        <!-- 统计信息(仅显示图标,暂无实际数据) -->
        <div class="flex items-center space-x-4 text-sm text-gray-400">
          <!-- 浏览量图标 -->
          <div class="flex items-center space-x-1" title="浏览量(功能开发中)">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
          </div>

          <!-- 点赞数图标 -->
          <div class="flex items-center space-x-1" title="点赞(功能开发中)">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
          </div>

          <!-- 评论数图标 -->
          <div class="flex items-center space-x-1" title="评论(功能开发中)">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'PostCard',
  props: {
    post: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const router = useRouter()

    const authorInitial = computed(() => {
      // 如果 username 是空字符串也处理一下
      const name = props.post.author?.username || ''
      return name ? name.charAt(0).toUpperCase() : 'A'
    })

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

    const goToDetail = () => {
      router.push(`/post/${props.post.id}`)
    }

    return {
      authorInitial,
      formatDate,
      goToDetail
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
