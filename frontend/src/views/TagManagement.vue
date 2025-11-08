<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <!-- Page Header -->
          <div class="mb-8">
            <h1 class="text-3xl font-bold text-gray-900 mb-2">标签管理</h1>
            <p class="text-gray-600">管理所有标签的属性：颜色、图标、描述和排序</p>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="space-y-4">
            <div v-for="i in 5" :key="i" class="card p-6 animate-pulse">
              <div class="h-6 bg-gray-200 rounded w-1/4 mb-4"></div>
              <div class="h-4 bg-gray-200 rounded w-3/4"></div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else-if="!tags.length" class="card p-12 text-center">
            <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">暂无标签</h3>
            <p class="text-gray-600">创建文章时会自动创建标签</p>
          </div>

          <!-- Tags List -->
          <div v-else class="space-y-4">
            <div
              v-for="tag in tags"
              :key="tag.id"
              class="card p-6 hover:shadow-lg transition-shadow"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <!-- Tag Display -->
                  <div class="flex items-center space-x-4 mb-4">
                    <div
                      class="inline-flex items-center px-4 py-2 rounded-lg text-lg font-semibold"
                      :style="getTagStyle(tag)"
                    >
                      <i v-if="tag.icon" :class="tag.icon" class="mr-2"></i>
                      {{ tag.name }}
                    </div>
                    <span class="text-sm text-gray-500">{{ tag.postCount }} 篇文章</span>
                  </div>

                  <!-- Edit Mode -->
                  <div v-if="editingId === tag.id" class="space-y-4 mt-4">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <!-- Color Input -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          颜色 (Color)
                        </label>
                        <div class="flex items-center space-x-2">
                          <input
                            v-model="editForm.color"
                            type="color"
                            class="h-10 w-20 rounded border border-gray-300 cursor-pointer"
                          />
                          <input
                            v-model="editForm.color"
                            type="text"
                            placeholder="#FF5733"
                            class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                          />
                        </div>
                      </div>

                      <!-- Icon Input -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          图标 (Icon) - Font Awesome类名
                        </label>
                        <input
                          v-model="editForm.icon"
                          type="text"
                          placeholder="fa-java"
                          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                        <p class="mt-1 text-xs text-gray-500">例如: fa-java, fa-python, fa-code</p>
                      </div>

                      <!-- Sort Order Input -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          排序 (Sort Order)
                        </label>
                        <input
                          v-model.number="editForm.sortOrder"
                          type="number"
                          min="0"
                          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                        <p class="mt-1 text-xs text-gray-500">数字越小越靠前</p>
                      </div>

                      <!-- Description Input -->
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                          描述 (Description)
                        </label>
                        <input
                          v-model="editForm.description"
                          type="text"
                          placeholder="标签描述"
                          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                      </div>
                    </div>

                    <!-- Action Buttons -->
                    <div class="flex items-center space-x-3">
                      <button
                        @click="saveTag(tag.id)"
                        :disabled="saving"
                        class="btn-primary px-6"
                      >
                        <span v-if="!saving">保存</span>
                        <span v-else class="flex items-center">
                          <div class="spinner w-4 h-4 mr-2"></div>
                          保存中...
                        </span>
                      </button>
                      <button
                        @click="cancelEdit"
                        class="btn-ghost px-6"
                      >
                        取消
                      </button>
                    </div>
                  </div>

                  <!-- View Mode Info -->
                  <div v-else class="text-sm text-gray-600 space-y-1">
                    <p v-if="tag.description">描述: {{ tag.description }}</p>
                    <p>颜色: {{ tag.color || '未设置' }}</p>
                    <p>图标: {{ tag.icon || '未设置' }}</p>
                    <p>排序: {{ tag.sortOrder !== null ? tag.sortOrder : '未设置' }}</p>
                    <p class="text-xs text-gray-400">创建者: {{ tag.createdByUsername }}</p>
                  </div>
                </div>

                <!-- Action Button -->
                <div v-if="editingId !== tag.id">
                  <button
                    @click="startEdit(tag)"
                    class="btn-secondary px-4 py-2"
                  >
                    <svg class="w-4 h-4 mr-1 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                    编辑
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-8">
            <Pagination
              :current-page="currentPage"
              :total-pages="totalPages"
              @page-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import Header from '@/components/Header.vue'
import Pagination from '@/components/Pagination.vue'
import { getTags, updateTag } from '@/api/tags'

export default {
  name: 'TagManagement',
  components: {
    Header,
    Pagination
  },
  setup() {
    const loading = ref(true)
    const saving = ref(false)
    const tags = ref([])
    const currentPage = ref(0)
    const totalPages = ref(0)
    const editingId = ref(null)
    const editForm = ref({
      name: '',
      description: '',
      color: '',
      icon: '',
      sortOrder: 0
    })

    const loadTags = async (page = 0) => {
      loading.value = true
      try {
        const response = await getTags({
          page: page,
          size: 20
        })
        tags.value = response.content || []
        totalPages.value = response.totalPages || 0
        currentPage.value = page
      } catch (error) {
        console.error('加载标签失败:', error)
        tags.value = []
      } finally {
        loading.value = false
      }
    }

    const getTagStyle = (tag) => {
      const color = tag.color || '#6B7280'
      
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
        backgroundColor: `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.15)`,
        color: color,
        border: `2px solid rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.3)`
      }
    }

    const startEdit = (tag) => {
      editingId.value = tag.id
      editForm.value = {
        name: tag.name,
        description: tag.description || '',
        color: tag.color || '#6B7280',
        icon: tag.icon || '',
        sortOrder: tag.sortOrder !== null ? tag.sortOrder : 0
      }
    }

    const cancelEdit = () => {
      editingId.value = null
      editForm.value = {
        name: '',
        description: '',
        color: '',
        icon: '',
        sortOrder: 0
      }
    }

    const saveTag = async (tagId) => {
      saving.value = true
      try {
        await updateTag(tagId, editForm.value)
        alert('标签更新成功!')
        editingId.value = null
        // Reload tags to show updated data
        await loadTags(currentPage.value)
      } catch (error) {
        console.error('更新标签失败:', error)
        alert(error?.response?.data?.message || '更新标签失败，请稍后重试')
      } finally {
        saving.value = false
      }
    }

    const handlePageChange = (page) => {
      loadTags(page)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    onMounted(() => {
      loadTags()
    })

    return {
      loading,
      saving,
      tags,
      currentPage,
      totalPages,
      editingId,
      editForm,
      getTagStyle,
      startEdit,
      cancelEdit,
      saveTag,
      handlePageChange
    }
  }
}
</script>
