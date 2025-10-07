<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-20 pb-12">
      <div class="container mx-auto px-4">
        <!-- Page Header -->
        <div class="max-w-7xl mx-auto mb-6">
          <div class="flex items-center justify-between">
            <h1 class="text-3xl font-bold text-gray-900">
              {{ isEditMode ? '编辑文章' : '创建文章' }}
            </h1>
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

              <!-- Content Editor -->
              <div class="card p-6">
                <div class="mb-4 flex items-center justify-between border-b border-gray-200 pb-3">
                  <span class="text-sm font-medium text-gray-700">内容 (支持Markdown)</span>
                  <div class="flex items-center space-x-2 text-sm text-gray-500">
                    <span>{{ wordCount }} 字</span>
                  </div>
                </div>
                <textarea
                    v-model="formData.content"
                    placeholder="开始写作... 支持Markdown语法"
                    class="w-full h-96 border-none outline-none resize-none font-mono text-sm leading-relaxed placeholder-gray-300"
                    :class="{ 'text-red-500': errors.content }"
                ></textarea>
                <p v-if="errors.content" class="mt-2 text-sm text-red-600">{{ errors.content }}</p>
              </div>

              <!-- Tags 和 Category 暂时隐藏 -->
              <!-- 后续功能开发 -->
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
                <div class="markdown-body prose max-w-none" v-html="previewContent"></div>

                <!-- Empty Preview -->
                <div v-if="!formData.content" class="text-center py-12 text-gray-400">
                  <svg class="w-16 h-16 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
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
                  <svg v-if="!loading" class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <div v-else class="spinner w-5 h-5 mr-2"></div>
                  <span>{{ loading ? (isDraftAction ? '保存中...' : '发布中...') : (isEditMode ? '更新文章' : '发布文章') }}</span>
                </button>

                <button
                    @click="handleSubmit(true)"
                    :disabled="loading"
                    class="w-full btn-secondary flex items-center justify-center"
                >
                  <svg v-if="!loading" class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
    // 用于区分当前正在执行的是否为保存草稿（用于按钮显示文案）
    const isDraftAction = ref(false)

    const formData = reactive({
      title: '',
      content: ''
    })

    const errors = reactive({
      title: '',
      content: ''
    })

    // 是否为编辑模式
    const isEditMode = computed(() => !!route.params.id)

    // 字数统计
    const wordCount = computed(() => {
      return formData.content.length
    })

    // Markdown预览
    const previewContent = computed(() => {
      if (!formData.content) return ''
      return marked(formData.content)
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
        // 调用API获取文章详情
        const post = await getPostById(postId)

        // 简单防御：若API返回为空则跳回首页
        if (!post) {
          throw new Error('未找到文章')
        }

        formData.title = post.title || ''
        formData.content = post.content || ''
      } catch (error) {
        console.error('加载文章失败:', error)
        alert('加载文章失败')
        router.push('/')
      }
    }

    // 提交表单
    // isDraft: true -> 保存草稿； false -> 发布/更新
    const handleSubmit = async (isDraft = false) => {
      // 如果是草稿保存，可以选择放宽校验（这里仍然要求 title/content 非空）
      if (!validateForm()) {
        return
      }

      loading.value = true
      isDraftAction.value = isDraft

      try {
        const postData = {
          title: formData.title,
          content: formData.content,
          draft: !!isDraft
        }

        if (isEditMode.value) {
          // 更新文章
          await updatePost(route.params.id, postData)
          alert(isDraft ? '草稿保存成功!' : '文章更新成功!')
        } else {
          // 创建文章
          await createPost(postData)
          alert(isDraft ? '草稿保存成功!' : '文章发布成功!')
        }

        router.push('/')
      } catch (error) {
        console.error('提交失败:', error)
        alert(error?.response?.data?.message || '操作失败,请稍后重试')
      } finally {
        loading.value = false
        isDraftAction.value = false
      }
    }

    onMounted(() => {
      loadPost()
    })

    return {
      loading,
      isDraftAction,
      formData,
      errors,
      isEditMode,
      wordCount,
      previewContent,
      handleSubmit
    }
  }
}
</script>

<style scoped>
textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>
