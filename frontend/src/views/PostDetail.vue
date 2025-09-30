<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <!-- Loading State -->
    <div v-if="loading" class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="animate-pulse">
            <div class="h-8 bg-gray-200 rounded w-3/4 mb-4"></div>
            <div class="h-4 bg-gray-200 rounded w-1/4 mb-8"></div>
            <div class="space-y-3">
              <div class="h-4 bg-gray-200 rounded"></div>
              <div class="h-4 bg-gray-200 rounded"></div>
              <div class="h-4 bg-gray-200 rounded w-5/6"></div>
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
          <header class="mb-8">
            <h1
              class="text-4xl md:text-5xl font-bold text-gray-900 mb-6 leading-tight"
            >
              {{ post.title }}
            </h1>

            <!-- Author Info Card -->
            <div
              class="card p-6 flex items-center justify-between flex-wrap gap-4"
            >
              <div class="flex items-center space-x-4">
                <div
                  class="w-12 h-12 rounded-full bg-gradient-primary flex items-center justify-center text-white font-semibold text-lg"
                >
                  {{ authorInitial }}
                </div>
                <div>
                  <p class="font-semibold text-gray-900">
                    {{ post.author?.username || "匿名" }}
                  </p>
                  <div
                    class="flex items-center space-x-3 text-sm text-gray-500"
                  >
                    <span class="flex items-center">
                      <svg
                        class="w-4 h-4 mr-1"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                      {{ formatDate(post.createdAt) }}
                    </span>
                    <span class="flex items-center">
                      <svg
                        class="w-4 h-4 mr-1"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                        />
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                        />
                      </svg>
                      {{ post.views || 0 }} 次阅读
                    </span>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div v-if="isAuthor" class="flex items-center space-x-2">
                <router-link
                  :to="`/post/${post.id}/edit`"
                  class="btn-ghost flex items-center space-x-1"
                >
                  <svg
                    class="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                    />
                  </svg>
                  <span>编辑</span>
                </router-link>
                <button
                  @click="handleDelete"
                  class="btn-ghost text-red-600 hover:bg-red-50 flex items-center space-x-1"
                >
                  <svg
                    class="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                  <span>删除</span>
                </button>
              </div>
            </div>

            <!-- Tags -->
            <div
              v-if="post.tags && post.tags.length"
              class="flex flex-wrap gap-2 mt-4"
            >
              <span v-for="tag in post.tags" :key="tag" class="tag">
                #{{ tag }}
              </span>
            </div>
          </header>

          <!-- Article Body -->
          <div class="card p-8 md:p-12 mb-8">
            <div
              class="markdown-body prose prose-lg max-w-none"
              v-html="renderedContent"
            ></div>
          </div>

          <!-- Article Footer Actions -->
          <div class="card p-6 flex items-center justify-between">
            <div class="flex items-center space-x-6">
              <!-- Like Button -->
              <button
                @click="handleLike"
                class="flex items-center space-x-2 group"
                :class="[
                  isLiked ? 'text-red-500' : 'text-gray-600 hover:text-red-500',
                ]"
              >
                <svg
                  class="w-6 h-6 transition-transform group-hover:scale-110"
                  :fill="isLiked ? 'currentColor' : 'none'"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                  />
                </svg>
                <span class="font-medium">{{ post.likes || 0 }}</span>
              </button>

              <!-- Comment Button -->
              <button
                class="flex items-center space-x-2 text-gray-600 hover:text-primary-600 group"
              >
                <svg
                  class="w-6 h-6 transition-transform group-hover:scale-110"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                  />
                </svg>
                <span class="font-medium">{{ post.comments || 0 }}</span>
              </button>
            </div>

            <!-- Share Button -->
            <button class="btn-ghost flex items-center space-x-2">
              <svg
                class="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"
                />
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
        <div class="max-w-4xl mx-auto text-center">
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
          <h2 class="text-2xl font-bold text-gray-900 mb-2">文章不存在</h2>
          <p class="text-gray-600 mb-6">抱歉,您访问的文章可能已被删除</p>
          <router-link to="/" class="btn-primary"> 返回首页 </router-link>
        </div>
      </div>
    </div>

    <!-- Floating Action Bar -->
    <div class="fixed right-8 bottom-8 flex flex-col space-y-3">
      <!-- Back to Top -->
      <button
        v-if="showBackToTop"
        @click="scrollToTop"
        class="w-12 h-12 bg-white rounded-full shadow-lg flex items-center justify-center hover:bg-primary-50 hover:text-primary-600 transition-all duration-200"
      >
        <svg
          class="w-6 h-6"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M5 10l7-7m0 0l7 7m-7-7v18"
          />
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useStore } from "vuex";
import { marked } from "marked";
import Header from "@/components/Header.vue";
// import { getPostById, deletePost } from '@/api/posts'

