<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <!-- Profile Header -->
          <div class="card p-8 mb-6">
            <div class="flex flex-col md:flex-row items-center md:items-start space-y-4 md:space-y-0 md:space-x-6">
              <!-- Avatar -->
              <div class="w-24 h-24 rounded-full bg-gradient-primary flex items-center justify-center text-white text-3xl font-bold shadow-lg">
                {{ userInitial }}
              </div>

              <!-- User Info -->
              <div class="flex-1 text-center md:text-left">
                <h1 class="text-3xl font-bold text-gray-900 mb-2">{{ currentUser?.username }}</h1>
                <p class="text-gray-600 mb-4">{{ currentUser?.email }}</p>

                <!-- Stats -->
                <div class="flex items-center justify-center md:justify-start space-x-6 text-sm">
                  <div>
                    <span class="font-semibold text-gray-900">{{ userStats.posts }}</span>
                    <span class="text-gray-600 ml-1">文章</span>
                  </div>
                </div>
              </div>

              <!-- Edit Button -->
              <button class="btn-secondary">
                <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                编辑资料
              </button>
            </div>
          </div>

          <!-- Tabs (暂时隐藏草稿箱) -->
          <div class="card mb-6">
            <div class="flex border-b border-gray-200">
              <button
                  class="px-6 py-4 font-medium text-primary-600 border-b-2 border-primary-600"
              >
                我的文章
              </button>
            </div>
          </div>

          <!-- Content -->
          <div v-if="loading" class="text-center py-12">
            <div class="spinner w-12 h-12 mx-auto"></div>
          </div>

          <!-- Posts List -->
          <div v-else-if="posts.length" class="space-y-4">
            <div
                v-for="post in posts"
                :key="post.id"
                class="card p-6 hover:shadow-card-hover transition-shadow duration-200"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <router-link
                      :to="`/post/${post.id}`"
                      class="text-xl font-bold text-gray-900 hover:text-primary-600 transition-colors duration-200"
                  >
                    {{ post.title }}
                  </router-link>
                  <p class="text-gray-600 mt-2 line-clamp-2">{{ post.summary }}</p>

                  <div class="flex items-center space-x-6 mt-4 text-sm text-gray-500">
                    <span>{{ formatDate(post.createdAt) }}</span>
                  </div>
                </div>

                <div class="flex items-center space-x-2 ml-4">
                  <router-link
                      :to="`/post/${post.id}/edit`"
                      class="p-2 text-gray-600 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors duration-200"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </router-link>
                  <button
                      @click="handleDelete(post.id)"
                      class="p-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors duration-200"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="card p-12 text-center">
            <svg class="w-20 h-20 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h3 class="text-xl font-semibold text-gray-700 mb-2">还没有发布文章</h3>
            <p class="text-gray-500 mb-6">开始创作你的第一篇文章吧!</p>
            <router-link to="/post/create" class="btn-primary inline-block">
              <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              创建文章
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import Header from '@/components/Header.vue'
import { getPosts, deletePost } from '@/api/posts'

export default {
  name: 'Profile',
  components: {
    Header
  },
  setup() {
    const store = useStore()

    const loading = ref(false)
    const posts = ref([])

    const currentUser = computed(() => store.getters.currentUser)
    const userInitial = computed(() => {
      return currentUser.value?.username?.charAt(0).toUpperCase() || 'U'
    })

    const userStats = reactive({
      posts: 0
    })

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }

    // 加载用户文章
    const loadPosts = async () => {
      loading.value = true
      try {
        // 直接调用 /api/posts 获取所有文章,前端过滤当前用户的文章
        const response = await getPosts({ page: 0, size: 100 })

        // 过滤出当前用户的文章
        const allPosts = response.content || []
        const myPosts = allPosts.filter(
            post => post.authorUsername === currentUser.value.username
        )

        posts.value = myPosts.map(post => ({
          id: post.id,
          title: post.title,
          summary: post.content ? post.content.substring(0, 100).replace(/[#*`\n]/g, '') : '',
          createdAt: post.createdAt
        }))

        // 更新统计数据
        userStats.posts = posts.value.length
      } catch (error) {
        console.error('加载文章失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 删除文章
    const handleDelete = async (postId) => {
      if (!confirm('确定要删除这篇文章吗?')) return

      try {
        await deletePost(postId)
        posts.value = posts.value.filter(post => post.id !== postId)
        userStats.posts = posts.value.length
        alert('删除成功')
      } catch (error) {
        console.error('删除失败:', error)
        alert('删除失败,请稍后重试')
      }
    }

    onMounted(() => {
      loadPosts()
    })

    return {
      loading,
      posts,
      currentUser,
      userInitial,
      userStats,
      formatDate,
      handleDelete
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
</style>