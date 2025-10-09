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
                  <div>
                    <span class="font-semibold text-gray-900">{{ userStats.drafts }}</span>
                    <span class="text-gray-600 ml-1">草稿</span>
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

          <!-- Tabs -->
          <div class="card mb-6">
            <div class="flex border-b border-gray-200">
              <button
                  :class="['px-6 py-4 font-medium', activeTab === 'posts' ? 'text-primary-600 border-b-2 border-primary-600' : 'text-gray-600']"
                  @click="activeTab = 'posts'"
              >
                我的文章 ({{ userStats.posts }})
              </button>
              <button
                  :class="['px-6 py-4 font-medium', activeTab === 'drafts' ? 'text-primary-600 border-b-2 border-primary-600' : 'text-gray-600']"
                  @click="activeTab = 'drafts'"
              >
                草稿箱 ({{ userStats.drafts }})
              </button>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="text-center py-12">
            <div class="spinner w-12 h-12 mx-auto"></div>
          </div>

          <!-- Posts List -->
          <div v-else-if="visibleList.length" class="space-y-4">
            <div
                v-for="post in visibleList"
                :key="post.id"
                class="card p-6 hover:shadow-card-hover transition-shadow duration-200"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-2">
                    <router-link
                        :to="`/post/${post.id}`"
                        class="text-xl font-bold text-gray-900 hover:text-primary-600 transition-colors duration-200"
                    >
                      {{ post.title }}
                    </router-link>
                    <span v-if="post.draft" class="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 rounded-full">
                      草稿
                    </span>
                  </div>

                  <p class="text-gray-600 mt-2 line-clamp-2">{{ post.summary }}</p>

                  <div class="flex items-center space-x-6 mt-4 text-sm text-gray-500">
                    <span>创建: {{ formatDate(post.createdAt) }}</span>
                    <span v-if="!post.draft && post.publishedAt">发布: {{ formatDate(post.publishedAt) }}</span>
                    <span v-if="post.updatedAt">更新: {{ formatDate(post.updatedAt) }}</span>
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
            <h3 class="text-xl font-semibold text-gray-700 mb-2">
              {{ activeTab === 'drafts' ? '草稿箱为空' : '还没有发布文章' }}
            </h3>
            <p class="text-gray-500 mb-6">
              {{ activeTab === 'drafts' ? '你还没有草稿，去创作第一篇草稿吧！' : '开始创作你的第一篇文章吧!' }}
            </p>
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
import { getMyPosts, deletePost } from '@/api/posts'

export default {
  name: 'Profile',
  components: { Header },
  setup() {
    const store = useStore()
    const loading = ref(false)
    const posts = ref([])
    const activeTab = ref('posts')

    const currentUser = computed(() => store.getters.currentUser)
    const userInitial = computed(() => currentUser.value?.username?.charAt(0).toUpperCase() || 'U')

    const userStats = reactive({ posts: 0, drafts: 0 })

    const formatDate = (str) => {
      if (!str) return ''
      const date = new Date(str)
      return date.toLocaleString('zh-CN', { hour12: false })
    }

    const loadPosts = async () => {
      if (!currentUser.value) return
      loading.value = true
      try {
        const res = await getMyPosts({ page: 0, size: 100 })
        const myPosts = res.content || []

        posts.value = myPosts.map(p => ({
          ...p,
          summary: p.content?.replace(/[#*`\n]/g, '').slice(0, 100) || ''
        }))

        // 调试：输出所有文章和草稿状态
        console.log('当前用户的所有文章（包括草稿）:', myPosts.length)
        console.log('文章列表详情:', posts.value.map(p => ({ 
          id: p.id, 
          title: p.title, 
          draft: p.draft,
          draftType: typeof p.draft 
        })))

        userStats.drafts = posts.value.filter(p => p.draft).length
        userStats.posts = posts.value.filter(p => !p.draft).length
        
        console.log('统计 - 草稿数:', userStats.drafts, '已发布:', userStats.posts)
      } catch (e) {
        console.error('加载失败:', e)
      } finally {
        loading.value = false
      }
    }

    const handleDelete = async (id) => {
      if (!confirm('确定删除该文章吗？')) return
      try {
        await deletePost(id)
        posts.value = posts.value.filter(p => p.id !== id)
        userStats.drafts = posts.value.filter(p => p.draft).length
        userStats.posts = posts.value.filter(p => !p.draft).length
        alert('删除成功')
      } catch (e) {
        console.error(e)
        alert('删除失败')
      }
    }

    const visibleList = computed(() =>
        activeTab.value === 'drafts' ? posts.value.filter(p => p.draft) : posts.value.filter(p => !p.draft)
    )

    onMounted(loadPosts)

    return { loading, posts, userStats, currentUser, userInitial, formatDate, handleDelete, activeTab, visibleList }
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