export default {
  name: "PostDetail",
  components: {
    Header,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const store = useStore();

    const loading = ref(true);
    const post = ref(null);
    const isLiked = ref(false);
    const showBackToTop = ref(false);

    const currentUser = computed(() => store.getters.currentUser);
    const isAuthor = computed(() => {
      return (
        currentUser.value && post.value?.author?.id === currentUser.value.id
      );
    });

    const authorInitial = computed(() => {
      return post.value?.author?.username?.charAt(0).toUpperCase() || "A";
    });

    // Markdown渲染
    const renderedContent = computed(() => {
      if (!post.value?.content) return "";
      return marked(post.value.content);
    });

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return "";
      const date = new Date(dateString);
      return date.toLocaleDateString("zh-CN", {
        year: "numeric",
        month: "long",
        day: "numeric",
      });
    };

    // 加载文章详情
    const loadPost = async () => {
      loading.value = true;
      try {
        const postId = route.params.id;
        // TODO: 调用API获取文章详情
        // post.value = await getPostById(postId)

        // 模拟数据
        await new Promise((resolve) => setTimeout(resolve, 500));
        post.value = {
          id: postId,
          title: "Vue 3 Composition API 深度解析",
          content: `# Vue 3 Composition API 深度解析

## 什么是 Composition API?

Composition API 是 Vue 3 中引入的一组新的 API,它为组件逻辑的组织和复用提供了更灵活的方式。

## 核心概念

### 1. setup 函数

\`setup\` 是 Composition API 的入口点:

\`\`\`javascript
export default {
  setup() {
    // 组件逻辑
    return {
      // 暴露给模板的数据和方法
    }
  }
}
\`\`\`

### 2. 响应式数据

使用 \`ref\` 和 \`reactive\` 创建响应式数据:

\`\`\`javascript
import { ref, reactive } from 'vue'

const count = ref(0)
const state = reactive({ name: 'Vue' })
\`\`\`

## 优势

- **更好的类型推导**: 天然支持 TypeScript
- **更灵活的代码组织**: 按功能组织而非选项
- **更容易复用逻辑**: 通过组合函数实现

> 💡 **提示**: Composition API 不是 Options API 的替代品,而是一种补充。

## 总结

Composition API 为大型应用提供了更好的可维护性和可扩展性。`,
          author: {
            id: 1,
            username: "techwriter",
          },
          tags: ["Vue", "JavaScript", "Web开发"],
          views: 1234,
          likes: 89,
          comments: 12,
          createdAt: new Date().toISOString(),
        };
      } catch (error) {
        console.error("加载文章失败:", error);
        post.value = null;
      } finally {
        loading.value = false;
      }
    };

    // 点赞
    const handleLike = () => {
      // TODO: 调用API点赞
      isLiked.value = !isLiked.value;
      if (isLiked.value) {
        post.value.likes++;
      } else {
        post.value.likes--;
      }
    };

    // 删除文章
    const handleDelete = async () => {
      if (!confirm("确定要删除这篇文章吗?此操作不可恢复。")) return;

      try {
        // TODO: 调用API删除文章
        // await deletePost(post.value.id)
        router.push("/");
      } catch (error) {
        console.error("删除文章失败:", error);
        alert("删除失败,请稍后重试");
      }
    };

    // 回到顶部
    const scrollToTop = () => {
      window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // 监听滚动
    const handleScroll = () => {
      showBackToTop.value = window.scrollY > 300;
    };

    onMounted(() => {
      loadPost();
      window.addEventListener("scroll", handleScroll);
    });

    onUnmounted(() => {
      window.removeEventListener("scroll", handleScroll);
    });

    return {
      loading,
      post,
      isLiked,
      isAuthor,
      authorInitial,
      renderedContent,
      showBackToTop,
      formatDate,
      handleLike,
      handleDelete,
      scrollToTop,
    };
  },
};
</script>
