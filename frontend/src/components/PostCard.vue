<template>
  <!-- List View Layout -->
  <div v-if="listView" class="card cursor-pointer group hover:shadow-lg transition-all duration-200" @click="goToDetail">
    <div class="p-6 flex gap-6">
      <!-- 封面图片 (左侧，较小) -->
      <div v-if="post.coverImageUrl" class="relative w-48 h-32 overflow-hidden rounded-lg flex-shrink-0">
        <img 
          :src="getFullImageUrl(post.coverImageUrl)" 
          :alt="post.title"
          class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
        />
      </div>

      <!-- 内容区域 (右侧，占据剩余空间) -->
      <div class="flex-1 min-w-0">
        <!-- 标题 -->
        <h3 class="text-2xl font-bold text-gray-900 mb-2 line-clamp-2 group-hover:text-primary-600 transition-colors duration-200">
          <span v-if="highlightKeyword" v-html="highlightText(post.title)"></span>
          <template v-else>{{ post.title }}</template>
        </h3>

        <!-- 摘要 -->
        <p class="text-gray-600 mb-3 line-clamp-2 leading-relaxed">
          <span v-if="highlightKeyword" v-html="highlightText(post.summary || '暂无摘要')"></span>
          <template v-else>{{ post.summary || '暂无摘要' }}</template>
        </p>

        <!-- 底部信息栏 -->
        <div class="flex items-center justify-between flex-wrap gap-4">
          <!-- 左侧：作者和时间 -->
          <div class="flex items-center space-x-3">
            <UserProfileHoverCard 
              v-if="post.authorUsername"
              :username="post.authorUsername"
              :user-data="authorData"
            >
              <div 
                class="w-8 h-8 rounded-full flex items-center justify-center text-white font-semibold shadow-sm overflow-hidden bg-primary-600"
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
                <span v-else class="text-xs">{{ authorInitial }}</span>
              </div>
            </UserProfileHoverCard>
            <div 
              v-else
              class="w-8 h-8 rounded-full flex items-center justify-center text-white font-semibold shadow-sm overflow-hidden bg-primary-600"
            >
              <span class="text-xs">{{ authorInitial }}</span>
            </div>
            <div class="text-sm">
              <span class="font-semibold text-gray-900">{{ authorDisplayName }}</span>
              <span class="text-gray-500 mx-2">·</span>
              <span class="text-gray-500">{{ dateLabel }} {{ displayDate }}</span>
            </div>
          </div>

          <!-- 右侧：标签和统计 -->
          <div class="flex items-center gap-4">
            <!-- 标签 -->
            <div v-if="post.tags && post.tags.length > 0" class="flex flex-wrap gap-2">
              <TagBadge
                v-for="tag in post.tags.slice(0, 2)"
                :key="tag.id"
                :tag="tag"
                :show-icon="false"
                :clickable="true"
                @click="handleTagClick(tag)"
              />
            </div>

            <!-- 统计信息 -->
            <div class="flex items-center space-x-4 text-sm text-gray-400">
              <!-- 浏览量 -->
              <div class="flex items-center space-x-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <span>{{ post.viewCount || 0 }}</span>
              </div>

              <!-- 点赞数 -->
              <button 
                @click.stop="handleLike"
                class="flex items-center space-x-1 hover:text-red-500 transition-colors"
                :class="{ 'text-red-500': post.isLiked }"
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

              <!-- 评论数 -->
              <div class="flex items-center space-x-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                </svg>
                <span>{{ post.commentCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Grid View Layout (Original) -->
  <div v-else class="card card-hover cursor-pointer group" @click="goToDetail">
    <!-- 头部区域 -->
    <div class="relative h-52 overflow-hidden">
      <!-- 封面图片 -->
      <img 
        v-if="post.coverImageUrl" 
        :src="getFullImageUrl(post.coverImageUrl)" 
        :alt="post.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
      />
      <!-- 渐变色背景（无封面图时显示） -->
      <div 
        v-else 
        class="w-full h-full bg-gradient-to-br from-primary-500 to-purple-600"
      ></div>
      <!-- Subtle overlay -->
      <div class="absolute inset-0 bg-black/5 group-hover:bg-black/10 transition-all duration-200"></div>
    </div>

    <!-- 内容区域 -->
    <div class="p-6">
      <!-- 标题 -->
      <h3 class="text-xl font-bold text-gray-900 mb-3 line-clamp-2 group-hover:text-primary-600 transition-colors duration-200 leading-tight">
        <span v-if="highlightKeyword" v-html="highlightText(post.title)"></span>
        <template v-else>{{ post.title }}</template>
      </h3>

      <!-- 标签 -->
      <div v-if="post.tags && post.tags.length > 0" class="flex flex-wrap gap-2 mb-3">
        <TagBadge
          v-for="tag in post.tags.slice(0, 3)"
          :key="tag.id"
          :tag="tag"
          :show-icon="true"
          :clickable="true"
          @click="handleTagClick(tag)"
        />
        <span v-if="post.tags.length > 3" class="text-xs text-gray-400">
          +{{ post.tags.length - 3 }}
        </span>
      </div>

      <!-- 摘要 -->
      <p class="text-gray-600 mb-4 line-clamp-3 leading-relaxed text-sm">
        <span v-if="highlightKeyword" v-html="highlightText(post.summary || '暂无摘要')"></span>
        <template v-else>{{ post.summary || '暂无摘要' }}</template>
      </p>

      <!-- 底部信息栏 -->
      <div class="flex items-center justify-between pt-4 border-t border-gray-100">
        <!-- 作者信息 -->
        <div class="flex items-center space-x-3">
          <UserProfileHoverCard 
            v-if="post.authorUsername"
            :username="post.authorUsername"
            :user-data="authorData"
          >
            <div 
              class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm overflow-hidden bg-primary-600"
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
            class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm overflow-hidden bg-primary-600"
          >
            <span>{{ authorInitial }}</span>
          </div>
          <div>
            <p class="text-sm font-semibold text-gray-900">{{ authorDisplayName }}</p>
            <p class="text-xs text-gray-500" :title="titleAttr">
              {{ dateLabel }} {{ displayDate }}
            </p>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="flex items-center space-x-3 text-sm text-gray-400">
          <!-- 浏览量 -->
          <div class="flex items-center space-x-1 hover:text-primary-500 transition-colors" :title="`${post.viewCount || 0} 次浏览`">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <span>{{ post.viewCount || 0 }}</span>
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
import { ref, computed, watch } from 'vue'
import { getFullAvatarUrl } from '@/utils/avatar'
import TagBadge from '@/components/TagBadge.vue'
import UserProfileHoverCard from '@/components/UserProfileHoverCard.vue'

export default {
  name: 'PostCard',
  components: {
    TagBadge,
    UserProfileHoverCard
  },
  props: {
    post: {
      type: Object,
      required: true
    },
    highlightKeyword: {
      type: String,
      default: ''
    },
    listView: {
      type: Boolean,
      default: false
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

    const getFullImageUrl = (url) => {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      // Remove leading slash if present and add base URL
      const cleanUrl = url.startsWith('/') ? url.substring(1) : url
      return `${window.location.origin}/${cleanUrl}`
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

    const handleTagClick = (tag) => {
      // Navigate to tag posts page
      router.push({
        path: '/tags/' + encodeURIComponent(tag.name)
      })
    }

    // HTML转义函数（防止XSS攻击）
    const escapeHtml = (str) => {
      const htmlEntities = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
      }
      return str.replace(/[&<>"']/g, char => htmlEntities[char])
    }

    // 转义特殊正则字符
    const escapeRegex = (str) => {
      return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    }

    // 高亮关键词
    const highlightText = (text) => {
      if (!text || !props.highlightKeyword) {
        return text
      }
      
      const escapedText = escapeHtml(text)
      const escapedKeyword = escapeRegex(props.highlightKeyword)
      
      // 创建不区分大小写的正则表达式
      const regex = new RegExp(`(${escapedKeyword})`, 'gi')
      
      // 用高亮样式替换匹配的关键词
      return escapedText.replace(regex, '<mark class="bg-yellow-200 text-yellow-900 px-0.5 rounded">$1</mark>')
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
      formatDate,
      displayDate,
      dateLabel,
      titleAttr,
      goToDetail,
      handleTagClick,
      handleAvatarError,
      handleAvatarLoad,
      handleLike,
      getFullImageUrl,
      highlightText
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
