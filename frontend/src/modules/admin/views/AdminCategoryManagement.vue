<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="分类管理"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>分类名称</label>
            <input type="text" v-model="searchForm.name" class="form-input" placeholder="搜索分类名称">
          </div>
          <div class="form-group">
            <label>创建者</label>
            <input type="text" v-model="searchForm.createdBy" class="form-input" placeholder="创建者用户名">
          </div>
          <div class="form-group">
            <label>创建开始日期</label>
            <input type="date" v-model="searchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>创建结束日期</label>
            <input type="date" v-model="searchForm.endDate" class="form-input">
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchCategories">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Category List -->
    <div class="card">
      <div class="card-header">
        <h3>分类列表 <span v-if="pagination.total > 0">({{ pagination.total }})</span></h3>
        <div class="header-actions">
          <button class="btn btn-primary btn-sm" @click="openCreateModal">+ 新建分类</button>
          <template v-if="selectedCategoryIds.length > 0">
            <span class="selected-count">已选择 {{ selectedCategoryIds.length }} 项</span>
            <button class="btn btn-danger btn-sm" @click="openBatchDeleteModal">批量删除</button>
          </template>
        </div>
      </div>
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">加载中...</div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th class="checkbox-col">
              <input
                type="checkbox"
                :checked="isAllSelected"
                @change="toggleSelectAll"
                :indeterminate="isPartialSelected"
              >
            </th>
            <th>ID</th>
            <th>分类名称</th>
            <th>描述</th>
            <th>颜色</th>
            <th>图标</th>
            <th>排序</th>
            <th>文章数</th>
            <th>创建者</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="categoryList.length === 0">
            <td colspan="11" class="empty-row">暂无分类数据</td>
          </tr>
          <tr v-for="category in categoryList" :key="category.id" :class="{ 'selected-row': isCategorySelected(category.id) }">
            <td class="checkbox-col">
              <input
                type="checkbox"
                :checked="isCategorySelected(category.id)"
                @change="toggleCategorySelection(category.id)"
              >
            </td>
            <td>{{ category.id }}</td>
            <td>
              <span class="tag-name-cell" @click="viewCategoryDetail(category)">
                <span v-if="category.color" class="tag-color-dot" :style="{ backgroundColor: category.color }"></span>
                {{ category.name }}
              </span>
            </td>
            <td class="desc-col">{{ truncateText(category.description, 30) }}</td>
            <td>
              <span v-if="category.color" class="color-preview" :style="{ backgroundColor: category.color }">{{ category.color }}</span>
              <span v-else class="text-muted">-</span>
            </td>
            <td>{{ category.icon || '-' }}</td>
            <td>{{ category.sortOrder != null ? category.sortOrder : '-' }}</td>
            <td>
              <span 
                class="post-count-badge clickable" 
                :class="getPostCountClass(category.postCount)"
                @click="toggleExpandPosts(category)"
                :title="category.postCount > 0 ? '点击展开/收起关联文章' : ''"
              >
                {{ category.postCount }}
                <span v-if="category.postCount > 0" class="expand-arrow">{{ isCategoryExpanded(category.id) ? '▲' : '▼' }}</span>
              </span>
              <div v-if="isCategoryExpanded(category.id) && expandedPostsMap[category.id]" class="expanded-posts">
                <div v-for="post in expandedPostsMap[category.id]" :key="post.postId" class="expanded-post-item">
                  <router-link :to="`/post/${post.postId}`" class="post-link-sm" target="_blank">
                    <span class="post-id-badge-sm">#{{ post.postId }}</span>
                    {{ truncateText(post.postTitle, 20) }}
                  </router-link>
                </div>
              </div>
              <div v-if="isCategoryExpanded(category.id) && expandedPostsLoading[category.id]" class="expanded-posts">
                <span class="text-muted">加载中...</span>
              </div>
            </td>
            <td>{{ category.createdByNickname || category.createdByUsername || '-' }}</td>
            <td>{{ formatDate(category.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewCategoryDetail(category)">详情</button>
              <button class="action-btn success" @click="openEditModal(category)">编辑</button>
              <button class="action-btn danger" @click="openDeleteModal(category)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div v-if="pagination.totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="pagination.page === 0"
          @click="changePage(pagination.page - 1)"
        >上一页</button>
        <span class="page-info">
          第 {{ pagination.page + 1 }} / {{ pagination.totalPages }} 页
        </span>
        <button 
          class="page-btn" 
          :disabled="pagination.page >= pagination.totalPages - 1"
          @click="changePage(pagination.page + 1)"
        >下一页</button>
      </div>
    </div>

    <!-- Category Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>分类详情</h3>
          <button class="close-btn" @click="closeDetailModal">&times;</button>
        </div>
        <div class="modal-body" v-if="selectedCategory">
          <div class="detail-row">
            <span class="label">ID:</span>
            <span class="value">{{ selectedCategory.id }}</span>
          </div>
          <div class="detail-row">
            <span class="label">名称:</span>
            <span class="value">
              <span v-if="selectedCategory.color" class="tag-color-dot" :style="{ backgroundColor: selectedCategory.color }"></span>
              {{ selectedCategory.name }}
            </span>
          </div>
          <div class="detail-row">
            <span class="label">描述:</span>
            <span class="value">{{ selectedCategory.description || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">颜色:</span>
            <span class="value">
              <span v-if="selectedCategory.color" class="color-preview" :style="{ backgroundColor: selectedCategory.color }">{{ selectedCategory.color }}</span>
              <span v-else>-</span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">图标:</span>
            <span class="value">{{ selectedCategory.icon || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">排序:</span>
            <span class="value">{{ selectedCategory.sortOrder != null ? selectedCategory.sortOrder : '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">文章数:</span>
            <span class="value">
              <span class="post-count-badge" :class="getPostCountClass(selectedCategory.postCount)">
                {{ selectedCategory.postCount }}
              </span>
            </span>
          </div>
          <!-- 添加文章 -->
          <div class="detail-row detail-row-block">
            <span class="label">添加文章:</span>
            <div class="assign-post-row">
              <div class="post-search-wrapper">
                <input type="text" v-model="postSearchQuery" class="form-input assign-post-input" placeholder="输入文章ID或搜索标题" @input="handlePostSearchInput" @focus="showPostSearchResults = postSearchResults.length > 0">
                <div v-if="postSearchLoading" class="post-search-loading">搜索中...</div>
                <div v-if="showPostSearchResults && postSearchResults.length > 0" class="post-search-dropdown">
                  <div v-for="post in postSearchResults" :key="post.postId" class="post-search-item" @click="selectPostFromSearch(post)">
                    <span class="post-id-badge">#{{ post.postId }}</span>
                    <span class="post-search-title">{{ post.postTitle }}</span>
                  </div>
                </div>
              </div>
              <button class="btn btn-primary btn-sm" @click="handleAssignPost" :disabled="!assignPostId">添加</button>
            </div>
          </div>
          <!-- 关联文章列表 -->
          <div v-if="selectedCategory.posts && selectedCategory.posts.length > 0" class="detail-row detail-row-block">
            <span class="label">关联文章:</span>
            <div class="associated-posts-list">
              <div v-for="post in selectedCategory.posts" :key="post.postId" class="associated-post-item">
                <router-link :to="`/post/${post.postId}`" class="post-link" target="_blank">
                  <span class="post-id-badge">#{{ post.postId }}</span>
                  {{ post.postTitle }}
                </router-link>
                <button class="action-btn danger" @click="handleRemovePost(post.postId)">移除</button>
              </div>
            </div>
          </div>
          <div v-else class="detail-row">
            <span class="label">关联文章:</span>
            <span class="value text-muted">暂无关联文章</span>
          </div>
          <div class="detail-row">
            <span class="label">创建者:</span>
            <span class="value">{{ selectedCategory.createdByNickname || selectedCategory.createdByUsername || '-' }} (@{{ selectedCategory.createdByUsername }})</span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDateTime(selectedCategory.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">更新时间:</span>
            <span class="value">{{ formatDateTime(selectedCategory.updatedAt) }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="openEditModalFromDetail">编辑</button>
          <button class="btn btn-danger" @click="openDeleteModalFromDetail">删除</button>
          <button class="btn btn-secondary" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showFormModal" class="modal-overlay" @click.self="closeFormModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑分类' : '创建分类' }}</h3>
          <button class="close-btn" @click="closeFormModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>分类名称 <span class="required">*</span></label>
            <input type="text" v-model="categoryForm.name" class="form-input" placeholder="请输入分类名称">
          </div>
          <div class="form-group">
            <label>分类描述 <span class="optional">(可选)</span></label>
            <textarea v-model="categoryForm.description" class="form-input form-textarea" placeholder="请输入分类描述" rows="3"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>颜色 <span class="optional">(可选)</span></label>
              <div class="color-input-group">
                <input type="color" v-model="colorPickerValue" class="color-picker" @input="onColorPick">
                <input type="text" v-model="categoryForm.color" class="form-input" placeholder="#FF5733" maxlength="7">
              </div>
            </div>
            <div class="form-group">
              <label>图标 <span class="optional">(可选)</span></label>
              <input type="text" v-model="categoryForm.icon" class="form-input" placeholder="输入图标名称，如 fa-solid fa-folder">
            </div>
            <div class="form-group">
              <label>排序 <span class="optional">(可选)</span></label>
              <input type="number" v-model.number="categoryForm.sortOrder" class="form-input" placeholder="数字越小越靠前" min="0">
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeFormModal">取消</button>
          <button 
            class="btn btn-primary" 
            @click="submitCategoryForm"
            :disabled="!categoryForm.name || categoryForm.name.trim() === ''"
          >{{ isEditMode ? '保存修改' : '创建分类' }}</button>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>{{ isBatchDelete ? '批量删除分类' : '删除分类' }}</h3>
          <button class="close-btn" @click="closeDeleteModal">&times;</button>
        </div>
        <div class="modal-body">
          <p class="warning-text">
            {{ isBatchDelete 
              ? `确定要删除选中的 ${selectedCategoryIds.length} 个分类吗？` 
              : '确定要删除此分类吗？' 
            }}
          </p>
          <p class="sub-text">
            删除分类将永久移除该分类，关联文章的分类字段将被清空，此操作不可恢复。
          </p>

          <!-- 单个删除时显示关联文章数量警告 -->
          <div v-if="!isBatchDelete && deleteTargetCategory" class="form-group">
            <p v-if="deleteTargetCategory.postCount > 0" class="warning-text">
              ⚠ 此分类下有 {{ deleteTargetCategory.postCount }} 篇关联文章，删除后这些文章的分类将被清空。
            </p>
            <p v-else class="text-muted">此分类暂无关联文章。</p>
          </div>

          <div class="form-group">
            <label>删除理由 <span class="required">*</span></label>
            <textarea v-model="deleteForm.reason" class="form-input form-textarea" placeholder="请填写删除理由" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteModal">取消</button>
          <button 
            class="btn btn-danger" 
            @click="confirmDelete"
            :disabled="!deleteForm.reason || deleteForm.reason.trim() === ''"
          >确认删除</button>
        </div>
      </div>
    </div>

    <!-- Quick Links -->
    <div class="quick-links">
      <router-link to="/" class="quick-link">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>
        返回前台首页
      </router-link>
      <button @click="handleLogout" class="quick-link">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
        退出登录
      </button>
    </div>
  </AdminLayout>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getAdminCategories, getAdminCategoryDetail, createAdminCategory, updateAdminCategory, assignPostToCategory, removePostFromCategory, executeCategoryAction, searchPostsForCategory } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminCategoryManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= State =======================
    const categoryList = ref([])
    const loading = ref(false)
    const pagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const searchForm = ref({
      name: '',
      createdBy: '',
      startDate: '',
      endDate: ''
    })

    // Selection state
    const selectedCategoryIds = ref([])

    // Detail modal state
    const showDetailModal = ref(false)
    const selectedCategory = ref(null)
    const assignPostId = ref(null)
    const postSearchQuery = ref('')
    const postSearchResults = ref([])
    const postSearchLoading = ref(false)
    const showPostSearchResults = ref(false)
    let postSearchTimer = null

    // Create/Edit modal state
    const showFormModal = ref(false)
    const isEditMode = ref(false)
    const editingCategoryId = ref(null)
    const colorPickerValue = ref('#1890ff')
    const categoryForm = ref({
      name: '',
      description: '',
      color: '',
      icon: '',
      sortOrder: null
    })

    // Delete modal state
    const showDeleteModal = ref(false)
    const isBatchDelete = ref(false)
    const deleteTargetCategory = ref(null)
    const deleteForm = ref({
      reason: ''
    })

    // Expanded posts in table
    const expandedCategoryIds = ref([])
    const expandedPostsMap = ref({})
    const expandedPostsLoading = ref({})

    // Menu items (consistent with other admin views)
    const menuItems = [
      { id: 'dashboard', path: '/admin', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin/categories', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin/tags', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin/comments', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'media', path: '/admin', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'categories'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    // ======================= Helper Methods =======================

    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }

    const formatDateTime = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }

    const truncateText = (text, maxLength) => {
      if (!text) return '-'
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    const getPostCountClass = (count) => {
      if (count >= 10) return 'count-high'
      if (count >= 5) return 'count-medium'
      return 'count-low'
    }

    // ======================= Data Loading =======================

    const loadCategories = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.value.page,
          size: pagination.value.size,
          name: searchForm.value.name || undefined,
          createdBy: searchForm.value.createdBy || undefined,
          startDate: searchForm.value.startDate || undefined,
          endDate: searchForm.value.endDate || undefined
        }
        const response = await getAdminCategories(params)
        categoryList.value = response.content || []
        pagination.value.total = response.totalElements || 0
        pagination.value.totalPages = response.totalPages || 0
        selectedCategoryIds.value = []
      } catch (error) {
        console.error('Failed to load categories:', error)
        alert('加载分类列表失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    const searchCategories = () => {
      pagination.value.page = 0
      loadCategories()
    }

    const resetSearch = () => {
      searchForm.value = {
        name: '',
        createdBy: '',
        startDate: '',
        endDate: ''
      }
      pagination.value.page = 0
      loadCategories()
    }

    const changePage = (newPage) => {
      pagination.value.page = newPage
      loadCategories()
    }

    // ======================= Detail Modal =======================

    const viewCategoryDetail = async (category) => {
      try {
        const detail = await getAdminCategoryDetail(category.id)
        selectedCategory.value = detail
        assignPostId.value = null
        postSearchQuery.value = ''
        postSearchResults.value = []
        showPostSearchResults.value = false
        showDetailModal.value = true
      } catch (error) {
        console.error('Failed to load category detail:', error)
        alert('加载分类详情失败')
      }
    }

    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedCategory.value = null
      assignPostId.value = null
      postSearchQuery.value = ''
      postSearchResults.value = []
      showPostSearchResults.value = false
    }

    const handlePostSearchInput = () => {
      const query = postSearchQuery.value.trim()
      if (!query) {
        postSearchResults.value = []
        showPostSearchResults.value = false
        assignPostId.value = null
        return
      }

      // If input is a pure number, treat as post ID
      if (/^\d+$/.test(query)) {
        assignPostId.value = parseInt(query)
        postSearchResults.value = []
        showPostSearchResults.value = false
        return
      }

      // Otherwise search by title with debounce
      assignPostId.value = null
      if (postSearchTimer) clearTimeout(postSearchTimer)
      postSearchTimer = setTimeout(async () => {
        if (query.length < 2) return
        postSearchLoading.value = true
        try {
          const results = await searchPostsForCategory(query)
          postSearchResults.value = results || []
          showPostSearchResults.value = postSearchResults.value.length > 0
        } catch (error) {
          console.error('Failed to search posts:', error)
          postSearchResults.value = []
        } finally {
          postSearchLoading.value = false
        }
      }, 300)
    }

    const selectPostFromSearch = (post) => {
      assignPostId.value = post.postId
      postSearchQuery.value = `#${post.postId} ${post.postTitle}`
      postSearchResults.value = []
      showPostSearchResults.value = false
    }

    const handleAssignPost = async () => {
      if (!assignPostId.value || !selectedCategory.value) return
      try {
        await assignPostToCategory(selectedCategory.value.id, assignPostId.value)
        alert('文章添加成功')
        const detail = await getAdminCategoryDetail(selectedCategory.value.id)
        selectedCategory.value = detail
        assignPostId.value = null
        postSearchQuery.value = ''
        postSearchResults.value = []
        showPostSearchResults.value = false
        await loadCategories()
      } catch (error) {
        console.error('Failed to assign post:', error)
        alert('添加文章失败: ' + (error.response?.data || error.message))
      }
    }

    const handleRemovePost = async (postId) => {
      if (!selectedCategory.value) return
      try {
        await removePostFromCategory(selectedCategory.value.id, postId)
        alert('文章已移除')
        const detail = await getAdminCategoryDetail(selectedCategory.value.id)
        selectedCategory.value = detail
        await loadCategories()
      } catch (error) {
        console.error('Failed to remove post:', error)
        alert('移除文章失败: ' + (error.response?.data || error.message))
      }
    }

    // ======================= Create/Edit Modal =======================

    const openCreateModal = () => {
      isEditMode.value = false
      editingCategoryId.value = null
      categoryForm.value = { name: '', description: '', color: '', icon: '', sortOrder: null }
      colorPickerValue.value = '#1890ff'
      showFormModal.value = true
    }

    const openEditModal = (category) => {
      isEditMode.value = true
      editingCategoryId.value = category.id
      categoryForm.value = {
        name: category.name,
        description: category.description || '',
        color: category.color || '',
        icon: category.icon || '',
        sortOrder: category.sortOrder
      }
      colorPickerValue.value = category.color || '#1890ff'
      showFormModal.value = true
    }

    const openEditModalFromDetail = () => {
      if (!selectedCategory.value) return
      closeDetailModal()
      openEditModal(selectedCategory.value)
    }

    const closeFormModal = () => {
      showFormModal.value = false
      isEditMode.value = false
      editingCategoryId.value = null
    }

    const onColorPick = () => {
      categoryForm.value.color = colorPickerValue.value
    }

    const submitCategoryForm = async () => {
      if (!categoryForm.value.name || categoryForm.value.name.trim() === '') {
        alert('请填写分类名称')
        return
      }

      const data = {
        name: categoryForm.value.name.trim(),
        description: categoryForm.value.description || undefined,
        color: categoryForm.value.color || undefined,
        icon: categoryForm.value.icon || undefined,
        sortOrder: categoryForm.value.sortOrder != null ? categoryForm.value.sortOrder : undefined
      }

      try {
        if (isEditMode.value) {
          await updateAdminCategory(editingCategoryId.value, data)
          alert('分类更新成功')
        } else {
          await createAdminCategory(data)
          alert('分类创建成功')
        }
        closeFormModal()
        await loadCategories()
      } catch (error) {
        console.error('Failed to save category:', error)
        alert((isEditMode.value ? '更新' : '创建') + '分类失败: ' + (error.response?.data || error.message))
      }
    }

    // ======================= Delete Modal =======================

    const openDeleteModal = (category) => {
      deleteTargetCategory.value = category
      isBatchDelete.value = false
      deleteForm.value = { reason: '' }
      showDeleteModal.value = true
    }

    const openDeleteModalFromDetail = () => {
      if (!selectedCategory.value) return
      const category = selectedCategory.value
      closeDetailModal()
      deleteTargetCategory.value = category
      isBatchDelete.value = false
      deleteForm.value = { reason: '' }
      showDeleteModal.value = true
    }

    const openBatchDeleteModal = () => {
      if (selectedCategoryIds.value.length === 0) return
      isBatchDelete.value = true
      deleteTargetCategory.value = null
      deleteForm.value = { reason: '' }
      showDeleteModal.value = true
    }

    const closeDeleteModal = () => {
      showDeleteModal.value = false
      deleteTargetCategory.value = null
      isBatchDelete.value = false
    }

    const confirmDelete = async () => {
      if (!deleteForm.value.reason || deleteForm.value.reason.trim() === '') {
        alert('请填写删除理由')
        return
      }

      const ids = isBatchDelete.value ? selectedCategoryIds.value : [deleteTargetCategory.value.id]

      const requestData = {
        action: 'DELETE',
        categoryIds: ids,
        reason: deleteForm.value.reason
      }

      try {
        await executeCategoryAction(requestData)
        closeDeleteModal()
        selectedCategoryIds.value = []
        await loadCategories()
        alert(`成功删除 ${ids.length} 个分类`)
      } catch (error) {
        console.error('Failed to delete categories:', error)
        alert('删除失败: ' + (error.response?.data || error.message))
      }
    }

    // ======================= Expand Posts in Table =======================

    const isCategoryExpanded = (categoryId) => {
      return expandedCategoryIds.value.includes(categoryId)
    }

    const toggleExpandPosts = async (category) => {
      if (category.postCount === 0) return

      const idx = expandedCategoryIds.value.indexOf(category.id)
      if (idx !== -1) {
        expandedCategoryIds.value.splice(idx, 1)
        return
      }

      if (!expandedPostsMap.value[category.id]) {
        expandedPostsLoading.value = { ...expandedPostsLoading.value, [category.id]: true }
        try {
          const detail = await getAdminCategoryDetail(category.id)
          expandedPostsMap.value = { ...expandedPostsMap.value, [category.id]: detail.posts || [] }
        } catch (error) {
          console.error('Failed to load category posts:', error)
          expandedPostsMap.value = { ...expandedPostsMap.value, [category.id]: [] }
        } finally {
          expandedPostsLoading.value = { ...expandedPostsLoading.value, [category.id]: false }
        }
      }
      expandedCategoryIds.value.push(category.id)
    }

    // ======================= Batch Selection =======================

    const isCategorySelected = (categoryId) => {
      return selectedCategoryIds.value.includes(categoryId)
    }

    const toggleCategorySelection = (categoryId) => {
      const index = selectedCategoryIds.value.indexOf(categoryId)
      if (index === -1) {
        selectedCategoryIds.value.push(categoryId)
      } else {
        selectedCategoryIds.value.splice(index, 1)
      }
    }

    const isAllSelected = computed(() => {
      return categoryList.value.length > 0 && categoryList.value.every(c => selectedCategoryIds.value.includes(c.id))
    })

    const isPartialSelected = computed(() => {
      const selectedCount = categoryList.value.filter(c => selectedCategoryIds.value.includes(c.id)).length
      return selectedCount > 0 && selectedCount < categoryList.value.length
    })

    const toggleSelectAll = () => {
      if (isAllSelected.value) {
        selectedCategoryIds.value = []
      } else {
        selectedCategoryIds.value = categoryList.value.map(c => c.id)
      }
    }

    onMounted(() => {
      loadCategories()
    })

    onBeforeUnmount(() => {
      if (postSearchTimer) clearTimeout(postSearchTimer)
    })

    return {
      menuItems,
      isActiveRoute,
      handleLogout,
      // Category list
      categoryList,
      loading,
      pagination,
      searchForm,
      formatDate,
      formatDateTime,
      truncateText,
      getPostCountClass,
      searchCategories,
      resetSearch,
      changePage,
      // Detail modal
      showDetailModal,
      selectedCategory,
      assignPostId,
      postSearchQuery,
      postSearchResults,
      postSearchLoading,
      showPostSearchResults,
      viewCategoryDetail,
      closeDetailModal,
      handlePostSearchInput,
      selectPostFromSearch,
      handleAssignPost,
      handleRemovePost,
      // Create/Edit modal
      showFormModal,
      isEditMode,
      categoryForm,
      colorPickerValue,
      openCreateModal,
      openEditModal,
      openEditModalFromDetail,
      closeFormModal,
      onColorPick,
      submitCategoryForm,
      // Delete modal
      showDeleteModal,
      isBatchDelete,
      deleteTargetCategory,
      deleteForm,
      openDeleteModal,
      openDeleteModalFromDetail,
      openBatchDeleteModal,
      closeDeleteModal,
      confirmDelete,
      // Expand posts in table
      expandedPostsMap,
      expandedPostsLoading,
      isCategoryExpanded,
      toggleExpandPosts,
      // Batch selection
      selectedCategoryIds,
      isCategorySelected,
      toggleCategorySelection,
      isAllSelected,
      isPartialSelected,
      toggleSelectAll
    }
  }
}
</script>

<style scoped>

/* Cards */
.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1d2e;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

/* Search Card */
.search-card .search-form {
  padding: 20px 24px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-row .form-group {
  flex: 1;
  min-width: 180px;
  margin-bottom: 0;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
}

.color-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.color-picker {
  width: 40px;
  height: 40px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  padding: 2px;
}

.search-btn-group {
  display: flex;
  align-items: flex-end;
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: #1890ff;
  color: #fff;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-primary:disabled {
  background: #91d5ff;
  cursor: not-allowed;
}

.btn-secondary {
  background: #fff;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-success {
  background: #52c41a;
  color: #fff;
}

.btn-success:hover {
  background: #73d13d;
}

.btn-danger {
  background: #f5222d;
  color: #fff;
}

.btn-danger:hover {
  background: #ff4d4f;
}

.btn-danger:disabled {
  background: #ffccc7;
  cursor: not-allowed;
}

.btn-warning {
  background: #faad14;
  color: #fff;
}

.btn-warning:hover {
  background: #ffc53d;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

/* Data Table */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e8e8e8;
  font-size: 14px;
}

.data-table th {
  background: #fafafa;
  font-weight: 600;
  color: #666;
}

.data-table td {
  color: #333;
}

.data-table .checkbox-col {
  width: 40px;
  text-align: center;
}

.data-table .checkbox-col input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.selected-row {
  background-color: #e6f7ff;
}

.desc-col {
  max-width: 200px;
  color: #666;
}

.tag-name-cell {
  cursor: pointer;
  color: #1890ff;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tag-name-cell:hover {
  text-decoration: underline;
}

.tag-color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.color-preview {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-family: monospace;
}

.text-muted {
  color: #999;
}

/* Post count badge */
.post-count-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}

.count-high {
  background: #fff2f0;
  color: #f5222d;
}

.count-medium {
  background: #fffbe6;
  color: #faad14;
}

.count-low {
  background: #f6ffed;
  color: #52c41a;
}

/* Badges */
.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background: #f6ffed;
  color: #52c41a;
}

.badge-danger {
  background: #fff2f0;
  color: #f5222d;
}

.badge-warning {
  background: #fffbe6;
  color: #faad14;
}

.badge-info {
  background: #e6f7ff;
  color: #1890ff;
}

.badge-default {
  background: #fafafa;
  color: #666;
}

/* Action Buttons */
.actions {
  white-space: nowrap;
}

.action-btn {
  padding: 4px 8px;
  margin-right: 4px;
  border: none;
  background: transparent;
  color: #1890ff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #e6f7ff;
  border-radius: 4px;
}

.action-btn.danger {
  color: #f5222d;
}

.action-btn.danger:hover {
  background: #fff2f0;
}

.action-btn.success {
  color: #52c41a;
}

.action-btn.success:hover {
  background: #f6ffed;
}

.action-btn.warning {
  color: #faad14;
}

.action-btn.warning:hover {
  background: #fffbe6;
}

/* Loading & Empty */
.loading-container {
  padding: 40px;
  text-align: center;
}

.loading-spinner {
  color: #666;
  font-size: 14px;
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 40px !important;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #e8e8e8;
}

.page-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.page-btn:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-lg {
  width: 700px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  width: 100px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
}

.detail-row .value {
  flex: 1;
  color: #333;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.warning-text {
  color: #f5222d;
  font-weight: 500;
  margin-bottom: 8px;
}

.sub-text {
  color: #666;
  font-size: 13px;
  margin-bottom: 16px;
}

.required {
  color: #f5222d;
}

.optional {
  color: #999;
  font-weight: normal;
}

/* Quick Links */
.quick-links {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.quick-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-link:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.quick-link svg {
  width: 18px;
  height: 18px;
}

/* Associated posts in detail modal */
.detail-row-block {
  flex-direction: column;
  gap: 8px;
}

.detail-row-block .label {
  width: auto;
}

.associated-posts-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 4px;
}

.associated-post-item {
  padding: 4px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.associated-post-item:hover {
  background: #f5f5f5;
}

.post-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
}

.post-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

.post-id-badge {
  display: inline-block;
  background: #e6f7ff;
  color: #1890ff;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

/* Assign post input */
.assign-post-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.post-search-wrapper {
  position: relative;
  flex: 1;
}

.assign-post-input {
  width: 100%;
}

.post-search-loading {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #999;
}

.post-search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-top: none;
  border-radius: 0 0 6px 6px;
  max-height: 240px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-search-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 13px;
}

.post-search-item:hover {
  background: #f0f5ff;
}

.post-search-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Expanded posts in table */
.post-count-badge.clickable {
  cursor: pointer;
}

.post-count-badge.clickable:hover {
  opacity: 0.8;
}

.expand-arrow {
  font-size: 10px;
  margin-left: 4px;
}

.expanded-posts {
  margin-top: 6px;
  padding: 6px 0;
  border-top: 1px solid #f0f0f0;
}

.expanded-post-item {
  padding: 2px 0;
}

.post-link-sm {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #1890ff;
  text-decoration: none;
  font-size: 12px;
}

.post-link-sm:hover {
  color: #40a9ff;
  text-decoration: underline;
}

.post-id-badge-sm {
  display: inline-block;
  background: #e6f7ff;
  color: #1890ff;
  padding: 0 4px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
}
</style>
