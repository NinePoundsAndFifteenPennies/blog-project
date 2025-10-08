<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-20 pb-12">
      <div class="container mx-auto px-4">
        <!-- Page Header -->
        <div class="max-w-7xl mx-auto mb-6">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">
                {{ isEditMode ? '编辑文章' : '创建文章' }}
              </h1>
              <p v-if="isDraft" class="text-sm text-yellow-600 mt-1">
                当前为草稿状态
              </p>
            </div>
            <router-link to="/" class="btn-ghost">
              <svg class="w-5 h-5 mr-1 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
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
                <p v-if="errors.title" class="mt-2 text-sm text-red-600">{{ errors.title }}</p>
              </div>

              <!-- Content Type Selector -->
              <div class="card p-6">
                <label class="block text-sm font-medium text-gray-700 mb-3">
                  内容类型
                </label>
                <div class="flex space-x-4">
                  <button
                      @click="formData.contentType = 'MARKDOWN'"
                      type="button"
                      class="flex-1 px-4 py-3 rounded-lg border-2 transition-all duration-200"
                      :class="formData.contentType === 'MARKDOWN'
                        ? 'border-primary-500 bg-primary-50 text-primary-700'
                        : 'border-gray-200 hover:border-gray-300'"
                  >
                    <svg class="w-6 h-6 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                    </svg>
                    <span class="font-medium">Markdown</span>
                  </button>
                  <button
                      @click="formData.contentType = 'HTML'"
                      type="button"
                      class="flex-1 px-4 py-3 rounded-lg border-2 transition-all duration-200"
                      :class="formData.contentType === 'HTML'
                        ? 'border-primary-500 bg-primary-50 text-primary-700'
                        : 'border-gray-200 hover:border-gray-300'"
                  >
                    <svg class="w-6 h-6 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                    </svg>
                    <span class="font-medium">HTML</span>
                  </button>
                </div>
                <p class="mt-2 text-xs text-gray-500">
                  {{ formData.contentType === 'MARKDOWN' ? '支持 Markdown 语法，适合快速写作' : '直接编写 HTML 代码，更灵活的排版' }}
                </p>
              </div>

              <!-- Content Editor -->
              <div class="card p-6">
                <div class="mb-4 flex items-center justify-between border-b border-gray-200 pb-3">
                  <span class="text-sm font-medium text-gray-700">
                    内容 ({{ formData.contentType === 'MARKDOWN' ? 'Markdown' : 'HTML' }})
                  </span>
                  <div class="flex items-center space-x-4 text-sm text-gray-500">
                    <span>{{ wordCount }} 字</span>
                  </div>
                </div>
                <textarea
                    v-model="formData.content"
                    :placeholder="contentPlaceholder"
                    class="w-full h-96 border-none outline-none resize-none font-mono text-sm leading-relaxed placeholder-gray-300"
                    :class="{ 'text-red-500': errors.content }"
                ></textarea>
                <p v-if="errors.content" class="mt-2 text-sm text-red-600">{{ errors.content }}</p>
              </div>
            </div>

            <!-- Preview Panel -->
            <div class="lg:sticky lg:top-24 h-fit">
              <div class="card p-6">
                <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
                  <h3 class="text-lg font-semibold text-gray-900">预览</h3>
                  <span class="text-sm text-gray-500">实时预览</span>
                </div>

                <!-- Preview Title -->
                <h1 class="text-3xl font-bold text-gray-900 mb-6">
                  {{ formData.title || '无标题' }}
                </h1>

                <!-- Preview Content -->
                <div
                    v-if="formData.content"
                    class="markdown-body prose max-w-none"
                    v-html="previewContent"
                ></div>

                <!-- Empty Preview -->
                <div v-else class="text-center py-12 text-gray-400">
                  <svg class="w-16 h-16 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <p>内容预览将在这里显示</p>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="mt-4 space-y-3">
                <!-- 发布/更新按钮 -->
                <button
                    @click="handlePublish"
                    :disabled="loading"
                    class="w-full btn-primary flex items-center justify-center"
                >
                  <svg v-if="!loading" class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <div v-else class="spinner w-5 h-5 mr-2"></div>
                  <span>{{ publishButtonText }}</span>
                </button>

                <!-- 保存草稿按钮 -->
                <button
                    @click="saveDraft"
                    :disabled="loading || (!formData.title && !formData.content)"
                    class="w-full btn-secondary flex items-center justify-center"
                >
                  <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import Header from '@/components/Header.vue'
