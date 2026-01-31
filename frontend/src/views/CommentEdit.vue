<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-20 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-5xl mx-auto mb-6">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">编辑评论</h1>
              <p class="text-sm text-gray-500 mt-1">修改您的评论内容</p>
            </div>
            <button @click="handleCancel" class="btn-ghost">
              <svg class="w-5 h-5 mr-1 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
              取消
            </button>
          </div>
        </div>

        <div class="max-w-5xl mx-auto">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Editor Section -->
            <div class="space-y-4">
              <!-- Editor Card -->
              <div class="card p-6">
                <div class="mb-4 flex items-center justify-between border-b border-gray-200 pb-3">
                  <span class="text-sm font-medium text-gray-700">
                    评论内容 (Markdown)
                  </span>
                  <div class="flex items-center space-x-4 text-sm text-gray-500">
                    <span>{{ content.length }} / 3000</span>
                  </div>
                </div>

                <!-- Markdown Toolbar -->
                <div class="mb-3 flex flex-wrap gap-1 pb-3 border-b border-gray-100">
                  <button @click="insertMarkdown('heading')" type="button" class="toolbar-btn" title="标题">
                    <span class="font-bold text-sm">H</span>
                  </button>
                  <button @click="insertMarkdown('bold')" type="button" class="toolbar-btn" title="粗体">
                    <span class="font-bold">B</span>
                  </button>
                  <button @click="insertMarkdown('italic')" type="button" class="toolbar-btn" title="斜体">
                    <span class="italic">I</span>
                  </button>
                  <button @click="insertMarkdown('strikethrough')" type="button" class="toolbar-btn" title="删除线">
                    <span class="line-through">S</span>
                  </button>
                  <button @click="insertMarkdown('code')" type="button" class="toolbar-btn" title="行内代码">
                    <span class="font-mono text-xs">&lt;/&gt;</span>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

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

                  <button @click="insertMarkdown('link')" type="button" class="toolbar-btn" title="链接">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('codeblock')" type="button" class="toolbar-btn" title="代码块">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                    </svg>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

                  <EmojiPicker @select="insertEmoji" title="表情" />
                </div>

                <!-- Textarea -->
                <textarea
                  ref="contentTextarea"
                  v-model="content"
                  placeholder="支持 Markdown 语法，如 **加粗**、*斜体*、`代码` 等..."
                  class="w-full h-80 border-none outline-none resize-none leading-relaxed placeholder-gray-300 font-mono text-sm"
                ></textarea>
                <p v-if="error" class="mt-2 text-sm text-red-600">{{ error }}</p>
              </div>
            </div>

            <!-- Preview Section -->
            <div class="lg:sticky lg:top-24 h-fit">
              <div class="card p-6">
                <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
                  <h3 class="text-lg font-semibold text-gray-900">预览</h3>
                  <span class="text-sm text-gray-500">实时预览</span>
                </div>

                <div v-if="content.trim()" class="min-h-[300px]">
                  <div class="markdown-body" v-html="previewContent"></div>
                </div>

                <div v-else class="text-center py-12 text-gray-400">
                  <svg class="w-16 h-16 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <p>预览将在这里显示</p>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="mt-4 space-y-3">
                <button
                  @click="handleSave"
                  :disabled="saving || !content.trim() || content.length > 3000"
                  class="w-full btn-primary flex items-center justify-center"
                >
                  <svg v-if="!saving" class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <div v-else class="spinner w-5 h-5 mr-2 border-white"></div>
                  <span>{{ saving ? '保存中...' : '保存评论' }}</span>
                </button>

                <button
                  @click="handleCancel"
                  :disabled="saving"
                  class="w-full btn-secondary flex items-center justify-center"
                >
                  <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  <span>取消</span>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import Header from '@/components/Header.vue'
import EmojiPicker from '@/components/EmojiPicker.vue'
import { updateComment } from '@/api/comments'

export default {
  name: 'CommentEdit',
  components: {
    Header,
    EmojiPicker
  },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const content = ref('')
    const saving = ref(false)
    const error = ref('')
    const contentTextarea = ref(null)
    const originalContent = ref('')
    const postId = ref(null)

    const previewContent = computed(() => {
      if (!content.value) return ''
      try {
        return marked(content.value)
      } catch (error) {
        console.error('Markdown rendering error:', error)
        return content.value
      }
    })

    const loadComment = () => {
      // Get comment data from route state
      const commentData = history.state?.comment
      if (commentData) {
        content.value = commentData.content || ''
        originalContent.value = commentData.content || ''
        postId.value = commentData.postId
      } else {
        // If no comment data, redirect back
        alert('评论数据丢失')
        router.back()
      }
    }

    const handleSave = async () => {
      if (!content.value.trim()) {
        error.value = '评论内容不能为空'
        return
      }

      if (content.value.length > 3000) {
        error.value = '评论内容不能超过3000字符'
        return
      }

      saving.value = true
      error.value = ''

      try {
        const commentId = route.params.id
        await updateComment(commentId, content.value)
        
        // Navigate back to post detail
        if (postId.value) {
          router.push(`/post/${postId.value}`)
        } else {
          router.back()
        }
      } catch (err) {
        console.error('保存评论失败:', err)
        error.value = '保存失败，请稍后重试'
      } finally {
        saving.value = false
      }
    }

    const handleCancel = () => {
      if (content.value !== originalContent.value) {
        if (!confirm('您有未保存的更改，确定要取消吗？')) {
          return
        }
      }
      router.back()
    }

    const insertMarkdown = (type) => {
      const textarea = contentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const selectedText = content.value.substring(start, end)
      const beforeText = content.value.substring(0, start)
      const afterText = content.value.substring(end)

      let insertText = ''
      let cursorOffset = 0

      switch (type) {
        case 'heading':
          insertText = `## ${selectedText || '标题'}`
          cursorOffset = selectedText ? insertText.length : insertText.length - 2
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
        case 'codeblock':
          insertText = selectedText ? `\`\`\`\n${selectedText}\n\`\`\`` : '```\n代码块\n```'
          cursorOffset = selectedText ? insertText.length - 4 : insertText.length - 5
          break
        default:
          return
      }

      content.value = beforeText + insertText + afterText

      setTimeout(() => {
        textarea.focus()
        textarea.setSelectionRange(start + cursorOffset, start + cursorOffset)
      }, 0)
    }

    // 插入表情
    const insertEmoji = (emoji) => {
      const textarea = contentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const beforeText = content.value.substring(0, start)
      const afterText = content.value.substring(end)

      content.value = beforeText + emoji + afterText

      // 等待下一个tick后设置光标位置
      setTimeout(() => {
        textarea.focus()
        const newPosition = start + emoji.length
        textarea.setSelectionRange(newPosition, newPosition)
      }, 0)
    }

    onMounted(() => {
      loadComment()
    })

    return {
      content,
      saving,
      error,
      contentTextarea,
      previewContent,
      handleSave,
      handleCancel,
      insertMarkdown,
      insertEmoji
    }
  }
}
</script>

<style scoped>
.toolbar-btn {
  @apply px-2.5 py-1.5 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100
  transition-colors duration-150 flex items-center justify-center min-w-[32px];
}

.toolbar-btn:active {
  @apply bg-gray-200;
}

.spinner {
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: currentColor;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
