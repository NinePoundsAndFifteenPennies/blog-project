<template>
  <div class="relative w-full" ref="searchContainer">
    <!-- Search Input -->
    <div class="relative group">
      <button
        @click="handleSearchIconClick"
        class="absolute inset-y-0 left-0 pl-3 flex items-center text-gray-400 hover:text-primary-500 transition-colors duration-200 cursor-pointer"
      >
        <svg class="h-5 w-5 group-focus-within:text-primary-500"
             fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
        </svg>
      </button>
      <input
          v-model="searchQuery"
          type="text"
          class="block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-dark-700 rounded-lg leading-5 bg-white dark:bg-dark-800 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:placeholder-gray-400 focus:border-primary-500 focus:ring-1 focus:ring-primary-500 sm:text-sm transition-all duration-200 shadow-sm hover:shadow-md"
          :placeholder="placeholder"
          @input="handleInput"
          @keydown.down.prevent="navigateDown"
          @keydown.up.prevent="navigateUp"
          @keydown.enter.prevent="handleEnter"
          @keydown.esc="closePreview"
          @focus="handleFocus"
          autocomplete="off"
      >
      <!-- Clear Button -->
      <button
          v-if="searchQuery"
          @click="clearSearch"
          class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors duration-200"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
      
      <!-- Loading Spinner -->
      <div v-if="loading" class="absolute inset-y-0 right-8 flex items-center">
        <div class="animate-spin rounded-full h-4 w-4 border-2 border-primary-500 border-t-transparent"></div>
      </div>
    </div>

    <!-- Search Preview Dropdown -->
    <transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
    >
      <div
          v-if="showPreview"
          class="absolute z-[100] mt-2 w-full bg-white dark:bg-dark-900 rounded-lg shadow-xl border border-gray-100 dark:border-dark-800 overflow-hidden ring-1 ring-black ring-opacity-5"
      >
        <!-- No Results -->
        <div v-if="!loading && searchResults.length === 0" class="p-4 text-center text-gray-500 dark:text-gray-400 text-sm">
          暂无搜索结果
        </div>

        <ul v-else class="max-h-[70vh] overflow-y-auto divide-y divide-gray-100 dark:divide-dark-800">
          <li
              v-for="(result, index) in searchResults"
              :key="result.post.id"
              class="cursor-pointer transition-colors duration-150 relative"
              :class="{'bg-primary-50 dark:bg-primary-900/30': index === selectedIndex, 'hover:bg-gray-50 dark:hover:bg-dark-800': index !== selectedIndex}"
              @click="selectResult(result.post)"
              @mouseenter="selectedIndex = index"
          >
            <!-- Highlight bar for selected item -->
            <div 
              class="absolute left-0 top-0 bottom-0 w-1 bg-primary-500 dark:bg-primary-400 transition-opacity duration-200" 
              :class="index === selectedIndex ? 'opacity-100' : 'opacity-0'"
            ></div>
            
            <div class="p-4 pl-5">
              <!-- Title -->
              <h4 
                class="text-sm font-semibold text-gray-900 dark:text-gray-100 mb-1"
                v-html="result.highlightedTitle"
              ></h4>
              
              <!-- Content Snippet -->
              <p 
                v-if="result.highlightedContent"
                class="text-xs text-gray-500 dark:text-gray-400 mb-2 line-clamp-2"
                v-html="result.highlightedContent"
              ></p>
              
              <div class="flex flex-wrap items-center gap-2 mt-2">
                <!-- Matched Tags -->
                <template v-if="result.matchedTags.length">
                  <span
                    v-for="tag in result.matchedTags"
                    :key="tag.id"
                    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400"
                    v-html="tag.highlighted"
                  ></span>
                  <span class="text-gray-300 dark:text-gray-600 text-xs">|</span>
                </template>
                
                <!-- Author -->
                <span 
                    v-if="result.highlightedAuthor"
                    class="flex items-center text-xs text-gray-500 dark:text-gray-400"
                >
                  <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span v-html="result.highlightedAuthor"></span>
                </span>
                
                <!-- Stats -->
                <span class="flex items-center text-xs text-gray-400 ml-auto space-x-3">
                   <span class="flex items-center">
                    <svg class="w-3 h-3 mr-1 text-red-400" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    {{ result.post.likeCount || 0 }}
                  </span>
                  <span class="flex items-center">
                    <svg class="w-3 h-3 mr-1 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
                      <path fill-rule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clip-rule="evenodd" />
                    </svg>
                    {{ result.post.commentCount || 0 }}
                  </span>
                </span>
              </div>
            </div>
          </li>
        </ul>
        
        <!-- Footer -->
        <div class="bg-gray-50 px-4 py-2 border-t border-gray-100 flex justify-between items-center">
            <span class="text-xs text-gray-500">
                点击🔍查看全部结果
            </span>
            <span class="text-xs text-gray-400">
                支持搜索 标题 / 内容 / 标签 / 作者
            </span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, watch, onMounted, onUnmounted } from 'vue'