import { getPostById, createPost, updatePost } from '@/api/posts'

export default {
  name: 'PostEdit',
  components: {
    Header
  },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const loading = ref(false)

    const formData = reactive({
      title: '',
      content: '',
      contentType: 'MARKDOWN'  // 默认 Markdown
    })

    const errors = reactive({
      title: '',
      content: ''
    })

    // 是否为草稿状态（从后端加载）
    const isDraft = ref(false)

    // 是否为编辑模式
    const isEditMode = computed(() => !!route.params.id)

    // 字数统计
    const wordCount = computed(() => {
      return formData.content.length
    })

    // 内容占位符
    const contentPlaceholder = computed(() => {
      if (formData.contentType === 'MARKDOWN') {
        return '开始写作... 支持Markdown语法\n\n例如:\n# 标题\n**粗体** *斜体*\n- 列表项'
      } else {
        return '开始写作... 直接编写HTML代码\n\n例如:\n<h1>标题</h1>\n<p>段落内容</p>'
      }
    })

    // 发布按钮文本
    const publishButtonText = computed(() => {
      if (loading.value) return '处理中...'
      if (isDraft.value) return '发布文章'  // 草稿状态显示"发布"
      if (isEditMode.value) return '更新文章'
      return '发布文章'
    })

    // Markdown/HTML 预览
    const previewContent = computed(() => {
      if (!formData.content) return ''

      if (formData.contentType === 'MARKDOWN') {
        return marked(formData.content)
      } else {
        // HTML 直接返回
        return formData.content
      }
    })

    // 表单验证
    const validateForm = () => {
      errors.title = ''
      errors.content = ''
      let isValid = true

      if (!formData.title.trim()) {
        errors.title = '请输入文章标题'
        isValid = false
      } else if (formData.title.length > 100) {
        errors.title = '标题不能超过100个字符'
        isValid = false
      }

      if (!formData.content.trim()) {
        errors.content = '请输入文章内容'
        isValid = false
      }

      return isValid
    }

    // 加载文章数据(编辑模式)
    const loadPost = async () => {
      if (!isEditMode.value) return

      try {
        const postId = route.params.id
        const post = await getPostById(postId)

        if (!post) {
          throw new Error('未找到文章')
        }

        formData.title = post.title || ''
        formData.content = post.content || ''
        formData.contentType = post.contentType || 'MARKDOWN'
        isDraft.value = post.draft || false
      } catch (error) {
        console.error('加载文章失败:', error)
        alert('加载文章失败')
        router.push('/')
      }
    }

    // 保存草稿（draft = true）
    const saveDraft = async () => {
      if (!validateForm()) {
        return
      }

      loading.value = true

      try {
        const postData = {
          title: formData.title,
          content: formData.content,
          contentType: formData.contentType,
          draft: true  // 标记为草稿
        }

        if (isEditMode.value) {
          await updatePost(route.params.id, postData)
          alert('草稿保存成功!')
          isDraft.value = true
        } else {
          const createdPost = await createPost(postData)
          alert('草稿保存成功!')
          // 创建草稿后跳转到编辑页面
          router.push(`/post/${createdPost.id}/edit`)
        }
      } catch (error) {
        console.error('保存草稿失败:', error)
        alert(error?.response?.data?.message || '保存草稿失败,请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 发布文章（draft = false）
    const handlePublish = async () => {
      if (!validateForm()) {
        return
      }

      loading.value = true

      try {
        const postData = {
          title: formData.title,
          content: formData.content,
          contentType: formData.contentType,
          draft: false  // 标记为已发布
        }

        if (isEditMode.value) {
          await updatePost(route.params.id, postData)
          alert(isDraft.value ? '文章发布成功!' : '文章更新成功!')
        } else {
          await createPost(postData)
          alert('文章发布成功!')
        }

        router.push('/')
      } catch (error) {
        console.error('提交失败:', error)
        alert(error?.response?.data?.message || '操作失败,请稍后重试')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadPost()
    })

    return {
      loading,
      formData,
      errors,
      isDraft,
      isEditMode,
      wordCount,
      contentPlaceholder,
      publishButtonText,
      previewContent,
      saveDraft,
      handlePublish
    }
  }
}
</script>

<style scoped>
textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>