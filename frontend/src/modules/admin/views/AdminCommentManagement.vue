<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="评论管理"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetCommentSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>评论内容</label>
            <input type="text" v-model="commentSearchForm.content" class="form-input" placeholder="搜索评论内容">
          </div>
          <div class="form-group">
            <label>作者</label>
            <input type="text" v-model="commentSearchForm.author" class="form-input" placeholder="用户名或昵称">
          </div>
          <div class="form-group">
            <label>文章标题</label>
            <input type="text" v-model="commentSearchForm.postTitle" class="form-input" placeholder="搜索文章标题">
          </div>
          <div class="form-group">
            <label>审核状态</label>
            <select v-model="commentSearchForm.status" class="form-input">
              <option value="">全部</option>
              <option value="PENDING">待审核</option>
              <option value="APPROVED">已通过</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>创建开始日期</label>
            <input type="date" v-model="commentSearchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>创建结束日期</label>
            <input type="date" v-model="commentSearchForm.endDate" class="form-input">
          </div>
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="commentSearchForm.includeReplies">
              <span>包含子评论</span>
            </label>
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchComments">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Comment List -->
    <div class="card">
      <div class="card-header">
        <h3>评论列表 <span v-if="commentPagination.total > 0">({{ commentPagination.total }})</span></h3>
        <div class="header-actions" v-if="selectedCommentIds.length > 0">
          <span class="selected-count">已选择 {{ selectedCommentIds.length }} 项</span>
          <button class="btn btn-success btn-sm" @click="batchApprove">批量通过</button>
          <button class="btn btn-danger btn-sm" @click="openBatchDeleteModal">批量删除</button>
        </div>
      </div>
      <div v-if="commentsLoading" class="loading-container">
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
            <th>评论内容</th>
            <th>作者</th>
            <th>所属文章</th>
            <th>类型</th>
            <th>审核状态</th>
            <th>点赞数</th>
            <th>回复数</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="commentList.length === 0">
            <td colspan="11" class="empty-row">暂无评论数据</td>
          </tr>
          <tr v-for="comment in commentList" :key="comment.id" :class="{ 'selected-row': isCommentSelected(comment.id) }">
            <td class="checkbox-col">
              <input
                type="checkbox"
                :checked="isCommentSelected(comment.id)"
                @change="toggleCommentSelection(comment.id)"
              >
            </td>
            <td>{{ comment.id }}</td>
            <td class="content-col">
              <span class="comment-content" @click="viewCommentDetail(comment)">{{ truncateText(comment.contentPreview || comment.content, 40) }}</span>
            </td>
            <td>
              <span class="author-info">
                <span class="author-name">{{ comment.authorNickname || comment.authorUsername }}</span>
                <span v-if="!comment.authorEnabled" class="badge badge-danger ml-1">已禁用</span>
              </span>
            </td>
            <td class="post-col">
              <span class="post-title" :title="comment.postTitle">{{ truncateText(comment.postTitle, 20) }}</span>
            </td>
            <td>
              <span :class="['badge', comment.level > 0 ? 'badge-info' : 'badge-default']">
                {{ comment.level > 0 ? '回复' : '评论' }}
              </span>
            </td>
            <td>
              <span :class="['badge', getStatusBadgeClass(comment.status)]">
                {{ getStatusLabel(comment.status) }}
              </span>
            </td>
            <td>{{ comment.likeCount }}</td>
            <td>{{ comment.replyCount }}</td>
            <td>{{ formatDate(comment.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewCommentDetail(comment)">详情</button>
              <button
                v-if="comment.status === 'PENDING'"
                class="action-btn success"
                @click="approveComment(comment)"
              >通过</button>
              <button
                class="action-btn danger"
                @click="openDeleteModal(comment)"
              >删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div v-if="commentPagination.totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="commentPagination.page === 0"
          @click="changeCommentPage(commentPagination.page - 1)"
        >上一页</button>
        <span class="page-info">
          第 {{ commentPagination.page + 1 }} / {{ commentPagination.totalPages }} 页
        </span>
        <button 
          class="page-btn" 
          :disabled="commentPagination.page >= commentPagination.totalPages - 1"
          @click="changeCommentPage(commentPagination.page + 1)"
        >下一页</button>
      </div>
    </div>

    <!-- Comment Detail Modal -->
    <div v-if="showCommentModal" class="modal-overlay" @click.self="closeCommentModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>评论详情</h3>
          <button class="close-btn" @click="closeCommentModal">&times;</button>
        </div>
        <div class="modal-body" v-if="selectedComment">
          <div class="detail-row">
            <span class="label">ID:</span>
            <span class="value">{{ selectedComment.id }}</span>
          </div>
          <div class="detail-row">
            <span class="label">评论内容:</span>
            <span class="value content-value">{{ selectedComment.content }}</span>
          </div>
          <div class="detail-row">
            <span class="label">作者:</span>
            <span class="value">{{ selectedComment.authorNickname }} (@{{ selectedComment.authorUsername }})</span>
          </div>
          <div class="detail-row">
            <span class="label">所属文章:</span>
            <span class="value">
              <router-link 
                v-if="selectedComment.postId" 
                :to="`/post/${selectedComment.postId}`" 
                target="_blank"
                class="link"
              >{{ selectedComment.postTitle }}</router-link>
              <span v-else>{{ selectedComment.postTitle || '-' }}</span>
            </span>
          </div>
          <div v-if="selectedComment.parentId" class="detail-row">
            <span class="label">父评论:</span>
            <span class="value">ID: {{ selectedComment.parentId }} - {{ selectedComment.parentContentPreview }}</span>
          </div>
          <div v-if="selectedComment.replyToUsername" class="detail-row">
            <span class="label">回复给:</span>
            <span class="value">@{{ selectedComment.replyToUsername }}</span>
          </div>
          <div class="detail-row">
            <span class="label">类型:</span>
            <span class="value">
              <span :class="['badge', selectedComment.level > 0 ? 'badge-info' : 'badge-default']">
                {{ selectedComment.level > 0 ? '回复（层级' + selectedComment.level + '）' : '顶层评论' }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">审核状态:</span>
            <span class="value">
              <span :class="['badge', getStatusBadgeClass(selectedComment.status)]">
                {{ getStatusLabel(selectedComment.status) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">点赞数:</span>
            <span class="value">{{ selectedComment.likeCount }}</span>
          </div>
          <div class="detail-row">
            <span class="label">回复数:</span>
            <span class="value">{{ selectedComment.replyCount }}</span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDateTime(selectedComment.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">更新时间:</span>
            <span class="value">{{ formatDateTime(selectedComment.updatedAt) }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button
            v-if="selectedComment && selectedComment.status === 'PENDING'"
            class="btn btn-success"
            @click="approveCommentFromModal"
          >通过审核</button>
          <button
            v-if="selectedComment"
            class="btn btn-danger"
            @click="openDeleteModalFromDetail"
          >删除评论</button>
          <button class="btn btn-secondary" @click="closeCommentModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ isBatchDelete ? '批量删除评论' : '删除评论' }}</h3>
          <button class="close-btn" @click="closeDeleteModal">&times;</button>
        </div>
        <div class="modal-body">
          <p class="warning-text">
            {{ isBatchDelete 
              ? `确定要删除选中的 ${selectedCommentIds.length} 条评论吗？` 
              : `确定要删除此评论吗？` 
            }}
          </p>
          <p class="sub-text">删除后将级联删除所有子评论，此操作不可恢复。</p>
          
          <div class="form-group">
            <label>通知标题 <span class="optional">(可选)</span></label>
            <input type="text" v-model="deleteForm.formTitle" class="form-input" placeholder="评论删除通知">
          </div>
          <div class="form-group">
            <label>删除理由 <span class="required">*</span></label>
            <textarea v-model="deleteForm.reason" class="form-input form-textarea" placeholder="请填写删除理由" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>扩展信息 <span class="optional">(可选)</span></label>
            <div v-for="(field, index) in deleteForm.extraFieldsList" :key="index" class="extra-field-row">
              <input type="text" v-model="field.fieldName" class="form-input extra-field-input" placeholder="字段名">
              <input type="text" v-model="field.fieldValue" class="form-input extra-field-input" placeholder="字段值">
              <button class="btn btn-sm btn-danger" @click="removeExtraField(index)">删除</button>
            </div>
            <button class="btn btn-sm btn-secondary" @click="addExtraField">+ 添加扩展字段</button>
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
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getComments, getCommentDetail, executeCommentAction } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminCommentManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= Comment Management State =======================
    const commentList = ref([])
    const commentsLoading = ref(false)
    const commentPagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const commentSearchForm = ref({
      content: '',
      author: '',
      postTitle: '',
      status: '',
      startDate: '',
      endDate: '',
      includeReplies: true
    })
    const showCommentModal = ref(false)
    const selectedComment = ref(null)
    const selectedCommentIds = ref([])

    // Delete modal state
    const showDeleteModal = ref(false)
    const isBatchDelete = ref(false)
    const deleteTargetComment = ref(null)
    const deleteForm = ref({
      formTitle: '',
      reason: '',
      extraFieldsList: []
    })

    // Extra field helper functions
    const addExtraField = () => {
      deleteForm.value.extraFieldsList.push({ fieldName: '', fieldValue: '' })
    }

    const removeExtraField = (index) => {
      deleteForm.value.extraFieldsList.splice(index, 1)
    }

    const getExtraFieldsJson = () => {
      const validFields = deleteForm.value.extraFieldsList.filter(
        f => f.fieldName && f.fieldName.trim() !== ''
      )
      return validFields.length > 0 ? JSON.stringify(validFields) : null
    }

    const menuItems = [
      { id: 'dashboard', path: '/admin', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin/tags', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin/comments', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'media', path: '/admin', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'comments'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    // ======================= Comment Management Methods =======================

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

    const getStatusLabel = (status) => {
      switch (status) {
        case 'PENDING': return '待审核'
        case 'APPROVED': return '已通过'
        default: return status || '-'
      }
    }

    const getStatusBadgeClass = (status) => {
      switch (status) {
        case 'PENDING': return 'badge-warning'
        case 'APPROVED': return 'badge-success'
        default: return 'badge-default'
      }
    }

    const loadComments = async () => {
      commentsLoading.value = true
      try {
        const params = {
          page: commentPagination.value.page,
          size: commentPagination.value.size,
          content: commentSearchForm.value.content || undefined,
          author: commentSearchForm.value.author || undefined,
          postTitle: commentSearchForm.value.postTitle || undefined,
          status: commentSearchForm.value.status || undefined,
          startDate: commentSearchForm.value.startDate || undefined,
          endDate: commentSearchForm.value.endDate || undefined,
          includeReplies: commentSearchForm.value.includeReplies
        }
        const response = await getComments(params)
        commentList.value = response.content || []
        commentPagination.value.total = response.totalElements || 0
        commentPagination.value.totalPages = response.totalPages || 0
        // Clear selections when page changes
        selectedCommentIds.value = []
      } catch (error) {
        console.error('Failed to load comments:', error)
        alert('加载评论列表失败: ' + (error.message || '未知错误'))
      } finally {
        commentsLoading.value = false
      }
    }

    const searchComments = () => {
      commentPagination.value.page = 0
      loadComments()
    }

    const resetCommentSearch = () => {
      commentSearchForm.value = {
        content: '',
        author: '',
        postTitle: '',
        status: '',
        startDate: '',
        endDate: '',
        includeReplies: true
      }
      commentPagination.value.page = 0
      loadComments()
    }

    const changeCommentPage = (newPage) => {
      commentPagination.value.page = newPage
      loadComments()
    }

    const viewCommentDetail = async (comment) => {
      try {
        const detail = await getCommentDetail(comment.id)
        selectedComment.value = detail
        showCommentModal.value = true
      } catch (error) {
        console.error('Failed to load comment detail:', error)
        alert('加载评论详情失败')
      }
    }

    const closeCommentModal = () => {
      showCommentModal.value = false
      selectedComment.value = null
    }

    const approveComment = async (comment) => {
      if (!confirm(`确定要通过评论 ID ${comment.id} 的审核吗？`)) return
      try {
        await executeCommentAction({
          action: 'APPROVE',
          commentIds: [comment.id]
        })
        await loadComments()
        alert('评论已通过审核')
      } catch (error) {
        console.error('Failed to approve comment:', error)
        alert('审核通过失败: ' + (error.response?.data || error.message))
      }
    }

    const approveCommentFromModal = async () => {
      if (!selectedComment.value) return
      await approveComment(selectedComment.value)
      closeCommentModal()
    }

    const openDeleteModal = (comment) => {
      deleteTargetComment.value = comment
      isBatchDelete.value = false
      deleteForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showDeleteModal.value = true
    }

    const openDeleteModalFromDetail = () => {
      if (!selectedComment.value) return
      closeCommentModal()
      openDeleteModal(selectedComment.value)
    }

    const openBatchDeleteModal = () => {
      if (selectedCommentIds.value.length === 0) return
      isBatchDelete.value = true
      deleteForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showDeleteModal.value = true
    }

    const closeDeleteModal = () => {
      showDeleteModal.value = false
      deleteTargetComment.value = null
      isBatchDelete.value = false
    }

    const confirmDelete = async () => {
      if (!deleteForm.value.reason || deleteForm.value.reason.trim() === '') {
        alert('请填写删除理由')
        return
      }

      const ids = isBatchDelete.value ? selectedCommentIds.value : [deleteTargetComment.value.id]
      
      try {
        await executeCommentAction({
          action: 'DELETE',
          commentIds: ids,
          formTitle: deleteForm.value.formTitle || undefined,
          reason: deleteForm.value.reason,
          extraFields: getExtraFieldsJson()
        })
        closeDeleteModal()
        selectedCommentIds.value = []
        await loadComments()
        alert(`成功删除 ${ids.length} 条评论`)
      } catch (error) {
        console.error('Failed to delete comments:', error)
        alert('删除失败: ' + (error.response?.data || error.message))
      }
    }

    // ======================= Batch Selection Methods =======================

    const isCommentSelected = (commentId) => {
      return selectedCommentIds.value.includes(commentId)
    }

    const toggleCommentSelection = (commentId) => {
      const index = selectedCommentIds.value.indexOf(commentId)
      if (index === -1) {
        selectedCommentIds.value.push(commentId)
      } else {
        selectedCommentIds.value.splice(index, 1)
      }
    }

    const isAllSelected = computed(() => {
      return commentList.value.length > 0 && commentList.value.every(c => selectedCommentIds.value.includes(c.id))
    })

    const isPartialSelected = computed(() => {
      const selectedCount = commentList.value.filter(c => selectedCommentIds.value.includes(c.id)).length
      return selectedCount > 0 && selectedCount < commentList.value.length
    })

    const toggleSelectAll = () => {
      if (isAllSelected.value) {
        selectedCommentIds.value = []
      } else {
        selectedCommentIds.value = commentList.value.map(c => c.id)
      }
    }

    const batchApprove = async () => {
      if (selectedCommentIds.value.length === 0) return
      if (!confirm(`确定要批量通过 ${selectedCommentIds.value.length} 条评论的审核吗？`)) return
      
      const count = selectedCommentIds.value.length
      try {
        await executeCommentAction({
          action: 'APPROVE',
          commentIds: selectedCommentIds.value
        })
        selectedCommentIds.value = []
        await loadComments()
        alert(`已成功通过 ${count} 条评论的审核`)
      } catch (error) {
        console.error('Batch approve failed:', error)
        alert('批量审核通过失败: ' + (error.response?.data || error.message))
      }
    }

    onMounted(() => {
      loadComments()
    })

    return {
      menuItems,
      isActiveRoute,
      handleLogout,
      // Comment management
      commentList,
      commentsLoading,
      commentPagination,
      commentSearchForm,
      showCommentModal,
      selectedComment,
      selectedCommentIds,
      formatDate,
      formatDateTime,
      truncateText,
      getStatusLabel,
      getStatusBadgeClass,
      searchComments,
      resetCommentSearch,
      changeCommentPage,
      viewCommentDetail,
      closeCommentModal,
      approveComment,
      approveCommentFromModal,
      // Delete operations
      showDeleteModal,
      isBatchDelete,
      deleteForm,
      openDeleteModal,
      openDeleteModalFromDetail,
      openBatchDeleteModal,
      closeDeleteModal,
      confirmDelete,
      addExtraField,
      removeExtraField,
      // Batch operations
      isCommentSelected,
      toggleCommentSelection,
      isAllSelected,
      isPartialSelected,
      toggleSelectAll,
      batchApprove
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

.extra-field-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.extra-field-input {
  flex: 1;
}

.checkbox-group {
  display: flex;
  align-items: flex-end;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
}

.checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
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

.content-col {
  max-width: 250px;
}

.comment-content {
  cursor: pointer;
  color: #1890ff;
}

.comment-content:hover {
  text-decoration: underline;
}

.post-col {
  max-width: 150px;
}

.post-title {
  color: #666;
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

.ml-1 {
  margin-left: 4px;
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
}

.content-value {
  white-space: pre-wrap;
  word-break: break-word;
}

.link {
  color: #1890ff;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
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
</style>
