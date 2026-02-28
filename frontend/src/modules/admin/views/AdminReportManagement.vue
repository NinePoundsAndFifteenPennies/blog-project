<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="举报管理"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetReportSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>状态</label>
            <select v-model="reportSearchForm.status" class="form-input">
              <option value="">全部</option>
              <option value="PENDING">待处理</option>
              <option value="APPROVED">已通过</option>
              <option value="REJECTED">已驳回</option>
            </select>
          </div>
          <div class="form-group">
            <label>举报目标类型</label>
            <select v-model="reportSearchForm.targetType" class="form-input">
              <option value="">全部</option>
              <option value="POST">文章</option>
              <option value="COMMENT">评论</option>
            </select>
          </div>
          <div class="form-group">
            <label>举报人</label>
            <input type="text" v-model="reportSearchForm.reporterUsername" class="form-input" placeholder="举报人用户名">
          </div>
          <div class="form-group">
            <label>被举报人</label>
            <input type="text" v-model="reportSearchForm.reportedUsername" class="form-input" placeholder="被举报人用户名">
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>创建开始日期</label>
            <input type="date" v-model="reportSearchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>创建结束日期</label>
            <input type="date" v-model="reportSearchForm.endDate" class="form-input">
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchReports">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Report List -->
    <div class="card">
      <div class="card-header">
        <h3>举报列表 <span v-if="reportPagination.total > 0">({{ reportPagination.total }})</span></h3>
      </div>
      <div v-if="reportsLoading" class="loading-container">
        <div class="loading-spinner">加载中...</div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>目标类型</th>
            <th>内容预览</th>
            <th>举报人</th>
            <th>被举报人</th>
            <th>举报理由</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="reportList.length === 0">
            <td colspan="9" class="empty-row">暂无举报数据</td>
          </tr>
          <tr v-for="report in reportList" :key="report.id">
            <td>{{ report.id }}</td>
            <td>
              <span :class="['badge', report.targetType === 'POST' ? 'badge-info' : 'badge-default']">
                {{ getTargetTypeLabel(report.targetType) }}
              </span>
            </td>
            <td class="content-col">
              <span class="content-link" @click="viewReportDetail(report)">{{ truncateText(report.targetContentPreview || report.reason, 40) }}</span>
            </td>
            <td>{{ report.reporterNickname || report.reporterUsername }}</td>
            <td>{{ report.reportedNickname || report.reportedUsername }}</td>
            <td class="content-col">{{ truncateText(report.reason, 30) }}</td>
            <td>
              <span :class="['badge', getStatusBadgeClass(report.status)]">
                {{ getStatusLabel(report.status) }}
              </span>
            </td>
            <td>{{ formatDate(report.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewReportDetail(report)">详情</button>
              <button
                v-if="report.status === 'PENDING'"
                class="action-btn success"
                @click="openActionModal(report, 'APPROVE')"
              >通过</button>
              <button
                v-if="report.status === 'PENDING'"
                class="action-btn danger"
                @click="openActionModal(report, 'REJECT')"
              >驳回</button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div v-if="reportPagination.totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="reportPagination.page === 0"
          @click="changeReportPage(reportPagination.page - 1)"
        >上一页</button>
        <span class="page-info">
          第 {{ reportPagination.page + 1 }} / {{ reportPagination.totalPages }} 页
        </span>
        <button 
          class="page-btn" 
          :disabled="reportPagination.page >= reportPagination.totalPages - 1"
          @click="changeReportPage(reportPagination.page + 1)"
        >下一页</button>
      </div>
    </div>

    <!-- Report Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>举报详情</h3>
          <button class="close-btn" @click="closeDetailModal">&times;</button>
        </div>
        <div class="modal-body" v-if="selectedReport">
          <div class="detail-row">
            <span class="label">ID:</span>
            <span class="value">{{ selectedReport.id }}</span>
          </div>
          <div class="detail-row">
            <span class="label">目标类型:</span>
            <span class="value">
              <span :class="['badge', selectedReport.targetType === 'POST' ? 'badge-info' : 'badge-default']">
                {{ getTargetTypeLabel(selectedReport.targetType) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">目标ID:</span>
            <span class="value">{{ selectedReport.targetId }}</span>
          </div>
          <div class="detail-row">
            <span class="label">举报人:</span>
            <span class="value">{{ selectedReport.reporterNickname }} (@{{ selectedReport.reporterUsername }})</span>
          </div>
          <div class="detail-row">
            <span class="label">被举报人:</span>
            <span class="value">{{ selectedReport.reportedNickname }} (@{{ selectedReport.reportedUsername }})</span>
          </div>
          <div class="detail-row">
            <span class="label">举报理由:</span>
            <span class="value content-value">{{ selectedReport.reason }}</span>
          </div>
          <div class="detail-row">
            <span class="label">状态:</span>
            <span class="value">
              <span :class="['badge', getStatusBadgeClass(selectedReport.status)]">
                {{ getStatusLabel(selectedReport.status) }}
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDateTime(selectedReport.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">更新时间:</span>
            <span class="value">{{ formatDateTime(selectedReport.updatedAt) }}</span>
          </div>
          <div v-if="selectedReport.targetContentPreview" class="detail-row">
            <span class="label">内容预览:</span>
            <span class="value content-value">{{ selectedReport.targetContentPreview }}</span>
          </div>

          <!-- Action Form Info -->
          <div v-if="selectedReport.actionForm" class="action-info">
            <h4>处理记录</h4>
            <div class="detail-row">
              <span class="label">表单标题:</span>
              <span class="value">{{ selectedReport.actionForm.title }}</span>
            </div>
            <div class="detail-row">
              <span class="label">处理理由:</span>
              <span class="value">{{ selectedReport.actionForm.reason }}</span>
            </div>
            <div class="detail-row">
              <span class="label">操作人:</span>
              <span class="value">{{ selectedReport.actionForm.adminNickname }}</span>
            </div>
            <div class="detail-row">
              <span class="label">操作时间:</span>
              <span class="value">{{ formatDateTime(selectedReport.actionForm.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button
            v-if="selectedReport && selectedReport.status === 'PENDING'"
            class="btn btn-success"
            @click="openActionModalFromDetail('APPROVE')"
          >通过</button>
          <button
            v-if="selectedReport && selectedReport.status === 'PENDING'"
            class="btn btn-danger"
            @click="openActionModalFromDetail('REJECT')"
          >驳回</button>
          <button class="btn btn-secondary" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- Action Modal -->
    <div v-if="showActionModal" class="modal-overlay" @click.self="closeActionModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ actionModalTitle }}</h3>
          <button class="close-btn" @click="closeActionModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>表单标题 <span class="required">*</span></label>
            <input type="text" v-model="actionForm.formTitle" class="form-input" placeholder="例如：举报处理通知">
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
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeActionModal">取消</button>
          <button 
            :class="['btn', actionType === 'APPROVE' ? 'btn-success' : 'btn-danger']" 
            @click="confirmAction"
            :disabled="!actionForm.formTitle || !actionForm.reason"
          >确认{{ actionType === 'APPROVE' ? '通过' : '驳回' }}</button>
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
import { getAdminReports, getAdminReportDetail, executeReportAction } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminReportManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= Report Management State =======================
    const reportList = ref([])
    const reportsLoading = ref(false)
    const reportPagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const reportSearchForm = ref({
      status: '',
      targetType: '',
      reporterUsername: '',
      reportedUsername: '',
      startDate: '',
      endDate: ''
    })
    const showDetailModal = ref(false)
    const selectedReport = ref(null)

    // Action Modal State
    const showActionModal = ref(false)
    const actionType = ref('')
    const actionTargetReport = ref(null)
    const actionForm = ref({
      formTitle: '',
      reason: '',
      extraFieldsList: []
    })

    const menuItems = [
      { id: 'dashboard', path: '/admin', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin/categories', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin/tags', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin/comments', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'reports', path: '/admin/reports', label: '举报管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 9v2m0 4h.01M5.07 19H19a2 2 0 001.75-2.97L13.75 4a2 2 0 00-3.5 0L3.32 16.03A2 2 0 005.07 19z"/></svg>' },
      { id: 'logs', path: '/admin/logs', label: '操作日志', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'reports'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    const actionModalTitle = computed(() => {
      return actionType.value === 'APPROVE' ? '通过举报' : '驳回举报'
    })

    // ======================= Helper Methods =======================

    const truncateText = (text, maxLength) => {
      if (!text) return '-'
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
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
        'PENDING': '待处理',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return labels[status] || status || '-'
    }

    const getStatusBadgeClass = (status) => {
      const classes = {
        'PENDING': 'badge-warning',
        'APPROVED': 'badge-success',
        'REJECTED': 'badge-danger'
      }
      return classes[status] || 'badge-default'
    }

    const getTargetTypeLabel = (targetType) => {
      const labels = {
        'POST': '文章',
        'COMMENT': '评论'
      }
      return labels[targetType] || targetType || '-'
    }

    // ======================= Report Management Methods =======================

    const loadReports = async () => {
      reportsLoading.value = true
      try {
        const params = {
          page: reportPagination.value.page,
          size: reportPagination.value.size,
          status: reportSearchForm.value.status || undefined,
          targetType: reportSearchForm.value.targetType || undefined,
          reporterUsername: reportSearchForm.value.reporterUsername || undefined,
          reportedUsername: reportSearchForm.value.reportedUsername || undefined,
          startDate: reportSearchForm.value.startDate || undefined,
          endDate: reportSearchForm.value.endDate || undefined
        }
        const response = await getAdminReports(params)
        reportList.value = response.content || []
        reportPagination.value.total = response.totalElements || 0
        reportPagination.value.totalPages = response.totalPages || 0
      } catch (error) {
        console.error('Failed to load reports:', error)
        alert('加载举报列表失败: ' + (error.message || '未知错误'))
      } finally {
        reportsLoading.value = false
      }
    }

    const searchReports = () => {
      reportPagination.value.page = 0
      loadReports()
    }

    const resetReportSearch = () => {
      reportSearchForm.value = {
        status: '',
        targetType: '',
        reporterUsername: '',
        reportedUsername: '',
        startDate: '',
        endDate: ''
      }
      reportPagination.value.page = 0
      loadReports()
    }

    const changeReportPage = (newPage) => {
      reportPagination.value.page = newPage
      loadReports()
    }

    const viewReportDetail = async (report) => {
      try {
        const detail = await getAdminReportDetail(report.id)
        selectedReport.value = detail
        showDetailModal.value = true
      } catch (error) {
        console.error('Failed to load report detail:', error)
        alert('加载举报详情失败')
      }
    }

    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedReport.value = null
    }

    // ======================= Action Methods =======================

    const openActionModal = (report, type) => {
      actionType.value = type
      actionTargetReport.value = report
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      showActionModal.value = true
    }

    const openActionModalFromDetail = (type) => {
      if (!selectedReport.value) return
      const report = selectedReport.value
      closeDetailModal()
      openActionModal(report, type)
    }

    const closeActionModal = () => {
      showActionModal.value = false
      actionType.value = ''
      actionTargetReport.value = null
      actionForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
    }

    const addExtraField = () => {
      actionForm.value.extraFieldsList.push({ fieldName: '', fieldValue: '' })
    }

    const removeExtraField = (index) => {
      actionForm.value.extraFieldsList.splice(index, 1)
    }

    const confirmAction = async () => {
      if (!actionForm.value.formTitle || !actionForm.value.reason) {
        alert('请填写表单标题和理由')
        return
      }

      const extraFields = actionForm.value.extraFieldsList.filter(f => f.fieldName && f.fieldValue)

      try {
        const currentActionType = actionType.value

        await executeReportAction({
          action: currentActionType,
          reportId: actionTargetReport.value.id,
          formTitle: actionForm.value.formTitle,
          reason: actionForm.value.reason,
          extraFields: extraFields.length > 0 ? JSON.stringify(extraFields) : undefined
        })

        closeActionModal()
        await loadReports()

        const actionLabel = currentActionType === 'APPROVE' ? '通过' : '驳回'
        alert(`举报已${actionLabel}`)
      } catch (error) {
        console.error('Action failed:', error)
        alert('操作失败: ' + (error.response?.data || error.message))
      }
    }

    onMounted(() => {
      loadReports()
    })

    return {
      // State
      reportList,
      reportsLoading,
      reportPagination,
      reportSearchForm,
      showDetailModal,
      selectedReport,
      showActionModal,
      actionType,
      actionForm,

      // Computed
      menuItems,
      actionModalTitle,

      // Methods
      isActiveRoute,
      handleLogout,
      truncateText,
      formatDate,
      formatDateTime,
      getStatusLabel,
      getStatusBadgeClass,
      getTargetTypeLabel,
      loadReports,
      searchReports,
      resetReportSearch,
      changeReportPage,
      viewReportDetail,
      closeDetailModal,
      openActionModal,
      openActionModalFromDetail,
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

.content-col {
  max-width: 200px;
}

.content-link {
  color: #3b82f6;
  cursor: pointer;
}

.content-link:hover {
  text-decoration: underline;
}

.empty-row {
  text-align: center;
  color: #9ca3af;
  padding: 40px !important;
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

.detail-row {
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

.content-value {
  white-space: pre-wrap;
  word-break: break-word;
}

.action-info {
  margin-top: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.action-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px 0;
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
