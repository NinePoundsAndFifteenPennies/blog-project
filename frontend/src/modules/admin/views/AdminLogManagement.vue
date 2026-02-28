<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="操作日志"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetLogSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>操作类型</label>
            <select v-model="logSearchForm.operationType" class="form-input">
              <option value="">全部</option>
              <option value="POST_APPROVE">文章审核通过</option>
              <option value="POST_REJECT">文章审核拒绝</option>
              <option value="POST_DELETE">文章删除</option>
              <option value="COMMENT_APPROVE">评论审核通过</option>
              <option value="COMMENT_DELETE">评论删除</option>
              <option value="TAG_CREATE">标签创建</option>
              <option value="TAG_UPDATE">标签更新</option>
              <option value="TAG_SOFT_DELETE">标签软删除</option>
              <option value="TAG_HARD_DELETE">标签硬删除</option>
              <option value="CATEGORY_CREATE">分类创建</option>
              <option value="CATEGORY_UPDATE">分类更新</option>
              <option value="CATEGORY_DELETE">分类删除</option>
              <option value="CATEGORY_ASSIGN_POST">分类添加文章</option>
              <option value="CATEGORY_REMOVE_POST">分类移除文章</option>
              <option value="USER_STATUS_CHANGE">用户状态变更</option>
            </select>
          </div>
          <div class="form-group">
            <label>管理员</label>
            <input type="text" v-model="logSearchForm.adminUsername" class="form-input" placeholder="搜索管理员用户名">
          </div>
          <div class="form-group">
            <label>操作标题</label>
            <input type="text" v-model="logSearchForm.title" class="form-input" placeholder="搜索操作标题">
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>开始日期</label>
            <input type="date" v-model="logSearchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>结束日期</label>
            <input type="date" v-model="logSearchForm.endDate" class="form-input">
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchLogs">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Log List -->
    <div class="card">
      <div class="card-header">
        <h3>日志列表 <span v-if="logPagination.total > 0">({{ logPagination.total }})</span></h3>
      </div>
      <div v-if="logsLoading" class="loading-container">
        <div class="loading-spinner">加载中...</div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>操作类型</th>
            <th>操作标题</th>
            <th>管理员</th>
            <th>关联信息</th>
            <th>操作时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="logList.length === 0">
            <td colspan="7" class="empty-row">暂无日志数据</td>
          </tr>
          <tr v-for="log in logList" :key="log.id">
            <td>{{ log.id }}</td>
            <td>
              <span :class="getOperationTypeBadgeClass(log.operationType)">
                {{ getOperationTypeLabel(log.operationType) }}
              </span>
            </td>
            <td>{{ truncateText(log.title, 30) }}</td>
            <td>{{ log.adminUsername || '-' }}</td>
            <td>{{ getRelatedInfo(log) }}</td>
            <td>{{ formatDate(log.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewLogDetail(log)">查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div v-if="logPagination.totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="logPagination.page === 0"
          @click="changeLogPage(logPagination.page - 1)"
        >上一页</button>
        <span class="page-info">
          第 {{ logPagination.page + 1 }} / {{ logPagination.totalPages }} 页
        </span>
        <button 
          class="page-btn" 
          :disabled="logPagination.page >= logPagination.totalPages - 1"
          @click="changeLogPage(logPagination.page + 1)"
        >下一页</button>
      </div>
    </div>

    <!-- Log Detail Modal -->
    <div v-if="showLogModal" class="modal-overlay" @click.self="closeLogModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>日志详情</h3>
          <button class="close-btn" @click="closeLogModal">&times;</button>
        </div>
        <div class="modal-body" v-if="selectedLog">
          <div class="detail-row">
            <span class="label">ID:</span>
            <span class="value">{{ selectedLog.id }}</span>
          </div>
          <div class="detail-row">
            <span class="label">操作类型:</span>
            <span class="value">
              <span :class="getOperationTypeBadgeClass(selectedLog.operationType)">
                {{ getOperationTypeLabel(selectedLog.operationType) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">操作标题:</span>
            <span class="value">{{ selectedLog.title || '-' }}</span>
          </div>
          <div v-if="selectedLog.description" class="detail-row">
            <span class="label">描述:</span>
            <span class="value content-value">{{ selectedLog.description }}</span>
          </div>
          <div class="detail-row">
            <span class="label">管理员:</span>
            <span class="value">{{ selectedLog.adminUsername || '-' }}</span>
          </div>
          <div v-if="selectedLog.postId" class="detail-row">
            <span class="label">关联文章:</span>
            <span class="value">
              <router-link :to="`/post/${selectedLog.postId}`" target="_blank" class="link">
                {{ selectedLog.postTitle || ('文章 #' + selectedLog.postId) }}
              </router-link>
            </span>
          </div>
          <div v-if="selectedLog.commentId" class="detail-row">
            <span class="label">关联评论:</span>
            <span class="value">评论 #{{ selectedLog.commentId }}</span>
          </div>
          <div v-if="selectedLog.tagId" class="detail-row">
            <span class="label">关联标签:</span>
            <span class="value">{{ selectedLog.tagName || ('标签 #' + selectedLog.tagId) }}</span>
          </div>
          <div v-if="selectedLog.categoryId" class="detail-row">
            <span class="label">关联分类:</span>
            <span class="value">{{ selectedLog.categoryName || ('分类 #' + selectedLog.categoryId) }}</span>
          </div>
          <div v-if="selectedLog.targetUserId" class="detail-row">
            <span class="label">关联用户:</span>
            <span class="value">{{ selectedLog.targetUsername || ('用户 #' + selectedLog.targetUserId) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDateTime(selectedLog.createdAt) }}</span>
          </div>
          <div v-if="selectedLog.formId" class="detail-row">
            <span class="label">关联表单:</span>
            <span class="value">表单 #{{ selectedLog.formId }}</span>
          </div>

          <!-- 表单详情区域 -->
          <div v-if="selectedLog.reason || selectedLog.extraFields" class="form-detail-section">
            <h4 class="section-title">表单详情</h4>
            <div v-if="selectedLog.reason" class="detail-row">
              <span class="label">操作理由:</span>
              <span class="value content-value">{{ selectedLog.reason }}</span>
            </div>
            <div v-if="parsedExtraFields.length > 0" class="detail-row">
              <span class="label">扩展信息:</span>
              <span class="value">
                <div v-for="(field, index) in parsedExtraFields" :key="index" class="extra-field-item">
                  <span class="extra-field-name">{{ field.fieldName }}:</span>
                  <span class="extra-field-value">{{ field.fieldValue }}</span>
                </div>
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeLogModal">关闭</button>
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
import { getAdminLogs, getAdminLogDetail } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminLogManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= Log Management State =======================
    const logList = ref([])
    const logsLoading = ref(false)
    const logPagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const logSearchForm = ref({
      operationType: '',
      adminUsername: '',
      title: '',
      startDate: '',
      endDate: ''
    })
    const showLogModal = ref(false)
    const selectedLog = ref(null)

    const menuItems = [
      { id: 'dashboard', path: '/admin', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin/categories', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin/tags', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin/comments', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'logs', path: '/admin/logs', label: '操作日志', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/></svg>' },
      { id: 'media', path: '/admin', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'logs'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    // ======================= Log Management Methods =======================

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

    const getOperationTypeLabel = (type) => {
      const labels = {
        'POST_APPROVE': '文章审核通过',
        'POST_REJECT': '文章审核拒绝',
        'POST_DELETE': '文章删除',
        'COMMENT_APPROVE': '评论审核通过',
        'COMMENT_DELETE': '评论删除',
        'TAG_CREATE': '标签创建',
        'TAG_UPDATE': '标签更新',
        'TAG_SOFT_DELETE': '标签软删除',
        'TAG_HARD_DELETE': '标签硬删除',
        'CATEGORY_CREATE': '分类创建',
        'CATEGORY_UPDATE': '分类更新',
        'CATEGORY_DELETE': '分类删除',
        'CATEGORY_ASSIGN_POST': '分类添加文章',
        'CATEGORY_REMOVE_POST': '分类移除文章',
        'USER_STATUS_CHANGE': '用户状态变更'
      }
      return labels[type] || type
    }

    const getOperationTypeBadgeClass = (type) => {
      if (type && (type.includes('DELETE') || type.includes('REJECT'))) return 'badge badge-danger'
      if (type && (type.includes('APPROVE') || type.includes('CREATE'))) return 'badge badge-success'
      if (type && type.includes('UPDATE')) return 'badge badge-info'
      if (type && type.includes('ASSIGN')) return 'badge badge-info'
      if (type && type.includes('REMOVE')) return 'badge badge-warning'
      if (type === 'USER_STATUS_CHANGE') return 'badge badge-warning'
      return 'badge badge-default'
    }

    const getRelatedInfo = (log) => {
      const parts = []
      if (log.postId) parts.push('文章 #' + log.postId)
      if (log.commentId) parts.push('评论 #' + log.commentId)
      if (log.tagName) parts.push('标签: ' + log.tagName)
      else if (log.tagId) parts.push('标签 #' + log.tagId)
      if (log.categoryName) parts.push('分类: ' + log.categoryName)
      else if (log.categoryId) parts.push('分类 #' + log.categoryId)
      if (log.targetUsername) parts.push('用户: ' + log.targetUsername)
      else if (log.targetUserId) parts.push('用户 #' + log.targetUserId)
      return parts.length > 0 ? parts.join(', ') : '-'
    }

    const loadLogs = async () => {
      logsLoading.value = true
      try {
        const params = {
          page: logPagination.value.page,
          size: logPagination.value.size,
          operationType: logSearchForm.value.operationType || undefined,
          adminUsername: logSearchForm.value.adminUsername || undefined,
          title: logSearchForm.value.title || undefined,
          startDate: logSearchForm.value.startDate || undefined,
          endDate: logSearchForm.value.endDate || undefined
        }
        const response = await getAdminLogs(params)
        logList.value = response.content || []
        logPagination.value.total = response.totalElements || 0
        logPagination.value.totalPages = response.totalPages || 0
      } catch (error) {
        console.error('Failed to load logs:', error)
        alert('加载日志列表失败: ' + (error.message || '未知错误'))
      } finally {
        logsLoading.value = false
      }
    }

    const searchLogs = () => {
      logPagination.value.page = 0
      loadLogs()
    }

    const resetLogSearch = () => {
      logSearchForm.value = {
        operationType: '',
        adminUsername: '',
        title: '',
        startDate: '',
        endDate: ''
      }
      logPagination.value.page = 0
      loadLogs()
    }

    const changeLogPage = (newPage) => {
      logPagination.value.page = newPage
      loadLogs()
    }

    const viewLogDetail = async (log) => {
      try {
        const detail = await getAdminLogDetail(log.id)
        selectedLog.value = detail
        showLogModal.value = true
      } catch (error) {
        console.error('Failed to load log detail:', error)
        alert('加载日志详情失败')
      }
    }

    const closeLogModal = () => {
      showLogModal.value = false
      selectedLog.value = null
    }

    const parsedExtraFields = computed(() => {
      if (!selectedLog.value || !selectedLog.value.extraFields) return []
      try {
        const parsed = JSON.parse(selectedLog.value.extraFields)
        if (Array.isArray(parsed)) {
          return parsed.filter(f => f.fieldName && f.fieldName.trim() !== '' && f.fieldValue != null)
        }
        return []
      } catch (e) {
        return []
      }
    })

    onMounted(() => {
      loadLogs()
    })

    return {
      menuItems,
      isActiveRoute,
      handleLogout,
      // Log management
      logList,
      logsLoading,
      logPagination,
      logSearchForm,
      showLogModal,
      selectedLog,
      formatDate,
      formatDateTime,
      truncateText,
      getOperationTypeLabel,
      getOperationTypeBadgeClass,
      getRelatedInfo,
      searchLogs,
      resetLogSearch,
      changeLogPage,
      viewLogDetail,
      closeLogModal,
      parsedExtraFields
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

/* Form Detail Section */
.form-detail-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid #e8e8e8;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1d2e;
  margin: 0 0 12px 0;
}

.extra-field-item {
  display: flex;
  gap: 8px;
  padding: 6px 0;
}

.extra-field-name {
  font-weight: 500;
  color: #666;
  flex-shrink: 0;
}

.extra-field-value {
  color: #333;
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
