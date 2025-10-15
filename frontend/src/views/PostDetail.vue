<template>
  <div class="min-h-screen">
    <Header />

    <!-- Loading State -->
    <div v-if="loading" class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="animate-pulse space-y-6">
            <div class="h-10 bg-gradient-to-r from-gray-200 to-gray-300 rounded-xl w-3/4"></div>
            <div class="h-4 bg-gradient-to-r from-gray-200 to-gray-300 rounded w-1/4"></div>
            <div class="space-y-3 mt-8">
              <div class="h-4 bg-gradient-to-r from-gray-200 to-gray-300 rounded"></div>
              <div class="h-4 bg-gradient-to-r from-gray-200 to-gray-300 rounded"></div>
              <div class="h-4 bg-gradient-to-r from-gray-200 to-gray-300 rounded w-5/6"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Article Content -->
    <article v-else-if="post" class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Article Header -->
          <header class="mb-10 animate-fade-in">
            <h1 class="text-4xl md:text-6xl font-bold text-gray-900 mb-8 leading-tight bg-gradient-to-r from-gray-900 via-primary-600 to-purple-600 bg-clip-text text-transparent">
              {{ post.title }}
            </h1>

            <!-- Author Info Card -->
            <div class="card p-6 md:p-8 flex items-center justify-between flex-wrap gap-4 backdrop-blur-sm bg-white/90 animate-slide-up" style="animation-delay: 0.1s;">
              <div class="flex items-center space-x-4">
                <div 
                  class="w-14 h-14 rounded-full flex items-center justify-center text-white font-bold text-xl shadow-lg ring-4 ring-white overflow-hidden"
                  :class="post.author?.avatarUrl ? '' : 'bg-gradient-primary'"
                >
                  <img 
                    v-if="post.author?.avatarUrl" 
                    :src="post.author.avatarUrl" 
                    :alt="post.author.username || '匿名'"
                    class="w-full h-full object-cover"
                  />
                  <span v-else>{{ authorInitial }}</span>
                </div>
                <div>
                  <p class="font-bold text-lg text-gray-900">{{ post.author?.username || '匿名' }}</p>
                  <div class="flex items-center space-x-4 text-sm text-gray-500">
                    <span class="flex items-center">
                      <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      {{ formatDate(post.createdAt) }}
                    </span>
                    <span class="flex items-center">
                      <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      阅读时间 5 分钟
                    </span>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div v-if="isAuthor" class="flex items-center space-x-3">
                <router-link
                    :to="`/post/${post.id}/edit`"
                    class="btn-secondary flex items-center space-x-2 px-6"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                  <span>编辑</span>
                </router-link>
                <button
                    @click="handleDelete"
                    class="btn-ghost text-red-600 hover:bg-red-50 flex items-center space-x-2 px-6"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  <span>删除</span>
                </button>
              </div>
            </div>
          </header>

          <!-- Article Body -->
          <div class="card p-8 md:p-12 mb-8 backdrop-blur-sm bg-white/95 animate-slide-up" style="animation-delay: 0.2s;">
            <div class="markdown-body prose prose-lg max-w-none" v-html="renderedContent"></div>
          </div>

          <!-- Article Footer Actions -->
          <div class="card p-6 flex items-center justify-between backdrop-blur-sm bg-white/90 animate-slide-up" style="animation-delay: 0.3s;">
            <div class="flex items-center space-x-6 text-gray-400">
              <!-- Like Button (暂未实现) -->
              <button
                  class="flex items-center space-x-2 hover:text-red-500 transition-colors cursor-not-allowed"
                  title="点赞功能开发中"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                <span class="font-medium">点赞</span>
              </button>

              <!-- Comment Button (暂未实现) -->
              <button class="flex items-center space-x-2 hover:text-primary-500 transition-colors cursor-not-allowed" title="评论功能开发中">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                </svg>
                <span class="font-medium">评论</span>
              </button>
            </div>

            <!-- Share Button (暂未实现) -->
            <button class="btn-secondary flex items-center space-x-2 cursor-not-allowed" title="分享功能开发中">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
              </svg>
              <span>分享</span>
            </button>
          </div>
        </div>
      </div>
    </article>

    <!-- Error State -->
    <div v-else class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto text-center animate-fade-in">
          <div class="inline-flex items-center justify-center w-32 h-32 rounded-full bg-gradient-to-br from-primary-100 to-purple-100 mb-8 animate-float">
            <svg class="w-16 h-16 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-gray-900 mb-4">文章不存在</h2>
          <p class="text-gray-600 mb-8 text-lg">抱歉,您访问的文章可能已被删除</p>
          <router-link to="/" class="btn-primary inline-flex items-center space-x-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <span>返回首页</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Floating Action Bar -->
    <div class="fixed right-8 bottom-8 flex flex-col space-y-3 z-40">
      <!-- Back to Top -->
      <button
          v-if="showBackToTop"
          @click="scrollToTop"
          class="w-14 h-14 bg-white/90 backdrop-blur-sm rounded-full shadow-glow flex items-center justify-center hover:bg-gradient-primary hover:text-white hover:shadow-glow-lg transition-all duration-300 transform hover:scale-110 group"
          title="回到顶部"
      >
        <svg class="w-6 h-6 text-gray-700 group-hover:text-white transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { marked } from 'marked'
import Header from '@/components/Header.vue'
import { getPostById, deletePost } from '@/api/posts'

export default {
  name: 'PostDetail',
  components: {
    Header
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const loading = ref(true)
    const post = ref(null)
    const showBackToTop = ref(false)

    const currentUser = computed(() => store.getters.currentUser)
    const isAuthor = computed(() => {
      return currentUser.value && post.value?.authorUsername === currentUser.value.username
    })

    const authorInitial = computed(() => {
      return post.value?.authorUsername?.charAt(0).toUpperCase() || 'A'
    })

    // Markdown渲染
    const renderedContent = computed(() => {
      if (!post.value?.content) return ''
      return marked(post.value.content)
    })

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    // 加载文章详情
    const loadPost = async () => {
      loading.value = true
      try {
        const postId = route.params.id
        // 调用API获取文章详情
        const response = await getPostById(postId)

        // 转换后端数据格式
        post.value = {
          id: response.id,
          title: response.title,
          content: response.content,
          authorUsername: response.authorUsername,
          author: {
            username: response.authorUsername
          },
          createdAt: response.createdAt,
          updatedAt: response.updatedAt
        }
      } catch (error) {
        console.error('加载文章失败:', error)
        post.value = null
      } finally {
        loading.value = false
      }
    }

    // 删除文章
    const handleDelete = async () => {
      if (!confirm('确定要删除这篇文章吗?此操作不可恢复。')) return

      try {
        await deletePost(post.value.id)
        alert('删除成功')
        router.push('/')
      } catch (error) {
        console.error('删除文章失败:', error)
        alert('删除失败,请稍后重试')
      }
    }

    // 回到顶部
    const scrollToTop = () => {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // 监听滚动
    const handleScroll = () => {
      showBackToTop.value = window.scrollY > 300
    }

    onMounted(() => {
      loadPost()
      window.addEventListener('scroll', handleScroll)
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
    })

    return {
      loading,
      post,
      isAuthor,
      authorInitial,
      renderedContent,
      showBackToTop,
      formatDate,
      handleDelete,
      scrollToTop
    }
  }
}
</script>