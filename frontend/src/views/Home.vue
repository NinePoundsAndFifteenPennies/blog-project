<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <!-- Hero Section -->
    <section class="pt-24 pb-12 bg-gradient-to-br from-primary-50 to-white">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-5xl font-bold mb-6 bg-gradient-primary bg-clip-text text-transparent animate-fade-in">
            分享你的技术见解
          </h1>
          <p class="text-xl text-gray-600 mb-8 animate-slide-up">
            在这里记录学习、分享经验、交流技术
          </p>
          <router-link
              v-if="!isLoggedIn"
              to="/register"
              class="btn-primary inline-block animate-slide-up"
          >
            立即开始写作
          </router-link>
          <router-link
              v-else
              to="/post/create"
              class="btn-primary inline-block animate-slide-up"
          >
            <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            写新文章
          </router-link>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <section class="py-12">
      <div class="container mx-auto px-4">
        <div class="max-w-5xl mx-auto">
          <!-- 文章统计 -->
          <div v-if="totalElements > 0" class="mb-6 text-center text-gray-600">
            共找到 <span class="font-semibold text-primary-600">{{ totalElements }}</span> 篇文章
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="flex justify-center items-center py-20">
            <div class="spinner w-12 h-12"></div>
          </div>

          <!-- Empty State -->
          <div v-else-if="!posts.length" class="text-center py-20">
            <svg class="w-24 h-24 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h3 class="text-xl font-semibold text-gray-700 mb-2">暂无文章</h3>
            <p class="text-gray-500 mb-6">成为第一个发布文章的人吧!</p>
            <router-link v-if="isLoggedIn" to="/post/create" class="btn-primary">
              写第一篇文章
            </router-link>
          </div>

          <!-- Posts Grid -->
          <div v-else class="space-y-6">
            <PostCard
                v-for="post in posts"
                :key="post.id"
                :post="post"
            />
          </div>

          <!-- Pagination -->
          <Pagination
              v-if="totalPages > 1"
              :current-page="currentPage"
              :total-pages="totalPages"
              @page-change="handlePageChange"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import PostCard from '@/components/PostCard.vue'
import Pagination from '@/components/Pagination.vue'
import { getPosts } from '@/api/posts'

export default {
  name: 'Home',
  components: {
    Header,
    PostCard,
    Pagination
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    const loading = ref(false)
    const posts = ref([])
    const currentPage = ref(1)
    const totalPages = ref(1)
    const totalElements = ref(0)
    const pageSize = 6 // 每页显示6篇文章

    const isLoggedIn = computed(() => store.getters.isLoggedIn)

    // 加载文章列表
    const loadPosts = async () => {
      loading.value = true
      try {
        // 调用API获取文章列表 (page从0开始)
        const response = await getPosts({
          page: currentPage.value - 1,
          size: pageSize
        })

        // 处理后端返回的Spring Data格式
        const contentArray = response.content || []

        // 转换数据格式以适配前端组件
        posts.value = contentArray.map(post => ({
          id: post.id,
          title: post.title,
          content: post.content,
          summary: post.content ? post.content.substring(0, 150).replace(/[#*`\n]/g, '') : '',
          author: {
            username: post.authorUsername
          },
          createdAt: post.createdAt,
          updatedAt: post.updatedAt,
          // 暂时显示静态数据,后续实现
          views: 0,
          likes: 0,
          comments: 0,
          tags: [],
          category: null
        }))

        totalPages.value = response.totalPages || 1
        totalElements.value = response.totalElements || 0

      } catch (error) {
        console.error('加载文章失败:', error)
        posts.value = []
      } finally {
        loading.value = false
      }
    }

    // 分页切换
    const handlePageChange = (page) => {
      currentPage.value = page
      loadPosts()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    onMounted(() => {
      loadPosts()
    })

    return {
      loading,
      posts,
      currentPage,
      totalPages,
      totalElements,
      isLoggedIn,
      handlePageChange
    }
  }
}
</script>