export default {
  name: 'SearchPreview',
  props: {
    searchFunction: {
      type: Function,
      required: true
    },
    placeholder: {
      type: String,
      default: '搜索文章...'
    },
    initialValue: {
      type: String,
      default: ''
    }
  },
  emits: ['search', 'select'],
  setup(props, { emit }) {
    const searchQuery = ref(props.initialValue || '')
    const showPreview = ref(false)
    const selectedIndex = ref(-1)  // Start with -1 to allow going to search page
    const searchContainer = ref(null)
    const searchResults = ref([])
    const loading = ref(false)
    let debounceTimer = null

    // Watch for initialValue changes (e.g., from URL)
    watch(() => props.initialValue, (newVal) => {
      if (newVal !== undefined && newVal !== searchQuery.value) {
        searchQuery.value = newVal
      }
    })

    // Clean markdown URL part only, keep link text
    const cleanMarkdown = (text) => {
      if (!text) return ''
      // Remove URL part from [text](url) -> text
      let cleaned = text.replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
      // Remove image with link ![alt](url) -> alt
      cleaned = cleaned.replace(/!\[([^\]]+)\]\([^)]+\)/g, '$1')
      // Remove bare URLs
      cleaned = cleaned.replace(/https?:\/\/[^\s]+/g, '')
      return cleaned
    }

    // Escape HTML to prevent XSS
    const escapeHtml = (text) => {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    }

    // Highlight matched text
    const highlightText = (text, query) => {
      if (!text || !query) return escapeHtml(text || '')
      
      const escapedText = escapeHtml(text)
      const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      const regex = new RegExp(`(${escapedQuery})`, 'gi')
      
      return escapedText.replace(regex, '<mark class="bg-yellow-200 text-gray-900 font-medium">$1</mark>')
    }

    // Check if text matches query (after cleaning)
    const matchesQuery = (text, query) => {
      if (!text || !query) return false
      return text.toLowerCase().includes(query.toLowerCase())
    }

    // Get content snippet around match
    const getContentSnippet = (content, query, maxLength = 100) => {
      if (!content || !query) return ''
      
      // Clean markdown first before matching
      const cleanedContent = cleanMarkdown(content)
      const lowerContent = cleanedContent.toLowerCase()
      const lowerQuery = query.toLowerCase()
      const index = lowerContent.indexOf(lowerQuery)
      
      if (index === -1) return cleanedContent.substring(0, maxLength)
      
      const start = Math.max(0, index - 30)
      const end = Math.min(cleanedContent.length, index + query.length + 70)
      
      let snippet = cleanedContent.substring(start, end)
      if (start > 0) snippet = '...' + snippet
      if (end < cleanedContent.length) snippet = snippet + '...'
      
      return snippet
    }

    // Process API results for display
    const processResults = (posts, query) => {
      if (!posts || !query) return []

      return posts.map(post => {
        const matchScore = {
          title: 0,
          tags: 0,
          content: 0,
          author: 0
        }

        let highlightedTitle = escapeHtml(post.title)
        let highlightedContent = null
        let highlightedAuthor = null
        const matchedTags = []

        // Check title match
        if (matchesQuery(post.title, query)) {
          matchScore.title = 4
          highlightedTitle = highlightText(post.title, query)
        }

        // Check tags match
        if (post.tags && post.tags.length > 0) {
          post.tags.forEach(tag => {
            if (matchesQuery(tag.name, query)) {
              matchScore.tags = 3
              matchedTags.push({
                ...tag,
                highlighted: highlightText(tag.name, query)
              })
            }
          })
        }

        // Check content match - use cleaned content
        const content = post.content || post.summary || ''
        const cleanedContent = cleanMarkdown(content)
        if (matchesQuery(cleanedContent, query)) {
          matchScore.content = 2
          const snippet = getContentSnippet(content, query) // reusing snippet logic which does cleaning
          highlightedContent = highlightText(snippet, query)
        }

        // Check author match
        const authorName = post.authorNickname || post.authorUsername || ''
        if (matchesQuery(authorName, query)) {
          matchScore.author = 1
          highlightedAuthor = highlightText(authorName, query)
        }

        return {
          post,
          highlightedTitle,
          highlightedContent,
          highlightedAuthor,
          matchedTags
        }
      })
    }

    // Handle input with debounce
    const handleInput = () => {
      clearTimeout(debounceTimer)
      
      if (!searchQuery.value.trim()) {
        searchResults.value = []
        showPreview.value = false
        return
      }

      debounceTimer = setTimeout(async () => {
        loading.value = true
        showPreview.value = true
        selectedIndex.value = -1  // Don't auto-select any item
        
        try {
          const results = await props.searchFunction(searchQuery.value)
          searchResults.value = processResults(results, searchQuery.value)
        } catch (error) {
          console.error('Search preview failed:', error)
          searchResults.value = []
        } finally {
          loading.value = false
        }
      }, 300)
    }

    // Handle focus - show preview if there's a query
    const handleFocus = () => {
      if (searchQuery.value.trim() && searchResults.value.length > 0) {
        showPreview.value = true
      }
    }

    // Handle search icon click
    const handleSearchIconClick = () => {
      if (searchQuery.value.trim()) {
        emit('search', searchQuery.value)
      }
    }

    // Keyboard navigation
    const navigateDown = () => {
      if (searchResults.value.length > 0) {
        if (selectedIndex.value < searchResults.value.length - 1) {
          selectedIndex.value++
        }
      }
    }

    const navigateUp = () => {
      if (selectedIndex.value > -1) {  // Allow going back to -1 (no selection)
        selectedIndex.value--
      }
    }

    const handleEnter = () => {
      // Only select result if an item is actually selected (index >= 0)
      if (selectedIndex.value >= 0 && searchResults.value.length > 0) {
        selectResult(searchResults.value[selectedIndex.value].post)
      } else {
        // No item selected or no results - go to search page
        emit('search', searchQuery.value)
        closePreview()
      }
    }

    // Select result
    const selectResult = (post) => {
      emit('select', post)
      closePreview()
    }

    // Clear search
    const clearSearch = () => {
      searchQuery.value = ''
      searchResults.value = []
      showPreview.value = false
      // emit('search', '') // Don't trigger search on clear, just clear
    }

    // Close preview
    const closePreview = () => {
      showPreview.value = false
    }

    // Click outside to close
    const handleClickOutside = (event) => {
      if (searchContainer.value && !searchContainer.value.contains(event.target)) {
        closePreview()
      }
    }

    // Watch search query
    watch(searchQuery, (newVal) => {
      if (newVal) {
        showPreview.value = true
      }
    })

    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      clearTimeout(debounceTimer)
    })

    return {
      searchQuery,
      showPreview,
      selectedIndex,
      searchContainer,
      searchResults,
      loading,
      handleInput,
      handleFocus,
      handleSearchIconClick,
      navigateDown,
      navigateUp,
      handleEnter,
      selectResult,
      clearSearch,
      closePreview
    }
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

mark {
  padding: 0 2px;
  border-radius: 2px;
}
</style>
