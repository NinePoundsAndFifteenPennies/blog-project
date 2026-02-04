<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="文章管理"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetPostSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>文章标题</label>
            <input type="text" v-model="postSearchForm.title" class="form-input" placeholder="搜索标题">
          </div>
          <div class="form-group">
            <label>作者</label>
            <input type="text" v-model="postSearchForm.author" class="form-input" placeholder="用户名或昵称">
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="postSearchForm.status" class="form-input">
              <option value="">全部</option>
              <option value="DRAFT">草稿</option>
              <option value="PENDING_REVIEW">待审核</option>
              <option value="PUBLISHED">已发布</option>
              <option value="REJECTED">已拒绝</option>
            </select>
          </div>
          <div class="form-group">
            <label>标签</label>
            <input type="text" v-model="postSearchForm.tag" class="form-input" placeholder="标签名称">
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>创建开始日期</label>
            <input type="date" v-model="postSearchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>创建结束日期</label>
            <input type="date" v-model="postSearchForm.endDate" class="form-input">
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchPosts">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Post List -->
    <div class="card">
      <div class="card-header">
        <h3>文章列表 <span v-if="postPagination.total > 0">({{ postPagination.total }})</span></h3>
        <div class="header-actions" v-if="selectedPostIds.length > 0">
          <span class="selected-count">已选择 {{ selectedPostIds.length }} 项</span>
          <button class="btn btn-success btn-sm" @click="batchApprove">批量通过</button>
          <button class="btn btn-warning btn-sm" @click="openBatchRejectModal">批量拒绝</button>
          <button class="btn btn-danger btn-sm" @click="openBatchDeleteModal">批量删除</button>
        </div>
      </div>
      <div v-if="postsLoading" class="loading-container">
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
            <th>标题</th>
            <th>作者</th>
            <th>状态</th>
            <th>浏览量</th>
            <th>点赞数</th>
            <th>评论数</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="postList.length === 0">
            <td colspan="10" class="empty-row">暂无文章数据</td>
          </tr>
          <tr v-for="post in postList" :key="post.id" :class="{ 'selected-row': isPostSelected(post.id) }">
            <td class="checkbox-col">
              <input
                type="checkbox"
                :checked="isPostSelected(post.id)"
                @change="togglePostSelection(post.id)"
              >
            </td>
            <td>{{ post.id }}</td>
            <td class="title-col">
              <span class="post-title" @click="viewPostDetail(post)">{{ truncateText(post.title, 30) }}</span>
            </td>
            <td>
              <span class="author-info">
                <span class="author-name">{{ post.authorNickname || post.authorUsername }}</span>
                <span v-if="!post.authorEnabled" class="badge badge-danger ml-1">已禁用</span>
              </span>
            </td>
            <td>
              <span :class="['badge', getStatusBadgeClass(post.status)]">
                {{ getStatusLabel(post.status) }}
              </span>
            </td>
            <td>{{ post.viewCount }}</td>
            <td>{{ post.likeCount }}</td>
            <td>{{ post.commentCount }}</td>
            <td>{{ formatDate(post.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewPostDetail(post)">详情</button>
              <button
                v-if="canApprove(post)"
                class="action-btn success"
                @click="approvePost(post)"
              >通过</button>
              <button
                v-if="canReject(post)"
                class="action-btn warning"
                @click="openRejectModal(post)"
              >拒绝</button>
              <button
                class="action-btn danger"
                @click="openDeleteModal(post)"
              >删除</button>
            </td>
          </tr>
        </tbody>
      </table>
          <!-- Pagination -->
          <div v-if="postPagination.totalPages > 1" class="pagination">
            <button 
              class="page-btn" 
              :disabled="postPagination.page === 0"
              @click="changePostPage(postPagination.page - 1)"
            >上一页</button>
            <span class="page-info">
              第 {{ postPagination.page + 1 }} / {{ postPagination.totalPages }} 页
            </span>
            <button 
              class="page-btn" 
              :disabled="postPagination.page >= postPagination.totalPages - 1"
              @click="changePostPage(postPagination.page + 1)"
            >下一页</button>
          </div>
        </div>

        <!-- Post Detail Modal -->
        <div v-if="showPostModal" class="modal-overlay" @click.self="closePostModal">
          <div class="modal-content modal-lg">
            <div class="modal-header">
              <h3>文章详情</h3>
              <button class="close-btn" @click="closePostModal">&times;</button>
            </div>
            <div class="modal-body" v-if="selectedPost">
              <div class="post-detail-grid">
                <div class="detail-row">
                  <span class="label">ID:</span>
                  <span class="value">{{ selectedPost.id }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">标题:</span>
                  <span class="value">{{ selectedPost.title }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">作者:</span>
                  <span class="value">{{ selectedPost.authorNickname }} (@{{ selectedPost.authorUsername }})</span>
                </div>
                <div class="detail-row">
                  <span class="label">状态:</span>
                  <span class="value">
                    <span :class="['badge', getStatusBadgeClass(selectedPost.status)]">
                      {{ getStatusLabel(selectedPost.status) }}
                    </span>
                  </span>
                </div>
                <div class="detail-row">
                  <span class="label">浏览量:</span>
                  <span class="value">{{ selectedPost.viewCount }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">点赞数:</span>
                  <span class="value">{{ selectedPost.likeCount }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">评论数:</span>
                  <span class="value">{{ selectedPost.commentCount }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">创建时间:</span>
                  <span class="value">{{ formatDateTime(selectedPost.createdAt) }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">更新时间:</span>
                  <span class="value">{{ formatDateTime(selectedPost.updatedAt) }}</span>
                </div>
                <div class="detail-row" v-if="selectedPost.publishedAt">
                  <span class="label">发布时间:</span>
                  <span class="value">{{ formatDateTime(selectedPost.publishedAt) }}</span>
                </div>
                <div class="detail-row" v-if="selectedPost.tags && selectedPost.tags.length">
                  <span class="label">标签:</span>
                  <span class="value">
                    <span v-for="tag in selectedPost.tags" :key="tag.id" class="tag-badge">{{ tag.name }}</span>
                  </span>
                </div>
                <div class="detail-row" v-if="selectedPost.category">
                  <span class="label">分类:</span>
                  <span class="value">{{ selectedPost.category.name }}</span>
                </div>
              </div>
              
              <!-- Previous version info for PENDING_REVISION -->
              <div v-if="selectedPost.status === 'PENDING_REVISION' && selectedPost.previousTitle" class="previous-version">
                <h4>修改前版本（前台展示内容）</h4>
                <div class="detail-row">
                  <span class="label">原标题:</span>
                  <span class="value">{{ selectedPost.previousTitle }}</span>
                </div>
              </div>

              <!-- Rejection Form Info -->
              <div v-if="selectedPost.rejectionForm" class="rejection-info">
                <h4>拒绝/删除记录</h4>
                <div class="detail-row">
                  <span class="label">表单标题:</span>
                  <span class="value">{{ selectedPost.rejectionForm.title }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">理由:</span>
                  <span class="value">{{ selectedPost.rejectionForm.reason }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">操作人:</span>
                  <span class="value">{{ selectedPost.rejectionForm.adminNickname }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">操作时间:</span>
                  <span class="value">{{ formatDateTime(selectedPost.rejectionForm.createdAt) }}</span>
                </div>
              </div>

              <!-- Content Preview -->
              <div class="content-preview">
                <h4>文章内容预览</h4>
                <div class="content-box">{{ truncateText(stripHtml(selectedPost.content), 500) }}</div>
              </div>
            </div>
            <div class="modal-footer">
              <button v-if="selectedPost && canApprove(selectedPost)" class="btn btn-success" @click="approvePost(selectedPost); closePostModal()">通过</button>
              <button v-if="selectedPost && canReject(selectedPost)" class="btn btn-warning" @click="openRejectModal(selectedPost); closePostModal()">拒绝</button>
              <button v-if="selectedPost" class="btn btn-danger" @click="openDeleteModal(selectedPost); closePostModal()">删除</button>
              <button class="btn btn-secondary" @click="closePostModal">关闭</button>
            </div>
          </div>
        </div>

        <!-- Reject/Delete Form Modal -->
        <div v-if="showActionModal" class="modal-overlay" @click.self="closeActionModal">
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ actionModalTitle }}</h3>
              <button class="close-btn" @click="closeActionModal">&times;</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label>表单标题（可选，用于提示字段显示）</label>
                <input type="text" v-model="actionForm.formTitle" class="form-input" placeholder="例如：文章审核拒绝通知">
              </div>
              <div class="form-group">
                <label>理由 <span class="required">*</span></label>
                <textarea v-model="actionForm.reason" class="form-input" rows="4" placeholder="请填写详细理由..."></textarea>
              </div>
              <div class="form-group">
                <label>扩展信息（可选）</label>
                <div v-for="(field, index) in actionForm.extraFieldsList" :key="index" class="extra-field-row">
                  <input type="text" v-model="field.fieldName" class="form-input" placeholder="字段名">
                  <input type="text" v-model="field.fieldValue" class="form-input" placeholder="字段值">
                  <button class="btn btn-sm btn-danger" @click="removeExtraField(index)">删除</button>
                </div>
                <button class="btn btn-sm btn-secondary" @click="addExtraField">+ 添加扩展字段</button>
              </div>
              <div class="affected-posts" v-if="actionTargetPosts.length > 1">
                <label>将影响以下 {{ actionTargetPosts.length }} 篇文章：</label>
                <ul>
                  <li v-for="post in actionTargetPosts" :key="post.id">{{ post.id }} - {{ truncateText(post.title, 40) }}</li>
                </ul>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-secondary" @click="closeActionModal">取消</button>
              <button 
                :class="['btn', actionType === 'DELETE' ? 'btn-danger' : 'btn-warning']" 
                @click="confirmAction"
                :disabled="!actionForm.reason"
              >确认{{ actionType === 'DELETE' ? '删除' : '拒绝' }}</button>
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
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getPosts, getPostDetail, executePostAction } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminPostManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= Post Management State =======================
    const postList = ref([])
    const postsLoading = ref(false)
    const postPagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const postSearchForm = ref({
      title: '',
      author: '',
      status: '',
      tag: '',
      startDate: '',
      endDate: ''
    })
    const showPostModal = ref(false)
    const selectedPost = ref(null)
    const selectedPostIds = ref([])

    // Action Modal State
    const showActionModal = ref(false)
    const actionType = ref('')  // 'REJECT' or 'DELETE'
    const actionTargetPosts = ref([])
    const actionForm = ref({
      formTitle: '',
      reason: '',
      extraFieldsList: []
    })

    const menuItems = [
      { id: 'dashboard', path: '/admin', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'media', path: '/admin', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'articles'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    const actionModalTitle = computed(() => {
      if (actionType.value === 'DELETE') {
        return actionTargetPosts.value.length > 1 ? `批量删除 ${actionTargetPosts.value.length} 篇文章` : '删除文章'
      }
      return actionTargetPosts.value.length > 1 ? `批量拒绝 ${actionTargetPosts.value.length} 篇文章` : '拒绝文章'
    })

    // ======================= Selection Methods =======================

    const isAllSelected = computed(() => {
      return postList.value.length > 0 && selectedPostIds.value.length === postList.value.length
    })

    const isPartialSelected = computed(() => {
      return selectedPostIds.value.length > 0 && selectedPostIds.value.length < postList.value.length
    })

    const isPostSelected = (id) => {
      return selectedPostIds.value.includes(id)
    }

    const toggleSelectAll = () => {
      if (isAllSelected.value) {
        selectedPostIds.value = []
      } else {
        selectedPostIds.value = postList.value.map(p => p.id)
      }
    }

    const togglePostSelection = (id) => {
      const index = selectedPostIds.value.indexOf(id)
      if (index === -1) {
        selectedPostIds.value.push(id)
      } else {
        selectedPostIds.value.splice(index, 1)
      }
    }

    // ======================= Helper Methods =======================

    const truncateText = (text, maxLength) => {
      if (!text) return ''
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
    }

    const stripHtml = (html) => {
      if (!html) return ''
      // Remove HTML tags and decode entities
      const doc = new DOMParser().parseFromString(html, 'text/html')
      return doc.body.textContent || ''
    }

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

    const getStatusLabel = (status) => {
      const labels = {
        'DRAFT': '草稿',
        'PENDING_REVIEW': '待审核',
        'PUBLISHED': '已发布',
        'REJECTED': '已拒绝',
        'PENDING_REVISION': '修改待审核'
      }
      return labels[status] || status
    }

    const getStatusBadgeClass = (status) => {
      const classes = {
        'DRAFT': 'badge-default',
        'PENDING_REVIEW': 'badge-warning',
        'PUBLISHED': 'badge-success',
        'REJECTED': 'badge-danger',
        'PENDING_REVISION': 'badge-info'
      }
      return classes[status] || 'badge-default'
    }

    const canApprove = (post) => {
      return post.status === 'PENDING_REVIEW' || post.status === 'PENDING_REVISION'
    }

    const canReject = (post) => {
      return post.status === 'PENDING_REVIEW' || post.status === 'PENDING_REVISION' || post.status === 'PUBLISHED'
    }

    // ======================= Post Management Methods =======================

    const loadPosts = async () => {
      postsLoading.value = true
      try {
        const params = {
          page: postPagination.value.page,
          size: postPagination.value.size,
          title: postSearchForm.value.title || undefined,
          author: postSearchForm.value.author || undefined,
          status: postSearchForm.value.status || undefined,
          tag: postSearchForm.value.tag || undefined,
          startDate: postSearchForm.value.startDate || undefined,
          endDate: postSearchForm.value.endDate || undefined
        }
        const response = await getPosts(params)
        postList.value = response.content || []
        postPagination.value.total = response.totalElements || 0
        postPagination.value.totalPages = response.totalPages || 0
        selectedPostIds.value = []
      } catch (error) {
        console.error('Failed to load posts:', error)
        alert('加载文章列表失败: ' + (error.message || '未知错误'))
      } finally {
        postsLoading.value = false
      }
    }

    const searchPosts = () => {
      postPagination.value.page = 0
      loadPosts()
    }

    const resetPostSearch = () => {
      postSearchForm.value = {
        title: '',
        author: '',
        status: '',
        tag: '',
        startDate: '',
        endDate: ''
      }
      postPagination.value.page = 0
      loadPosts()
    }

    const changePostPage = (newPage) => {
      postPagination.value.page = newPage
      loadPosts()
    }

    const viewPostDetail = async (post) => {
      try {
        const detail = await getPostDetail(post.id)
        selectedPost.value = detail
        showPostModal.value = true
      } catch (error) {
        console.error('Failed to load post detail:', error)
        alert('加载文章详情失败')
      }
    }

    const closePostModal = () => {
      showPostModal.value = false
      selectedPost.value = null
    }

    // ======================= Action Methods =======================

    const approvePost = async (post) => {
      if (!confirm(`确定要通过文章「${post.title}」吗？`)) return
      try {
        await executePostAction({
          action: 'APPROVE',
          postIds: [post.id]
        })
        await loadPosts()
        alert('文章已通过审核')
      } catch (error) {
        console.error('Failed to approve post:', error)
        alert('操作失败: ' + (error.response?.data || error.message))
      }
    }

    const batchApprove = async () => {
      const validPosts = postList.value.filter(p => selectedPostIds.value.includes(p.id) && canApprove(p))
      if (validPosts.length === 0) {
        alert('没有可以审核通过的文章')
        return
      }
      if (!confirm(`确定要批量通过 ${validPosts.length} 篇文章吗？`)) return
      try {
        const result = await executePostAction({
          action: 'APPROVE',
          postIds: validPosts.map(p => p.id)
        })
        await loadPosts()
        alert(`成功通过 ${result.successCount} 篇文章` + 
              (result.failureCount > 0 ? `，${result.failureCount} 篇失败` : ''))
      } catch (error) {
        console.error('Failed to batch approve:', error)
        alert('批量操作失败: ' + (error.response?.data || error.message))
      }
    }

    const openRejectModal = (post) => {
      actionType.value = 'REJECT'
      actionTargetPosts.value = [post]
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showActionModal.value = true
    }

    const openBatchRejectModal = () => {
      const validPosts = postList.value.filter(p => selectedPostIds.value.includes(p.id) && canReject(p))
      if (validPosts.length === 0) {
        alert('没有可以拒绝的文章')
        return
      }
      actionType.value = 'REJECT'
      actionTargetPosts.value = validPosts
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showActionModal.value = true
    }

    const openDeleteModal = (post) => {
      actionType.value = 'DELETE'
      actionTargetPosts.value = [post]
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showActionModal.value = true
    }

    const openBatchDeleteModal = () => {
      const validPosts = postList.value.filter(p => selectedPostIds.value.includes(p.id))
      if (validPosts.length === 0) {
        alert('请选择要删除的文章')
        return
      }
      actionType.value = 'DELETE'
      actionTargetPosts.value = validPosts
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showActionModal.value = true
    }

    const closeActionModal = () => {
      showActionModal.value = false
      actionType.value = ''
      actionTargetPosts.value = []
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
    }

    const addExtraField = () => {
      actionForm.value.extraFieldsList.push({ fieldName: '', fieldValue: '' })
    }

    const removeExtraField = (index) => {
      actionForm.value.extraFieldsList.splice(index, 1)
    }

    const confirmAction = async () => {
      if (!actionForm.value.reason) {
        alert('请填写理由')
        return
      }
      
      const extraFields = actionForm.value.extraFieldsList.filter(f => f.fieldName && f.fieldValue)
      
      try {
        // 保存操作类型，因为closeActionModal会清空它
        const currentActionType = actionType.value
        
        const result = await executePostAction({
          action: currentActionType,
          postIds: actionTargetPosts.value.map(p => p.id),
          formTitle: actionForm.value.formTitle || undefined,
          reason: actionForm.value.reason,
          extraFields: extraFields.length > 0 ? JSON.stringify(extraFields) : undefined
        })
        
        closeActionModal()
        await loadPosts()
        
        const actionLabel = currentActionType === 'DELETE' ? '删除' : '拒绝'
        alert(`成功${actionLabel} ${result.successCount} 篇文章` + 
              (result.failureCount > 0 ? `，${result.failureCount} 篇失败` : ''))
      } catch (error) {
        console.error('Action failed:', error)
        alert('操作失败: ' + (error.response?.data || error.message))
      }
    }

    onMounted(() => {
      loadPosts()
    })

    return {
      // State
      postList,
      postsLoading,
      postPagination,
      postSearchForm,
      showPostModal,
      selectedPost,
      selectedPostIds,
      showActionModal,
      actionType,
      actionTargetPosts,
      actionForm,
      
      // Computed
      menuItems,
      isAllSelected,
      isPartialSelected,
      actionModalTitle,
      
      // Methods
      isActiveRoute,
      handleLogout,
      isPostSelected,
      toggleSelectAll,
      togglePostSelection,
      truncateText,
      stripHtml,
      formatDate,
      formatDateTime,
      getStatusLabel,
      getStatusBadgeClass,
      canApprove,
      canReject,
      loadPosts,
      searchPosts,
      resetPostSearch,
      changePostPage,
      viewPostDetail,
      closePostModal,
      approvePost,
      batchApprove,
      openRejectModal,
      openBatchRejectModal,
      openDeleteModal,
      openBatchDeleteModal,
      closeActionModal,
      addExtraField,
      removeExtraField,
      confirmAction
    }
  }
}
</script>

<style scoped>

/* Card Styles */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.selected-count {
  font-size: 14px;
  color: #6b7280;
  margin-right: 8px;
}

/* Search Form Styles */
.search-form {
  padding: 16px 20px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.form-input {
  height: 38px;
  padding: 0 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

textarea.form-input {
  height: auto;
  padding: 12px;
  resize: vertical;
}

.search-btn-group {
  display: flex;
  align-items: flex-end;
}

/* Button Styles */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn-warning:hover {
  background: #d97706;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-info {
  background: #06b6d4;
  color: white;
}

.btn-info:hover {
  background: #0891b2;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Data Table Styles */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.data-table th {
  background: #f9fafb;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  white-space: nowrap;
}

.data-table td {
  font-size: 14px;
  color: #374151;
}

.data-table tbody tr:hover {
  background: #f9fafb;
}

.checkbox-col {
  width: 40px;
  text-align: center;
}

.title-col {
  max-width: 200px;
}

.post-title {
  color: #3b82f6;
  cursor: pointer;
}

.post-title:hover {
  text-decoration: underline;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-row {
  text-align: center;
  color: #9ca3af;
  padding: 40px !important;
}

.selected-row {
  background: rgba(59, 130, 246, 0.05);
}

.actions {
  white-space: nowrap;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  margin-right: 4px;
  background: #e5e7eb;
  color: #374151;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #d1d5db;
}

.action-btn.success {
  background: #d1fae5;
  color: #059669;
}

.action-btn.success:hover {
  background: #a7f3d0;
}

.action-btn.warning {
  background: #fef3c7;
  color: #d97706;
}

.action-btn.warning:hover {
  background: #fde68a;
}

.action-btn.danger {
  background: #fee2e2;
  color: #dc2626;
}

.action-btn.danger:hover {
  background: #fecaca;
}

/* Badge Styles */
.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge-default {
  background: #e5e7eb;
  color: #6b7280;
}

.badge-success {
  background: #d1fae5;
  color: #059669;
}

.badge-warning {
  background: #fef3c7;
  color: #d97706;
}

.badge-danger {
  background: #fee2e2;
  color: #dc2626;
}

.badge-info {
  background: #cffafe;
  color: #0891b2;
}

.ml-1 {
  margin-left: 4px;
}

/* Tag Badge */
.tag-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #e5e7eb;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 4px;
  margin-bottom: 4px;
}

/* Pagination Styles */
.pagination {
  padding: 16px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  border-top: 1px solid #e5e7eb;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
}

/* Modal Styles */
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
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-content.modal-lg {
  max-width: 700px;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  border-radius: 6px;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.detail-row,
.post-detail-grid .detail-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  width: 100px;
  font-size: 14px;
  color: #6b7280;
  flex-shrink: 0;
}

.detail-row .value {
  font-size: 14px;
  color: #1f2937;
  flex: 1;
}

.previous-version,
.rejection-info {
  margin-top: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.previous-version h4,
.rejection-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px 0;
}

.content-preview {
  margin-top: 16px;
}

.content-preview h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px 0;
}

.content-box {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}

/* Extra Field Row */
.extra-field-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.extra-field-row .form-input {
  flex: 1;
}

.affected-posts {
  margin-top: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.affected-posts label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.affected-posts ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
  font-size: 13px;
  color: #6b7280;
  max-height: 150px;
  overflow-y: auto;
}

.required {
  color: #ef4444;
}

/* Loading Container */
.loading-container {
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  color: #6b7280;
  font-size: 14px;
}

/* Quick Links */
.quick-links {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.quick-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  cursor: pointer;
}

.quick-link:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.quick-link svg {
  width: 18px;
  height: 18px;
}

/* Responsive */
@media (max-width: 1024px) {
  .form-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
