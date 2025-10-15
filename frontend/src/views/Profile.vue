<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <!-- Profile Header -->
          <div class="card p-8 mb-8 backdrop-blur-sm bg-white/90 animate-fade-in">
            <div class="flex flex-col md:flex-row items-center md:items-start space-y-6 md:space-y-0 md:space-x-8">
              <!-- Avatar -->
              <div class="relative group">
                <!-- Avatar Image or Initial -->
                <div 
                  class="w-32 h-32 rounded-full flex items-center justify-center text-white text-5xl font-bold shadow-glow-lg ring-8 ring-white transform hover:scale-110 transition-transform duration-300 cursor-pointer overflow-hidden"
                  :class="currentUser?.avatarUrl ? '' : 'bg-gradient-primary'"
                  @click="triggerFileInput"
                >
                  <img 
                    v-if="currentUser?.avatarUrl" 
                    :src="currentUser.avatarUrl" 
                    :alt="currentUser.username"
                    :key="currentUser.avatarUrl"
                    class="w-full h-full object-cover"
                  />
                  <span v-else>{{ userInitial }}</span>
                </div>
                
                <!-- Upload Overlay -->
                <div 
                  v-if="!uploadingAvatar"
                  class="absolute inset-0 w-32 h-32 rounded-full bg-black bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 cursor-pointer"
                  @click="triggerFileInput"
                >
                  <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </div>

                <!-- Uploading Indicator -->
                <div 
                  v-if="uploadingAvatar"
                  class="absolute inset-0 w-32 h-32 rounded-full bg-black bg-opacity-70 flex items-center justify-center"
                >
                  <div class="spinner w-10 h-10 border-white"></div>
                </div>

                <!-- Hidden File Input -->
                <input 
                  ref="fileInput"
                  type="file" 
                  accept="image/jpeg,image/png,image/jpg"
                  class="hidden"
                  @change="handleFileSelect"
                />

                <!-- Only show checkmark badge if avatar exists -->
                <div v-if="currentUser?.avatarUrl" class="absolute -bottom-2 -right-2 w-12 h-12 bg-green-500 rounded-full border-4 border-white flex items-center justify-center shadow-lg">
                  <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </div>

              <!-- User Info -->
              <div class="flex-1 text-center md:text-left">
                <h1 class="text-4xl font-bold bg-gradient-primary bg-clip-text text-transparent mb-3">{{ currentUser?.username }}</h1>
                <p class="text-gray-600 text-lg mb-6">{{ currentUser?.email }}</p>

                <!-- Stats -->
                <div class="flex items-center justify-center md:justify-start space-x-8">
                  <div class="text-center">
                    <div class="text-3xl font-bold text-primary-600 mb-1">{{ userStats.posts }}</div>
                    <div class="text-sm text-gray-600 font-medium">文章</div>
                  </div>
                  <div class="w-px h-12 bg-gray-200"></div>
                  <div class="text-center">
                    <div class="text-3xl font-bold text-orange-600 mb-1">{{ userStats.drafts }}</div>
                    <div class="text-sm text-gray-600 font-medium">草稿</div>
                  </div>
                  <div class="w-px h-12 bg-gray-200"></div>
                  <div class="text-center">
                    <div class="text-3xl font-bold text-purple-600 mb-1">0</div>
                    <div class="text-sm text-gray-600 font-medium">粉丝</div>
                  </div>
                </div>
              </div>

              <!-- Edit Button -->
              <button class="btn-secondary shrink-0">
                <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                编辑资料
              </button>
            </div>
          </div>

          <!-- Tabs -->
          <div class="card mb-8 backdrop-blur-sm bg-white/90 animate-slide-up" style="animation-delay: 0.1s;">
            <div class="flex border-b border-gray-200">
              <button
                  :class="['px-8 py-4 font-semibold transition-all duration-200 relative', 
                    activeTab === 'posts' ? 'text-primary-600' : 'text-gray-600 hover:text-gray-900']"
                  @click="activeTab = 'posts'"
              >
                <span>我的文章</span>
                <span class="ml-2 px-2.5 py-0.5 text-xs font-bold rounded-full" 
                      :class="activeTab === 'posts' ? 'bg-primary-100 text-primary-700' : 'bg-gray-100 text-gray-600'">
                  {{ userStats.posts }}
                </span>
                <div v-if="activeTab === 'posts'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-primary"></div>
              </button>
              <button
                  :class="['px-8 py-4 font-semibold transition-all duration-200 relative', 
                    activeTab === 'drafts' ? 'text-primary-600' : 'text-gray-600 hover:text-gray-900']"
                  @click="activeTab = 'drafts'"
              >
                <span>草稿箱</span>
                <span class="ml-2 px-2.5 py-0.5 text-xs font-bold rounded-full" 
                      :class="activeTab === 'drafts' ? 'bg-primary-100 text-primary-700' : 'bg-gray-100 text-gray-600'">
                  {{ userStats.drafts }}
                </span>
                <div v-if="activeTab === 'drafts'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-primary"></div>
              </button>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="text-center py-20">
            <div class="spinner w-16 h-16 mx-auto"></div>
            <p class="text-gray-600 mt-4">加载中...</p>
          </div>

          <!-- Posts List -->
          <div v-else-if="visibleList.length" class="space-y-4 animate-slide-up" style="animation-delay: 0.2s;">
            <div
                v-for="post in visibleList"
                :key="post.id"
                class="card p-6 hover:shadow-glow transition-all duration-300 group backdrop-blur-sm bg-white/90"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center space-x-3 mb-3">
                    <router-link
                        :to="`/post/${post.id}`"
                        class="text-xl font-bold text-gray-900 group-hover:text-primary-600 transition-colors duration-200"
                    >
                      {{ post.title }}
                    </router-link>
                    <span v-if="post.draft" class="px-3 py-1 text-xs font-bold bg-gradient-to-r from-yellow-100 to-orange-100 text-orange-700 rounded-full shadow-sm">
                      草稿
                    </span>
                  </div>

                  <p class="text-gray-600 mt-2 line-clamp-2 text-sm">{{ post.summary }}</p>

                  <div class="flex items-center space-x-6 mt-4 text-sm text-gray-500">
                    <span class="flex items-center">
                      <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      创建: {{ formatDate(post.createdAt) }}
                    </span>
                    <span v-if="!post.draft && post.publishedAt" class="flex items-center">
                      <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      发布: {{ formatDate(post.publishedAt) }}
                    </span>
                    <span v-if="post.updatedAt" class="flex items-center">
                      <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                      </svg>
                      更新: {{ formatDate(post.updatedAt) }}
                    </span>
                  </div>
                </div>

                <div class="flex items-center space-x-2 ml-6">
                  <router-link
                      :to="`/post/${post.id}/edit`"
                      class="p-3 text-gray-600 hover:text-primary-600 hover:bg-gradient-to-br hover:from-primary-50 hover:to-purple-50 rounded-xl transition-all duration-200 transform hover:scale-110"
                      title="编辑"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </router-link>
                  <button
                      @click="handleDelete(post.id)"
                      class="p-3 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all duration-200 transform hover:scale-110"
                      title="删除"
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
          <div v-else class="card p-16 text-center backdrop-blur-sm bg-white/90 animate-scale-in">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gradient-to-br from-primary-100 to-purple-100 mb-6 animate-float">
              <svg class="w-12 h-12 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-700 mb-3">
              {{ activeTab === 'drafts' ? '草稿箱为空' : '还没有发布文章' }}
            </h3>
            <p class="text-gray-500 mb-8 text-lg">
              {{ activeTab === 'drafts' ? '你还没有草稿，去创作第一篇草稿吧！' : '开始创作你的第一篇文章吧!' }}
            </p>
            <router-link to="/post/create" class="btn-primary inline-flex items-center space-x-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              <span>创建文章</span>
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
import { uploadAvatar, updateAvatar, saveUserAvatar } from '@/api/files'

