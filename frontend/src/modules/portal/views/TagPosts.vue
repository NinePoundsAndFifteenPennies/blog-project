<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-7xl mx-auto">
          <!-- Tag Header -->
          <div class="mb-8">
            <div v-if="loading" class="animate-pulse">
              <div class="h-10 bg-gray-200 rounded w-1/3 mb-4"></div>
              <div class="h-6 bg-gray-200 rounded w-2/3"></div>
            </div>
            <div v-else-if="tagInfo" class="flex items-center space-x-4">
              <div
                class="inline-flex items-center px-6 py-3 rounded-lg text-2xl font-bold"
                :style="getTagStyle(tagInfo)"
              >
                <i v-if="tagInfo.icon" :class="getIconClass(tagInfo.icon)" class="mr-2"></i>
                {{ tagInfo.name }}
              </div>
              <div class="text-gray-600">
                <span class="font-semibold">{{ tagInfo.postCount }}</span> 篇文章
              </div>
            </div>
            <p v-if="tagInfo && tagInfo.description" class="mt-4 text-gray-600">
              {{ tagInfo.description }}
            </p>
          </div>

          <!-- Back button -->
          <div class="mb-6">
            <router-link to="/" class="btn-ghost inline-flex items-center space-x-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              <span>返回首页</span>
            </router-link>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div v-for="i in 6" :key="i" class="animate-pulse">
              <div class="card p-6">
                <div class="h-48 bg-gray-200 rounded-lg mb-4"></div>
                <div class="h-6 bg-gray-200 rounded w-3/4 mb-3"></div>
                <div class="h-4 bg-gray-200 rounded w-full mb-2"></div>
                <div class="h-4 bg-gray-200 rounded w-5/6"></div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else-if="!posts.length" class="text-center py-20">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-primary-100 mb-6">
              <svg class="w-12 h-12 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-3">暂无文章</h3>
            <p class="text-gray-600 mb-8">这个标签下还没有文章</p>
            <router-link to="/" class="btn-primary">
              浏览其他文章
            </router-link>
          </div>

          <!-- Posts Grid -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <PostCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
              @like-changed="handleLikeChanged"
            />
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-12">
            <Pagination
              :current-page="currentPage"
              :total-pages="totalPages"
              @page-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/components/Header.vue'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'
import { getPosts } from '@/api/posts'
import { getTagByName } from '@/api/tags'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'TagPosts',
  components: {
    Header,
    PostCard,
    Pagination
  },
  setup() {
    const route = useRoute()

    const loading = ref(true)
    const posts = ref([])
    const tagInfo = ref(null)
    const currentPage = ref(0)
    const totalPages = ref(0)
    const totalElements = ref(0)

    const tagName = computed(() => route.params.tagName)

    const loadTagInfo = async () => {
      try {
        const response = await getTagByName(tagName.value)
        tagInfo.value = response
      } catch (error) {
        console.error('加载标签信息失败:', error)
        tagInfo.value = { name: tagName.value, postCount: 0 }
      }
    }

    const loadPosts = async (page = 0) => {
      loading.value = true
      try {
        // Note: This is a workaround since the backend doesn't have a direct tag filter endpoint
        // We fetch all posts and filter by tag on the frontend
        const response = await getPosts({
          page: page,
          size: 100 // Get more posts to filter
        })

        // Filter posts by tag
        const filteredPosts = response.content.filter(post => 
          post.tags && post.tags.some(tag => tag.name === tagName.value)
        )

        // Transform posts data
        posts.value = filteredPosts.map(post => ({
          ...post,  // 保留所有原始字段（包括 authorUsername, authorNickname, authorAvatarUrl 等）
          
          // 处理需要转换的字段
          summary: post.content ? post.content.substring(0, 150) + '...' : '',
          authorAvatarUrl: getFullAvatarUrl(post.authorAvatarUrl),  // 转换为完整URL
          
          // 确保这些字段有默认值
          likeCount: post.likeCount || 0,
          isLiked: post.isLiked || false,
          commentCount: post.commentCount || 0,
          tags: post.tags || []
        }))

        totalElements.value = filteredPosts.length
        currentPage.value = page
        totalPages.value = Math.ceil(filteredPosts.length / 10)
      } catch (error) {
        console.error('加载文章失败:', error)
        posts.value = []
      } finally {
        loading.value = false
      }
    }

    const getTagStyle = (tag) => {
      const color = tag.color || '#6B7280'
      
      const hexToRgb = (hex) => {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
        return result ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        } : { r: 107, g: 112, b: 128 }
      }
      
      const rgb = hexToRgb(color)
      
      return {
        backgroundColor: `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.15)`,
        color: color,
        border: `2px solid rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.3)`
      }
    }

    const handlePageChange = (page) => {
      loadPosts(page)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    const handleLikeChanged = ({ postId, likeCount, isLiked }) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        post.likeCount = likeCount
        post.isLiked = isLiked
      }
    }

    // 获取Font Awesome图标类名
    const getIconClass = (icon) => {
      if (!icon) return ''
      
      // 如果已经包含 fa-brands、fa-solid 等前缀，直接返回
      if (icon.includes('fa-brands') || icon.includes('fa-solid') || icon.includes('fa-regular') || 
          icon.includes('fab ') || icon.includes('fas ') || icon.includes('far ')) {
        return icon
      }
      
      // 如果只是 fa-xxx 格式，添加 fa-brands 前缀（大多数品牌图标）
      if (icon.startsWith('fa-')) {
        return `fa-brands ${icon}`
      }
      
      // 其他情况返回原值
      return icon
    }

    watch(tagName, () => {
      loadTagInfo()
      loadPosts()
    })

    onMounted(() => {
      loadTagInfo()
      loadPosts()
    })

    return {
      loading,
      posts,
      tagInfo,
      currentPage,
      totalPages,
      totalElements,
      getTagStyle,
      handlePageChange,
      handleLikeChanged,
      getIconClass
    }
  }
}
</script>
