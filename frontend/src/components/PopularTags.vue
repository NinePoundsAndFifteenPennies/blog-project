<template>
  <div class="card p-6">
    <h3 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
      <svg class="w-5 h-5 mr-2 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
      </svg>
      热门标签
    </h3>

    <!-- Loading state -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 5" :key="i" class="h-8 bg-gray-200 rounded animate-pulse"></div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!tags.length" class="text-center py-4 text-gray-500 text-sm">
      暂无标签
    </div>

    <!-- Tags list -->
    <div v-else class="flex flex-wrap gap-2">
      <button
        v-for="tag in tags"
        :key="tag.id"
        @click="handleTagClick(tag)"
        class="inline-flex items-center px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200 hover:scale-105 hover:shadow-md"
        :style="getTagStyle(tag)"
        :title="tag.description || tag.name"
      >
        <i v-if="tag.icon" :class="getIconClass(tag.icon)" class="mr-1.5"></i>
        <span>{{ tag.name }}</span>
        <span class="ml-2 text-xs opacity-75">({{ tag.postCount }})</span>
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPopularTags } from '@/api/tags'

export default {
  name: 'PopularTags',
  setup() {
    const router = useRouter()
    const loading = ref(true)
    const tags = ref([])

    const loadPopularTags = async () => {
      loading.value = true
      try {
        const response = await getPopularTags()
        // Show top 20 popular tags
        tags.value = response.slice(0, 20)
      } catch (error) {
        console.error('加载热门标签失败:', error)
        tags.value = []
      } finally {
        loading.value = false
      }
    }

    const getTagStyle = (tag) => {
      const color = tag.color || '#6B7280'
      
      // Convert hex to RGB
      const hexToRgb = (hex) => {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
        return result ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        } : { r: 107, g: 112, b: 128 }
      }
      
      const rgb = hexToRgb(color)
      
      return {
        backgroundColor: `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.1)`,
        color: color,
        border: `1px solid rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.3)`
      }
    }

    const handleTagClick = (tag) => {
      // Navigate to tag-filtered posts page
      router.push({
        path: '/tags/' + encodeURIComponent(tag.name)
      })
    }

    // 获取Font Awesome图标类名
    const getIconClass = (icon) => {
      if (!icon) return ''
      
      // 如果已经包含 fa-brands、fa-solid 等前缀，直接返回
      if (icon.includes('fa-brands') || icon.includes('fa-solid') || icon.includes('fa-regular') || 
          icon.includes('fab ') || icon.includes('fas ') || icon.includes('far ')) {
        return icon
      }
      
      // 如果只是 fa-xxx 格式，添加 fa-brands 前缀（大多数品牌图标）
      if (icon.startsWith('fa-')) {
        return `fa-brands ${icon}`
      }
      
      // 其他情况返回原值
      return icon
    }

    onMounted(() => {
      loadPopularTags()
    })

    return {
      loading,
      tags,
      getTagStyle,
      handleTagClick,
      getIconClass
    }
  }
}
</script>