export default {
  name: 'Profile',
  components: { Header },
  setup() {
    const store = useStore()
    const loading = ref(false)
    const posts = ref([])
    const activeTab = ref('posts')
    const fileInput = ref(null)
    const uploadingAvatar = ref(false)

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

        userStats.drafts = posts.value.filter(p => p.draft).length
        userStats.posts = posts.value.filter(p => !p.draft).length
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

    const triggerFileInput = () => {
      if (fileInput.value) {
        fileInput.value.click()
      }
    }

    const validateImageFile = (file) => {
      // 检查文件类型
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png']
      if (!validTypes.includes(file.type)) {
        throw new Error('只支持 JPG 和 PNG 格式的图片')
      }

      // 检查文件大小 (5MB)
      const maxSize = 5 * 1024 * 1024
      if (file.size > maxSize) {
        throw new Error('图片大小不能超过 5MB')
      }
    }

    const handleFileSelect = async (event) => {
      const file = event.target.files[0]
      if (!file) return

      try {
        // 验证文件
        validateImageFile(file)

        uploadingAvatar.value = true

        // 判断用户是否已有头像，使用不同的接口
        let avatarUrl
        if (currentUser.value?.avatarUrl) {
          // 更新头像
          avatarUrl = await updateAvatar(file)
        } else {
          // 首次上传头像
          avatarUrl = await uploadAvatar(file)
        }

        // 保存头像URL到用户信息
        await saveUserAvatar(avatarUrl)

        // 更新Vuex store中的用户头像
        store.commit('UPDATE_USER_AVATAR', avatarUrl)

        // 重新获取用户信息以确保状态同步
        await store.dispatch('fetchCurrentUser')

        alert('头像更新成功！')
      } catch (error) {
        console.error('头像上传失败:', error)
        const errorMessage = error.response?.data || error.message || '头像上传失败，请重试'
        alert(errorMessage)
      } finally {
        uploadingAvatar.value = false
        // 清空文件输入，允许重新选择相同文件
        if (fileInput.value) {
          fileInput.value.value = ''
        }
      }
    }

    const visibleList = computed(() =>
        activeTab.value === 'drafts' ? posts.value.filter(p => p.draft) : posts.value.filter(p => !p.draft)
    )

    onMounted(loadPosts)

    return { 
      loading, 
      posts, 
      userStats, 
      currentUser, 
      userInitial, 
      formatDate, 
      handleDelete, 
      activeTab, 
      visibleList,
      fileInput,
      uploadingAvatar,
      triggerFileInput,
      handleFileSelect
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
