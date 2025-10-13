<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-20 pb-12">
      <div class="container mx-auto px-4">
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

        <div class="max-w-7xl mx-auto">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="space-y-4">
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

              <div class="card p-6">
                <div class="mb-4 flex items-center justify-between border-b border-gray-200 pb-3">
                  <span class="text-sm font-medium text-gray-700">
                    内容 ({{ formData.contentType === 'MARKDOWN' ? 'Markdown' : 'HTML' }})
                  </span>
                  <div class="flex items-center space-x-4 text-sm text-gray-500">
                    <span>{{ wordCount }} 字</span>
                  </div>
                </div>

                <div v-if="formData.contentType === 'MARKDOWN'" class="mb-3 flex flex-wrap gap-1 pb-3 border-b border-gray-100">
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
                  <button @click="insertMarkdown('tasklist')" type="button" class="toolbar-btn" title="任务列表">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                    </svg>
                  </button>
                  <button @click="insertMarkdown('quote')" type="button" class="toolbar-btn" title="引用">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                    </svg>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

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

                <div v-if="formData.contentType === 'HTML'" class="mb-3 flex flex-wrap gap-1 pb-3 border-b border-gray-100">
                  <button @click="insertHTML('h1')" type="button" class="toolbar-btn" title="标题 1">
                    <span class="font-bold text-lg">H1</span>
                  </button>
                  <button @click="insertHTML('h2')" type="button" class="toolbar-btn" title="标题 2">
                    <span class="font-bold">H2</span>
                  </button>
                  <button @click="insertHTML('h3')" type="button" class="toolbar-btn" title="标题 3">
                    <span class="font-bold text-sm">H3</span>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

                  <button @click="insertHTML('strong')" type="button" class="toolbar-btn" title="粗体">
                    <span class="font-bold">B</span>
                  </button>
                  <button @click="insertHTML('em')" type="button" class="toolbar-btn" title="斜体">
                    <span class="italic">I</span>
                  </button>
                  <button @click="insertHTML('u')" type="button" class="toolbar-btn" title="下划线">
                    <span class="underline">U</span>
                  </button>
                  <button @click="insertHTML('code')" type="button" class="toolbar-btn" title="代码">
                    <span class="font-mono text-xs">&lt;/&gt;</span>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

                  <button @click="insertHTML('ul')" type="button" class="toolbar-btn" title="无序列表">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                  </button>
                  <button @click="insertHTML('ol')" type="button" class="toolbar-btn" title="有序列表">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                  </button>
                  <button @click="insertHTML('blockquote')" type="button" class="toolbar-btn" title="引用">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                    </svg>
                  </button>
                  <button @click="insertHTML('p')" type="button" class="toolbar-btn" title="段落">
                    <span class="text-xs">P</span>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

                  <button @click="insertHTML('a')" type="button" class="toolbar-btn" title="链接">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                    </svg>
                  </button>
                  <button @click="insertHTML('img')" type="button" class="toolbar-btn" title="图片">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </button>
                  <button @click="insertHTML('pre')" type="button" class="toolbar-btn" title="预格式化">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                    </svg>
                  </button>

                  <div class="w-px h-6 bg-gray-300 mx-1"></div>

                  <button @click="insertHTML('table')" type="button" class="toolbar-btn" title="表格">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                    </svg>
                  </button>
                  <button @click="insertHTML('div')" type="button" class="toolbar-btn" title="容器">
                    <span class="text-xs font-mono">div</span>
                  </button>
                  <button @click="insertHTML('hr')" type="button" class="toolbar-btn" title="分隔线">
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

            <div class="lg:sticky lg:top-24 h-fit">
              <div class="card p-6">
                <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
                  <h3 class="text-lg font-semibold text-gray-900">预览</h3>
                  <span class="text-sm text-gray-500">实时预览</span>
                </div>

                <h1 class="text-3xl font-bold text-gray-900 mb-6">
                  {{ formData.title || '无标题' }}
                </h1>

                <div
                    v-if="formData.content"
                    class="markdown-body prose max-w-none"
                    v-html="previewContent"
                ></div>

                <div v-else class="text-center py-12 text-gray-400">
                  <svg class="w-16 h-16 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <p>内容预览将在这里显示</p>
                </div>
              </div>

              <div class="mt-4 space-y-3">
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

    <div v-if="showLanguageModal" class="fixed inset-0 bg-black bg-opacity-50 flex justify-center z-[9999] pt-32" @click.self="showLanguageModal = false">
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 relative">
        <h3 class="text-xl font-bold mb-4">选择代码语言</h3>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">编程语言</label>
          <select v-model="codeLanguage" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500">
            <option v-for="lang in availableLanguages" :key="lang.value" :value="lang.value">
              {{ lang.label }}
            </option>
          </select>
        </div>
        <div class="flex space-x-3">
          <button @click="insertCodeBlock" class="flex-1 btn-primary">
            插入代码块
          </button>
          <button @click="showLanguageModal = false" class="flex-1 btn-secondary">
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js/lib/core'
// 导入常用语言
import javascript from 'highlight.js/lib/languages/javascript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import cpp from 'highlight.js/lib/languages/cpp'
import css from 'highlight.js/lib/languages/css'
import html from 'highlight.js/lib/languages/xml'
import json from 'highlight.js/lib/languages/json'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import go from 'highlight.js/lib/languages/go'
import rust from 'highlight.js/lib/languages/rust'
import typescript from 'highlight.js/lib/languages/typescript'
import 'highlight.js/styles/atom-one-dark.css'

