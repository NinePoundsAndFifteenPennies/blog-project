<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-20 pb-12">
      <div class="container mx-auto px-4">
        <!-- Page Header -->
        <div class="max-w-7xl mx-auto mb-6">
          <div class="flex items-center justify-between">
            <h1 class="text-3xl font-bold text-gray-900">
              {{ isEditMode ? "编辑文章" : "创建文章" }}
            </h1>
            <router-link to="/" class="btn-ghost">
              <svg
                class="w-5 h-5 mr-1 inline"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
              取消
            </router-link>
          </div>
        </div>

        <!-- Editor Layout -->
        <div class="max-w-7xl mx-auto">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Editor Panel -->
            <div class="space-y-4">
              <!-- Title Input -->
              <div class="card p-6">
                <input
                  v-model="formData.title"
                  type="text"
                  placeholder="请输入文章标题..."
                  class="w-full text-3xl font-bold border-none outline-none placeholder-gray-300"
                  :class="{ 'text-red-500': errors.title }"
                />
                <p v-if="errors.title" class="mt-2 text-sm text-red-600">
                  {{ errors.title }}
                </p>
              </div>

              <!-- Content Editor -->
              <div class="card p-6">
                <div
                  class="mb-4 flex items-center justify-between border-b border-gray-200 pb-3"
                >
                  <span class="text-sm font-medium text-gray-700"
                    >内容 (支持Markdown)</span
                  >
                  <div
                    class="flex items-center space-x-2 text-sm text-gray-500"
                  >
                    <span>{{ wordCount }} 字</span>
                  </div>
                </div>
                <textarea
                  v-model="formData.content"
                  placeholder="开始写作... 支持Markdown语法"
                  class="w-full h-96 border-none outline-none resize-none font-mono text-sm leading-relaxed placeholder-gray-300"
                  :class="{ 'text-red-500': errors.content }"
                ></textarea>
                <p v-if="errors.content" class="mt-2 text-sm text-red-600">
                  {{ errors.content }}
                </p>
              </div>

              <!-- Tags Input -->
              <div class="card p-6">
                <label class="block text-sm font-medium text-gray-700 mb-3">
                  标签
                </label>
                <div class="flex flex-wrap gap-2 mb-3">
                  <span
                    v-for="(tag, index) in formData.tags"
                    :key="index"
                    class="tag flex items-center space-x-1"
                  >
                    <span>#{{ tag }}</span>
                    <button
                      @click="removeTag(index)"
                      class="hover:text-red-600"
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
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </button>
                  </span>
                </div>
                <div class="flex space-x-2">
                  <input
                    v-model="newTag"
                    type="text"
                    placeholder="添加标签 (按回车确认)"
                    class="flex-1 input-field"
                    @keyup.enter="addTag"
                  />
                  <button @click="addTag" class="btn-secondary">添加</button>
                </div>
              </div>

              <!-- Category Selection -->
              <div class="card p-6">
                <label class="block text-sm font-medium text-gray-700 mb-3">
                  分类
                </label>
                <select v-model="formData.category" class="input-field">
                  <option value="">请选择分类</option>
                  <option
                    v-for="category in categories"
                    :key="category"
                    :value="category"
                  >
                    {{ category }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Preview Panel -->
            <div class="lg:sticky lg:top-24 h-fit">
              <div class="card p-6">
                <div
                  class="flex items-center justify-between mb-4 pb-3 border-b border-gray-200"
                >
                  <h3 class="text-lg font-semibold text-gray-900">预览</h3>
                  <span class="text-sm text-gray-500">实时预览</span>
                </div>

                <!-- Preview Title -->
                <h1 class="text-3xl font-bold text-gray-900 mb-4">
                  {{ formData.title || "无标题" }}
                </h1>

                <!-- Preview Tags -->
                <div
                  v-if="formData.tags.length"
                  class="flex flex-wrap gap-2 mb-6"
                >
                  <span v-for="tag in formData.tags" :key="tag" class="tag">
                    #{{ tag }}
                  </span>
                </div>

                <!-- Preview Content -->
                <div
                  class="markdown-body prose max-w-none"
                  v-html="previewContent"
                ></div>

                <!-- Empty Preview -->
                <div
                  v-if="!formData.content"
                  class="text-center py-12 text-gray-400"
                >
                  <svg
                    class="w-16 h-16 mx-auto mb-3"
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
                  <p>内容预览将在这里显示</p>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="mt-4 space-y-3">
                <button
                  @click="handleSubmit(false)"
                  :disabled="loading"
                  class="w-full btn-primary flex items-center justify-center"
                >
                  <svg
                    v-if="!loading"
                    class="w-5 h-5 mr-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  <div v-else class="spinner w-5 h-5 mr-2"></div>
                  <span>{{
                    loading ? "发布中..." : isEditMode ? "更新文章" : "发布文章"
                  }}</span>
                </button>

                <button
                  @click="handleSubmit(true)"
                  :disabled="loading"
                  class="w-full btn-secondary flex items-center justify-center"
                >
                  <svg
                    class="w-5 h-5 mr-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"
                    />
                  </svg>
                  <span>保存草稿</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { marked } from "marked";
import Header from "@/components/Header.vue";
// import { getPostById, createPost, updatePost } from '@/api/posts'

export default {
  name: "PostEdit",
  components: {
    Header,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const loading = ref(false);
    const newTag = ref("");

    const formData = reactive({
      title: "",
      content: "",
      tags: [],
      category: "",
    });

    const errors = reactive({
      title: "",
      content: "",
    });

    const categories = ref([
      "前端开发",
      "后端开发",
      "数据库",
      "算法",
      "工具分享",
      "其他",
    ]);

    // 是否为编辑模式
    const isEditMode = computed(() => !!route.params.id);

    // 字数统计
    const wordCount = computed(() => {
      return formData.content.length;
    });

    // Markdown预览
    const previewContent = computed(() => {
      if (!formData.content) return "";
      return marked(formData.content);
    });

    // 添加标签
    const addTag = () => {
      const tag = newTag.value.trim();
      if (tag && !formData.tags.includes(tag)) {
        if (formData.tags.length >= 5) {
          alert("最多只能添加5个标签");
          return;
        }
        formData.tags.push(tag);
        newTag.value = "";
      }
    };

    // 移除标签
    const removeTag = (index) => {
      formData.tags.splice(index, 1);
    };

    // 表单验证
    const validateForm = () => {
      errors.title = "";
      errors.content = "";
      let isValid = true;

      if (!formData.title.trim()) {
        errors.title = "请输入文章标题";
        isValid = false;
      } else if (formData.title.length > 100) {
        errors.title = "标题不能超过100个字符";
        isValid = false;
      }

      if (!formData.content.trim()) {
        errors.content = "请输入文章内容";
        isValid = false;
      }

      return isValid;
    };

    // 加载文章数据(编辑模式)
    const loadPost = async () => {
      if (!isEditMode.value) return;

      try {
        const postId = route.params.id;
        // TODO: 调用API获取文章详情
        // const post = await getPostById(postId)

        // 模拟数据
        const post = {
          title: "Vue 3 实战教程",
          content: "# Vue 3 实战教程\n\n这是文章内容...",
          tags: ["Vue", "JavaScript"],
          category: "前端开发",
        };

        formData.title = post.title;
        formData.content = post.content;
        formData.tags = post.tags || [];
        formData.category = post.category || "";
      } catch (error) {
        console.error("加载文章失败:", error);
        alert("加载文章失败");
        router.push("/");
      }
    };

    // 提交表单
    const handleSubmit = async (isDraft = false) => {
      if (!isDraft && !validateForm()) {
        return;
      }

      loading.value = true;

      try {
        const postData = {
          title: formData.title,
          content: formData.content,
          tags: formData.tags,
          category: formData.category,
          isDraft,
        };

        if (isEditMode.value) {
          // TODO: 更新文章
          // await updatePost(route.params.id, postData)
          console.log("更新文章:", postData);
        } else {
          // TODO: 创建文章
          // const newPost = await createPost(postData)
          console.log("创建文章:", postData);
        }

        alert(
          isDraft
            ? "草稿保存成功!"
            : isEditMode.value
            ? "文章更新成功!"
            : "文章发布成功!"
        );
        router.push("/");
      } catch (error) {
        console.error("提交失败:", error);
        alert(error.response?.data?.message || "操作失败,请稍后重试");
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadPost();
    });

    return {
      loading,
      formData,
      errors,
      newTag,
      categories,
      isEditMode,
      wordCount,
      previewContent,
      addTag,
      removeTag,
      handleSubmit,
    };
  },
};
</script>

<style scoped>
textarea {
  font-family: "Monaco", "Menlo", "Ubuntu Mono", monospace;
}
</style>
