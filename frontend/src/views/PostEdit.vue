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

                <!-- Formatting Toolbar -->
                <div v-if="formData.contentType === 'MARKDOWN'" class="mb-3 flex flex-wrap gap-1 pb-3 border-b border-gray-100">
                  <!-- Headings -->
                  <button @click="insertMarkdown('heading1')" type="button" class="toolbar-btn" title="标题 1 (Ctrl+1)">
                    <span class="font-bold text-lg">H1</span>
                  </button>
                  <button @click="insertMarkdown('heading2')" type="button" class="toolbar-btn" title="标题 2 (Ctrl+2)">
                    <span class="font-bold">H2</span>
                  </button>
                  <button @click="insertMarkdown('heading3')" type="button" class="toolbar-btn" title="标题 3 (Ctrl+3)">
                    <span class="font-bold text-sm">H3</span>
                  </button>
                  
                  <div class="w-px h-6 bg-gray-300 mx-1"></div>
                  
                  <!-- Text Formatting -->
                  <button @click="insertMarkdown('bold')" type="button" class="toolbar-btn" title="粗体 (Ctrl+B)">
                    <span class="font-bold">B</span>
                  </button>
                  <button @click="insertMarkdown('italic')" type="button" class="toolbar-btn" title="斜体 (Ctrl+I)">
                    <span class="italic">I</span>
                  </button>
                  <button @click="insertMarkdown('strikethrough')" type="button" class="toolbar-btn" title="删除线">
                    <span class="line-through">S</span>
                  </button>
                  <button @click="insertMarkdown('code')" type="button" class="toolbar-btn" title="行内代码 (Ctrl+`)">
                    <span class="font-mono text-xs">&lt;/&gt;</span>
                  </button>
                  
                  <div class="w-px h-6 bg-gray-300 mx-1"></div>
                  
                  <!-- Lists -->
                  <button @click="insertMarkdown('ul')" type="button" class="toolbar-btn" title="无序列表">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('ol')" type="button" class="toolbar-btn" title="有序列表">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('quote')" type="button" class="toolbar-btn" title="引用">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                    </svg>
                  </button>
                  
                  <div class="w-px h-6 bg-gray-300 mx-1"></div>
                  
                  <!-- Links and Images -->
                  <button @click="insertMarkdown('link')" type="button" class="toolbar-btn" title="链接 (Ctrl+K)">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('image')" type="button" class="toolbar-btn" title="图片">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('codeblock')" type="button" class="toolbar-btn" title="代码块">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                    </svg>
                  </button>
                  
                  <div class="w-px h-6 bg-gray-300 mx-1"></div>
                  
                  <!-- Table -->
                  <button @click="insertMarkdown('table')" type="button" class="toolbar-btn" title="表格">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('hr')" type="button" class="toolbar-btn" title="分隔线">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14" />
                    </svg>
                  </button>
                </div>

                <textarea
                    ref="contentTextarea"
                    v-model="formData.content"
                    @keydown="handleKeydown"
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
    const contentTextarea = ref(null)

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

    // Markdown格式化插入函数
    const insertMarkdown = (type) => {
      const textarea = contentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const selectedText = formData.content.substring(start, end)
      const beforeText = formData.content.substring(0, start)
      const afterText = formData.content.substring(end)

      let insertText = ''
      let cursorOffset = 0

      switch (type) {
        case 'heading1':
          insertText = `# ${selectedText || '标题 1'}`
          cursorOffset = selectedText ? insertText.length : 2
          break
        case 'heading2':
          insertText = `## ${selectedText || '标题 2'}`
          cursorOffset = selectedText ? insertText.length : 3
          break
        case 'heading3':
          insertText = `### ${selectedText || '标题 3'}`
          cursorOffset = selectedText ? insertText.length : 4
          break
        case 'bold':
          insertText = `**${selectedText || '粗体文本'}**`
          cursorOffset = selectedText ? insertText.length : insertText.length - 2
          break
        case 'italic':
          insertText = `*${selectedText || '斜体文本'}*`
          cursorOffset = selectedText ? insertText.length : insertText.length - 1
          break
        case 'strikethrough':
          insertText = `~~${selectedText || '删除线文本'}~~`
          cursorOffset = selectedText ? insertText.length : insertText.length - 2
          break
        case 'code':
          insertText = `\`${selectedText || '代码'}\``
          cursorOffset = selectedText ? insertText.length : insertText.length - 1
          break
        case 'ul':
          insertText = selectedText ? `- ${selectedText}` : '- 列表项'
          cursorOffset = insertText.length
          break
        case 'ol':
          insertText = selectedText ? `1. ${selectedText}` : '1. 列表项'
          cursorOffset = insertText.length
          break
        case 'quote':
          insertText = selectedText ? `> ${selectedText}` : '> 引用内容'
          cursorOffset = insertText.length
          break
        case 'link':
          insertText = `[${selectedText || '链接文本'}](url)`
          cursorOffset = selectedText ? insertText.length - 4 : insertText.length - 5
          break
        case 'image':
          insertText = `![${selectedText || '图片描述'}](图片URL)`
          cursorOffset = selectedText ? insertText.length - 7 : insertText.length - 9
          break
        case 'codeblock':
          insertText = selectedText 
            ? `${'```'}\n${selectedText}\n${'```'}` 
            : `${'```'}javascript\n// 代码\n${'```'}`
          cursorOffset = selectedText ? insertText.length - 4 : 16
          break
        case 'table':
          insertText = '| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |'
          cursorOffset = 2
          break
        case 'hr':
          insertText = '\n---\n'
          cursorOffset = insertText.length
          break
        default:
          return
      }

      formData.content = beforeText + insertText + afterText
      
      // 等待下一个tick后设置光标位置
      setTimeout(() => {
        textarea.focus()
        textarea.setSelectionRange(start + cursorOffset, start + cursorOffset)
      }, 0)
    }

    // 键盘快捷键处理
    const handleKeydown = (e) => {
      // Tab键处理：插入两个空格
      if (e.key === 'Tab') {
        e.preventDefault()
        const textarea = contentTextarea.value
        const start = textarea.selectionStart
        const end = textarea.selectionEnd
        const beforeText = formData.content.substring(0, start)
        const afterText = formData.content.substring(end)
        
        formData.content = beforeText + '  ' + afterText
        
        setTimeout(() => {
          textarea.setSelectionRange(start + 2, start + 2)
        }, 0)
        return
      }

      // Ctrl/Cmd + 快捷键
      if (e.ctrlKey || e.metaKey) {
        switch (e.key.toLowerCase()) {
          case 'b':
            e.preventDefault()
            insertMarkdown('bold')
            break
          case 'i':
            e.preventDefault()
            insertMarkdown('italic')
            break
          case 'k':
            e.preventDefault()
            insertMarkdown('link')
            break
          case '`':
            e.preventDefault()
            insertMarkdown('code')
            break
          case '1':
            e.preventDefault()
            insertMarkdown('heading1')
            break
          case '2':
            e.preventDefault()
            insertMarkdown('heading2')
            break
          case '3':
            e.preventDefault()
            insertMarkdown('heading3')
            break
        }
      }
    }

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
      handlePublish,
      contentTextarea,
      insertMarkdown,
      handleKeydown
    }
  }
}
</script>

<style scoped>
textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.toolbar-btn {
  @apply px-2.5 py-1.5 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100 
         transition-colors duration-150 flex items-center justify-center min-w-[32px];
}

.toolbar-btn:active {
  @apply bg-gray-200;
}

.toolbar-btn:focus {
  @apply outline-none ring-2 ring-primary-500 ring-opacity-50;
}
</style>