import Header from '@/components/Header.vue'
import { getPostById, createPost, updatePost } from '@/api/posts'

// 注册语言
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('py', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c++', cpp)
hljs.registerLanguage('css', css)
hljs.registerLanguage('html', html)
hljs.registerLanguage('xml', html)
hljs.registerLanguage('json', json)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sh', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('go', go)
hljs.registerLanguage('golang', go)
hljs.registerLanguage('rust', rust)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)

// 配置 marked 使用 highlight.js
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error('Highlight error:', err)
      }
    }
    // 自动检测语言
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

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
    const showLanguageModal = ref(false)
    const codeLanguage = ref('javascript')
    const availableLanguages = [
      { value: 'javascript', label: 'JavaScript' },
      { value: 'typescript', label: 'TypeScript' },
      { value: 'python', label: 'Python' },
      { value: 'java', label: 'Java' },
      { value: 'cpp', label: 'C++' },
      { value: 'c', label: 'C' },
      { value: 'csharp', label: 'C#' },
      { value: 'go', label: 'Go' },
      { value: 'rust', label: 'Rust' },
      { value: 'php', label: 'PHP' },
      { value: 'ruby', label: 'Ruby' },
      { value: 'swift', label: 'Swift' },
      { value: 'kotlin', label: 'Kotlin' },
      { value: 'html', label: 'HTML' },
      { value: 'css', label: 'CSS' },
      { value: 'scss', label: 'SCSS' },
      { value: 'json', label: 'JSON' },
      { value: 'xml', label: 'XML' },
      { value: 'sql', label: 'SQL' },
      { value: 'bash', label: 'Bash/Shell' },
      { value: 'powershell', label: 'PowerShell' },
      { value: 'yaml', label: 'YAML' },
      { value: 'markdown', label: 'Markdown' },
      { value: 'plaintext', label: 'Plain Text' }
    ]

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
        case 'tasklist':
          insertText = selectedText ? `- [ ] ${selectedText}` : '- [ ] 任务项'
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
          // 显示语言选择器
          showLanguageModal.value = true
          return // 不直接插入，等用户选择语言
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

    // 插入代码块（带语言）
    const insertCodeBlock = () => {
      const textarea = contentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const selectedText = formData.content.substring(start, end)
      const beforeText = formData.content.substring(0, start)
      const afterText = formData.content.substring(end)

      const insertText = selectedText
          ? `${'```'}${codeLanguage.value}\n${selectedText}\n${'```'}`
          : `${'```'}${codeLanguage.value}\n// ${availableLanguages.find(l => l.value === codeLanguage.value)?.label || '代码'}\n${'```'}`

      const cursorOffset = selectedText ? insertText.length - 4 : insertText.length - 4

      formData.content = beforeText + insertText + afterText

      setTimeout(() => {
        textarea.focus()
        textarea.setSelectionRange(start + cursorOffset, start + cursorOffset)
      }, 0)

      // 关闭模态框
      showLanguageModal.value = false
    }

    // 键盘快捷键处理
    const handleKeydown = (e) => {
      const textarea = contentTextarea.value
      if (!textarea) return

      // Enter键处理：自动延续列表、引用等
      if (e.key === 'Enter' && formData.contentType === 'MARKDOWN') {
        const start = textarea.selectionStart
        const beforeCursor = formData.content.substring(0, start)
        const afterCursor = formData.content.substring(start)

        // 找到当前行
        const lines = beforeCursor.split('\n')
        const currentLine = lines[lines.length - 1]

        // 检查是否是任务列表（必须在无序列表之前检查）
        const taskMatch = currentLine.match(/^(\s*)-\s+\[([ xX])\]\s+(.*)$/)
        if (taskMatch) {
          e.preventDefault()
          const indent = taskMatch[1]
          const content = taskMatch[3]

          // 如果内容为空，退出列表
          if (!content.trim()) {
            formData.content = beforeCursor.slice(0, -currentLine.length) + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start - currentLine.length, start - currentLine.length)
            }, 0)
          } else {
            // 继续任务列表
            formData.content = beforeCursor + '\n' + indent + '- [ ] ' + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start + indent.length + 6, start + indent.length + 6)
            }, 0)
          }
          return
        }

        // 检查是否是无序列表
        const ulMatch = currentLine.match(/^(\s*)-\s+(.*)$/)
        if (ulMatch) {
          e.preventDefault()
          const indent = ulMatch[1]
          const content = ulMatch[2]

          // 如果内容为空，退出列表
          if (!content.trim()) {
            formData.content = beforeCursor.slice(0, -currentLine.length) + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start - currentLine.length, start - currentLine.length)
            }, 0)
          } else {
            // 继续列表
            formData.content = beforeCursor + '\n' + indent + '- ' + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start + indent.length + 3, start + indent.length + 3)
            }, 0)
          }
          return
        }

        // 检查是否是有序列表
        const olMatch = currentLine.match(/^(\s*)(\d+)\.\s+(.*)$/)
        if (olMatch) {
          e.preventDefault()
          const indent = olMatch[1]
          const num = parseInt(olMatch[2])
          const content = olMatch[3]

          // 如果内容为空，退出列表
          if (!content.trim()) {
            formData.content = beforeCursor.slice(0, -currentLine.length) + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start - currentLine.length, start - currentLine.length)
            }, 0)
          } else {
            // 继续列表，序号自增
            formData.content = beforeCursor + '\n' + indent + (num + 1) + '. ' + afterCursor
            setTimeout(() => {
              const newPos = start + indent.length + (num + 1).toString().length + 3
              textarea.setSelectionRange(newPos, newPos)
            }, 0)
          }
          return
        }

        // 检查是否是引用
        const quoteMatch = currentLine.match(/^(>\s*)(.*)$/)
        if (quoteMatch) {
          e.preventDefault()
          const content = quoteMatch[2]

          // 如果内容为空，检查前一行是否也是空引用
          if (!content.trim()) {
            // 检查前一行是否也是空的引用（> 开头但无内容）
            if (lines.length >= 2) {
              const prevLine = lines[lines.length - 2]
              const prevQuoteMatch = prevLine.match(/^>\s*$/)
              if (prevQuoteMatch) {
                // 前一行也是空引用，退出引用模式
                formData.content = beforeCursor.slice(0, -currentLine.length) + afterCursor
                setTimeout(() => {
                  textarea.setSelectionRange(start - currentLine.length, start - currentLine.length)
                }, 0)
                return
              }
            }
            // 只有一个空引用，继续引用（允许空行）
            formData.content = beforeCursor + '\n> ' + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start + 3, start + 3)
            }, 0)
          } else {
            // 有内容，继续引用
            formData.content = beforeCursor + '\n> ' + afterCursor
            setTimeout(() => {
              textarea.setSelectionRange(start + 3, start + 3)
            }, 0)
          }
          return
        }
      }

      // Tab键处理：插入两个空格
      if (e.key === 'Tab') {
        e.preventDefault()
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
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('bold')
            } else {
              insertHTML('strong')
            }
            break
          case 'i':
            e.preventDefault()
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('italic')
            } else {
              insertHTML('em')
            }
            break
          case 'k':
            e.preventDefault()
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('link')
            } else {
              insertHTML('a')
            }
            break
          case '`':
            e.preventDefault()
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('code')
            }
            break
          case '1':
            e.preventDefault()
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('heading1')
            } else {
              insertHTML('h1')
            }
            break
          case '2':
            e.preventDefault()
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('heading2')
            } else {
              insertHTML('h2')
            }
            break
          case '3':
            e.preventDefault()
            if (formData.contentType === 'MARKDOWN') {
              insertMarkdown('heading3')
            } else {
              insertHTML('h3')
            }
            break
        }
      }
    }

    // HTML格式化插入函数
    const insertHTML = (tag) => {
      const textarea = contentTextarea.value
      if (!textarea) return

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const selectedText = formData.content.substring(start, end)
      const beforeText = formData.content.substring(0, start)
      const afterText = formData.content.substring(end)

      let insertText = ''
      let cursorOffset = 0

      switch (tag) {
        case 'h1':
        case 'h2':
        case 'h3':
        case 'h4':
        case 'h5':
        case 'h6':
          insertText = `<${tag}>${selectedText || '标题'}</${tag}>`
          cursorOffset = selectedText ? insertText.length : tag.length + 2
          break
        case 'strong':
          insertText = `<strong>${selectedText || '粗体文本'}</strong>`
          cursorOffset = selectedText ? insertText.length : 8
          break
        case 'em':
          insertText = `<em>${selectedText || '斜体文本'}</em>`
          cursorOffset = selectedText ? insertText.length : 4
          break
        case 'u':
          insertText = `<u>${selectedText || '下划线文本'}</u>`
          cursorOffset = selectedText ? insertText.length : 3
          break
        case 'code':
          insertText = `<code>${selectedText || '代码'}</code>`
          cursorOffset = selectedText ? insertText.length : 6
          break
        case 'p':
          insertText = `<p>${selectedText || '段落内容'}</p>`
          cursorOffset = selectedText ? insertText.length : 3
          break
        case 'blockquote':
          insertText = `<blockquote>${selectedText || '引用内容'}</blockquote>`
          cursorOffset = selectedText ? insertText.length : 12
          break
        case 'ul':
          insertText = '<ul>\n  <li>列表项 1</li>\n  <li>列表项 2</li>\n</ul>'
          cursorOffset = 11
          break
        case 'ol':
          insertText = '<ol>\n  <li>列表项 1</li>\n  <li>列表项 2</li>\n</ol>'
          cursorOffset = 11
          break
        case 'a':
          insertText = `<a href="url">${selectedText || '链接文本'}</a>`
          cursorOffset = selectedText ? 9 : 9
          break
        case 'img':
          insertText = '<img src="图片URL" alt="图片描述" />'
          cursorOffset = 10
          break
        case 'pre':
          insertText = '<pre><code>\n代码内容\n</code></pre>'
          cursorOffset = 12
          break
        case 'table':
          insertText = `<table>
  <thead>
    <tr>
      <th>列1</th>
      <th>列2</th>
      <th>列3</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>内容</td>
      <td>内容</td>
      <td>内容</td>
    </tr>
  </tbody>
</table>`
          cursorOffset = 30
          break
        case 'div':
          insertText = `<div>${selectedText || '内容'}</div>`
          cursorOffset = selectedText ? insertText.length : 5
          break
        case 'hr':
          insertText = '<hr />'
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
      insertHTML,
      handleKeydown,
      showLanguageModal,
      codeLanguage,
      availableLanguages,
      insertCodeBlock
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