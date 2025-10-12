<template>
  <div class="editor-toolbar border-b border-gray-200 pb-3 mb-3">
    <div class="flex flex-wrap gap-1">
      <!-- Headings -->
      <div class="toolbar-group border-r border-gray-200 pr-2 mr-2">
        <button
          v-for="level in [1, 2, 3]"
          :key="level"
          @click="insertHeading(level)"
          class="toolbar-btn"
          :title="`标题 ${level}`"
        >
          <span class="font-bold">H{{ level }}</span>
        </button>
      </div>

      <!-- Text Formatting -->
      <div class="toolbar-group border-r border-gray-200 pr-2 mr-2">
        <button @click="insertBold" class="toolbar-btn" title="粗体 (Ctrl+B)">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z" />
          </svg>
        </button>
        <button @click="insertItalic" class="toolbar-btn" title="斜体 (Ctrl+I)">
          <svg class="w-4 h-4 italic" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l6-16" />
          </svg>
        </button>
        <button @click="insertStrikethrough" class="toolbar-btn" title="删除线">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 12h12M9 5v14m6-14v14" />
          </svg>
        </button>
      </div>

      <!-- Lists -->
      <div class="toolbar-group border-r border-gray-200 pr-2 mr-2">
        <button @click="insertUnorderedList" class="toolbar-btn" title="无序列表">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <button @click="insertOrderedList" class="toolbar-btn" title="有序列表">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7" />
          </svg>
        </button>
      </div>

      <!-- Insert Elements -->
      <div class="toolbar-group border-r border-gray-200 pr-2 mr-2">
        <button @click="insertLink" class="toolbar-btn" title="插入链接 (Ctrl+K)">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
          </svg>
        </button>
        <button @click="insertImage" class="toolbar-btn" title="插入图片">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </button>
        <button @click="insertCodeBlock" class="toolbar-btn" title="代码块">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
          </svg>
        </button>
        <button @click="insertInlineCode" class="toolbar-btn" title="行内代码">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </button>
      </div>

      <!-- Other -->
      <div class="toolbar-group">
        <button @click="insertQuote" class="toolbar-btn" title="引用">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
          </svg>
        </button>
        <button @click="insertHorizontalRule" class="toolbar-btn" title="分割线">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EditorToolbar',
  props: {
    contentType: {
      type: String,
      default: 'MARKDOWN'
    }
  },
  emits: ['insert'],
  setup(props, { emit }) {
    const insertText = (before, after = '', placeholder = '') => {
      emit('insert', { before, after, placeholder })
    }

    const insertHeading = (level) => {
      if (props.contentType === 'MARKDOWN') {
        const prefix = '#'.repeat(level)
        insertText(`${prefix} `, '', '标题文本')
      } else {
        insertText(`<h${level}>`, `</h${level}>`, '标题文本')
      }
    }

    const insertBold = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('**', '**', '粗体文本')
      } else {
        insertText('<strong>', '</strong>', '粗体文本')
      }
    }

    const insertItalic = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('*', '*', '斜体文本')
      } else {
        insertText('<em>', '</em>', '斜体文本')
      }
    }

    const insertStrikethrough = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('~~', '~~', '删除线文本')
      } else {
        insertText('<del>', '</del>', '删除线文本')
      }
    }

    const insertUnorderedList = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('- ', '', '列表项')
      } else {
        insertText('<ul>\n  <li>', '</li>\n</ul>', '列表项')
      }
    }

    const insertOrderedList = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('1. ', '', '列表项')
      } else {
        insertText('<ol>\n  <li>', '</li>\n</ol>', '列表项')
      }
    }

    const insertLink = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('[', '](https://example.com)', '链接文字')
      } else {
        insertText('<a href="https://example.com">', '</a>', '链接文字')
      }
    }

    const insertImage = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('![', '](https://example.com/image.jpg)', '图片描述')
      } else {
        insertText('<img src="https://example.com/image.jpg" alt="', '" />', '图片描述')
      }
    }

    const insertCodeBlock = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('```\n', '\n```', '代码内容')
      } else {
        insertText('<pre><code>', '</code></pre>', '代码内容')
      }
    }

    const insertInlineCode = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('`', '`', '代码')
      } else {
        insertText('<code>', '</code>', '代码')
      }
    }

    const insertQuote = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('> ', '', '引用文本')
      } else {
        insertText('<blockquote>', '</blockquote>', '引用文本')
      }
    }

    const insertHorizontalRule = () => {
      if (props.contentType === 'MARKDOWN') {
        insertText('\n---\n', '', '')
      } else {
        insertText('<hr />', '', '')
      }
    }

    return {
      insertHeading,
      insertBold,
      insertItalic,
      insertStrikethrough,
      insertUnorderedList,
      insertOrderedList,
      insertLink,
      insertImage,
      insertCodeBlock,
      insertInlineCode,
      insertQuote,
      insertHorizontalRule
    }
  }
}
</script>

<style scoped>
.toolbar-btn {
  @apply p-2 rounded hover:bg-gray-100 transition-colors duration-200 text-gray-600 hover:text-gray-900;
}

.toolbar-btn:active {
  @apply bg-gray-200;
}

.toolbar-group {
  @apply flex gap-1;
}
</style>
