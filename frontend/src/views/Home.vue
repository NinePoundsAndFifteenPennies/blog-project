<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <!-- Hero Section -->
    <section class="pt-24 pb-12 bg-gradient-to-br from-primary-50 to-white">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto text-center">
          <h1
            class="text-5xl font-bold mb-6 bg-gradient-primary bg-clip-text text-transparent animate-fade-in"
          >
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
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <section class="py-12">
      <div class="container mx-auto px-4">
        <div class="flex flex-col lg:flex-row gap-8">
          <!-- Sidebar -->
          <aside class="lg:w-64 flex-shrink-0">
            <div class="sticky top-24 space-y-6">
              <!-- 分类筛选 -->
              <div class="card p-6">
                <h3 class="text-lg font-bold mb-4 flex items-center">
                  <svg
                    class="w-5 h-5 mr-2 text-primary-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
                    />
                  </svg>
                  分类
                </h3>
                <div class="space-y-2">
                  <button
                    @click="selectCategory(null)"
                    class="w-full text-left px-3 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                    :class="{
                      'bg-primary-50 text-primary-700 font-medium':
                        selectedCategory === null,
                    }"
                  >
                    全部
                  </button>
                  <button
                    v-for="category in categories"
                    :key="category"
                    @click="selectCategory(category)"
                    class="w-full text-left px-3 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                    :class="{
                      'bg-primary-50 text-primary-700 font-medium':
                        selectedCategory === category,
                    }"
                  >
                    {{ category }}
                  </button>
                </div>
              </div>

              <!-- 热门标签 -->
              <div class="card p-6">
                <h3 class="text-lg font-bold mb-4 flex items-center">
                  <svg
                    class="w-5 h-5 mr-2 text-primary-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"
                    />
                  </svg>
                  热门标签
                </h3>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="tag in popularTags"
                    :key="tag"
                    class="tag cursor-pointer hover:bg-primary-200 transition-colors duration-200"
                    @click="searchByTag(tag)"
                  >
                    #{{ tag }}
                  </span>
                </div>
              </div>
            </div>
          </aside>

          <!-- Main Content Area -->
          <main class="flex-1">
            <!-- Loading State -->
            <div v-if="loading" class="flex justify-center items-center py-20">
              <div class="spinner w-12 h-12"></div>
            </div>

            <!-- Empty State -->
            <div v-else-if="!posts.length" class="text-center py-20">
              <svg
                class="w-24 h-24 mx-auto text-gray-300 mb-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
              <h3 class="text-xl font-semibold text-gray-700 mb-2">暂无文章</h3>
              <p class="text-gray-500 mb-6">成为第一个发布文章的人吧!</p>
              <router-link
                v-if="isLoggedIn"
                to="/post/create"
                class="btn-primary"
              >
                写第一篇文章
              </router-link>
            </div>

            <!-- Posts Grid -->
            <div v-else class="space-y-6">
              <PostCard v-for="post in posts" :key="post.id" :post="post" />
            </div>

            <!-- Pagination -->
            <Pagination
              v-if="totalPages > 1"
              :current-page="currentPage"
              :total-pages="totalPages"
              @page-change="handlePageChange"
            />
          </main>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import { useStore } from "vuex";
import { useRoute, useRouter } from "vue-router";
import Header from "@/components/Header.vue";
import PostCard from "@/components/PostCard.vue";
import Pagination from '../components/Pagination.vue';
// import { getPosts } from '@/api/posts'

export default {
  name: "Home",
  components: {
    Header,
    PostCard,
    Pagination,
  },
  setup() {
    const store = useStore();
    const route = useRoute();
    const router = useRouter();

    const loading = ref(false);
    const posts = ref([]);
    const currentPage = ref(1);
    const totalPages = ref(1);
    const selectedCategory = ref(null);

    const isLoggedIn = computed(() => store.getters.isLoggedIn);

    // 模拟分类数据
    const categories = ref([
      "前端开发",
      "后端开发",
      "数据库",
      "算法",
      "工具分享",
    ]);

    // 模拟热门标签
    const popularTags = ref([
      "Vue",
      "React",
      "JavaScript",
      "Python",
      "Java",
      "MySQL",
      "Docker",
    ]);

    // 加载文章列表
    const loadPosts = async () => {
      loading.value = true;
      try {
        // TODO: 调用API获取文章列表
        // const response = await getPosts({
        //   page: currentPage.value,
        //   category: selectedCategory.value,
        //   keyword: route.query.keyword
        // })
        // posts.value = response.data
        // totalPages.value = response.totalPages

        // 模拟数据 - 您需要替换为实际API调用
        await new Promise((resolve) => setTimeout(resolve, 800));
        posts.value = generateMockPosts();
        totalPages.value = 5;
      } catch (error) {
        console.error("加载文章失败:", error);
      } finally {
        loading.value = false;
      }
    };

    // 生成模拟文章数据
    const generateMockPosts = () => {
      return Array.from({ length: 6 }, (_, i) => ({
        id: i + 1,
        title: `Vue 3 实战教程 ${i + 1} - 构建现代化Web应用`,
        content:
          "Vue 3 带来了许多令人兴奋的新特性,包括Composition API、更好的TypeScript支持等。本文将带你深入了解这些新特性...",
        summary: "深入探讨Vue 3的新特性与最佳实践",
        author: {
          username: `user${i + 1}`,
        },
        category: categories.value[i % categories.value.length],
        tags: ["Vue", "JavaScript", "Web开发"],
        views: Math.floor(Math.random() * 1000),
        likes: Math.floor(Math.random() * 100),
        comments: Math.floor(Math.random() * 50),
        createdAt: new Date(Date.now() - i * 86400000).toISOString(),
      }));
    };

    // 切换分类
    const selectCategory = (category) => {
      selectedCategory.value = category;
      currentPage.value = 1;
      loadPosts();
    };

    // 标签搜索
    const searchByTag = (tag) => {
      router.push({ path: "/", query: { keyword: tag } });
      currentPage.value = 1;
      loadPosts();
    };

    // 分页切换
    const handlePageChange = (page) => {
      currentPage.value = page;
      loadPosts();
      window.scrollTo({ top: 0, behavior: "smooth" });
    };

    onMounted(() => {
      loadPosts();
    });

    return {
      loading,
      posts,
      currentPage,
      totalPages,
      selectedCategory,
      categories,
      popularTags,
      isLoggedIn,
      selectCategory,
      searchByTag,
      handlePageChange,
    };
  },
};
</script>
