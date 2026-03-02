<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="用户管理"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetUserSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>用户名/昵称</label>
            <input type="text" v-model="userSearchForm.username" class="form-input" placeholder="搜索用户名或昵称">
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="text" v-model="userSearchForm.email" class="form-input" placeholder="搜索邮箱">
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="userSearchForm.role" class="form-input">
              <option value="">全部</option>
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="userSearchForm.enabled" class="form-input">
              <option value="">全部</option>
              <option value="true">启用</option>
              <option value="false">禁用</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>注册开始日期</label>
            <input type="date" v-model="userSearchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>注册结束日期</label>
            <input type="date" v-model="userSearchForm.endDate" class="form-input">
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchUsers">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- User List -->
    <div class="card">
      <div class="card-header">
        <h3>用户列表 <span v-if="userPagination.total > 0">({{ userPagination.total }})</span></h3>
        <div class="header-actions" v-if="selectedUserIds.length > 0">
          <span class="selected-count">已选择 {{ selectedUserIds.length }} 项</span>
          <button class="btn btn-success btn-sm" @click="batchEnableUsers">批量启用</button>
          <button class="btn btn-danger btn-sm" @click="batchDisableUsers">批量禁用</button>
        </div>
      </div>
      <div v-if="usersLoading" class="loading-container">
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
            <th>用户名</th>
            <th>昵称</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>状态</th>
            <th>发文数</th>
            <th>评论数</th>
            <th>注册日期</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="userList.length === 0">
            <td colspan="11" class="empty-row">暂无用户数据</td>
          </tr>
          <tr v-for="user in userList" :key="user.id" :class="{ 'selected-row': isUserSelected(user.id) }">
            <td class="checkbox-col">
              <input
                type="checkbox"
                :checked="isUserSelected(user.id)"
                @change="toggleUserSelection(user.id)"
                :disabled="isCurrentUser(user)"
              >
            </td>
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>{{ user.email }}</td>
            <td>
              <span :class="['badge', user.role === 'ADMIN' ? 'badge-info' : 'badge-default']">
                {{ user.role === 'ADMIN' ? '管理员' : '用户' }}
              </span>
            </td>
            <td>
              <span :class="['badge', user.enabled ? 'badge-success' : 'badge-danger']">
                {{ user.enabled ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ user.postCount }}</td>
            <td>{{ user.commentCount }}</td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewUserDetail(user)">详情</button>
              <button
                v-if="user.enabled && !isCurrentUser(user) && user.role !== 'ADMIN'"
                class="action-btn danger"
                @click="disableUser(user)"
              >禁用</button>
              <button
                v-if="!user.enabled && user.role !== 'ADMIN'"
                class="action-btn success"
                @click="enableUser(user)"
              >启用</button>
            </td>
          </tr>
        </tbody>
      </table>
          <!-- Pagination -->
          <div v-if="userPagination.totalPages > 1" class="pagination">
            <button 
              class="page-btn" 
              :disabled="userPagination.page === 0"
              @click="changeUserPage(userPagination.page - 1)"
            >上一页</button>
            <span class="page-info">
              第 {{ userPagination.page + 1 }} / {{ userPagination.totalPages }} 页
            </span>
            <button 
              class="page-btn" 
              :disabled="userPagination.page >= userPagination.totalPages - 1"
              @click="changeUserPage(userPagination.page + 1)"
            >下一页</button>
          </div>
        </div>

        <!-- User Detail Modal -->
        <div v-if="showUserModal" class="modal-overlay" @click.self="closeUserModal">
          <div class="modal-content">
            <div class="modal-header">
              <h3>用户详情</h3>
              <button class="close-btn" @click="closeUserModal">&times;</button>
            </div>
            <div class="modal-body" v-if="selectedUser">
              <div class="user-detail-row">
                <span class="label">ID:</span>
                <span class="value">{{ selectedUser.id }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">用户名:</span>
                <span class="value">{{ selectedUser.username }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">昵称:</span>
                <span class="value">{{ selectedUser.nickname || '-' }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">邮箱:</span>
                <span class="value">{{ selectedUser.email }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">简介:</span>
                <span class="value">{{ selectedUser.bio || '-' }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">角色:</span>
                <span class="value">
                  <span :class="['badge', selectedUser.role === 'ADMIN' ? 'badge-info' : 'badge-default']">
                    {{ selectedUser.role === 'ADMIN' ? '管理员' : '用户' }}
                  </span>
                </span>
              </div>
              <div class="user-detail-row">
                <span class="label">状态:</span>
                <span class="value">
                  <span :class="['badge', selectedUser.enabled ? 'badge-success' : 'badge-danger']">
                    {{ selectedUser.enabled ? '启用' : '禁用' }}
                  </span>
                </span>
              </div>
              <div class="user-detail-row">
                <span class="label">发文数:</span>
                <span class="value">{{ selectedUser.postCount }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">评论数:</span>
                <span class="value">{{ selectedUser.commentCount }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">注册时间:</span>
                <span class="value">{{ formatDateTime(selectedUser.createdAt) }}</span>
              </div>
              <div class="user-detail-row">
                <span class="label">更新时间:</span>
                <span class="value">{{ formatDateTime(selectedUser.updatedAt) }}</span>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-secondary" @click="closeUserModal">关闭</button>
            </div>
          </div>
        </div>

        <!-- Status Change Modal -->
        <div v-if="showStatusModal" class="modal-overlay" @click.self="closeStatusModal">
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ isBatchStatus ? (statusForm.enabled ? '批量启用用户' : '批量禁用用户') : (statusForm.enabled ? '启用用户' : '禁用用户') }}</h3>
              <button class="close-btn" @click="closeStatusModal">&times;</button>
            </div>
            <div class="modal-body">
              <p class="warning-text">
                {{ isBatchStatus
                  ? `确定要${statusForm.enabled ? '启用' : '禁用'}选中的 ${selectedUserIds.length} 个用户吗？`
                  : `确定要${statusForm.enabled ? '启用' : '禁用'}用户 "${statusTargetUser ? statusTargetUser.username : ''}" 吗？`
                }}
              </p>
              <p v-if="!statusForm.enabled" class="sub-text">禁用后该用户将无法登录，其内容将对外隐藏。</p>

              <div class="form-group">
                <label>表单标题 <span class="optional">(可选)</span></label>
                <input type="text" v-model="statusForm.formTitle" class="form-input" :placeholder="statusForm.enabled ? '用户启用记录' : '用户禁用记录'">
              </div>
              <div class="form-group">
                <label>理由 <span class="required">*</span></label>
                <textarea v-model="statusForm.reason" class="form-input form-textarea" placeholder="请填写理由" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>扩展信息 <span class="optional">(可选)</span></label>
                <div v-for="(field, index) in statusForm.extraFieldsList" :key="index" class="extra-field-row">
                  <input type="text" v-model="field.fieldName" class="form-input extra-field-input" placeholder="字段名">
                  <input type="text" v-model="field.fieldValue" class="form-input extra-field-input" placeholder="字段值">
                  <button class="btn btn-sm btn-danger" @click="removeStatusExtraField(index)">删除</button>
                </div>
                <button class="btn btn-sm btn-secondary" @click="addStatusExtraField">+ 添加扩展字段</button>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-secondary" @click="closeStatusModal">取消</button>
              <button 
                :class="['btn', statusForm.enabled ? 'btn-success' : 'btn-danger']"
                @click="confirmStatusChange"
                :disabled="!statusForm.reason || statusForm.reason.trim() === ''"
              >{{ statusForm.enabled ? '确认启用' : '确认禁用' }}</button>
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
import { useRoute, useRouter } from 'vue-router'
import { getUsers, getUserDetail, updateUserStatus } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminUserManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    // ======================= User Management State =======================
    const userList = ref([])
    const usersLoading = ref(false)
    const userPagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const userSearchForm = ref({
      username: '',
      email: '',
      role: '',
      enabled: '',
      startDate: '',
      endDate: ''
    })
    const showUserModal = ref(false)
    const selectedUser = ref(null)
    const selectedUserIds = ref([])

    // Status change modal state
    const showStatusModal = ref(false)
    const isBatchStatus = ref(false)
    const statusTargetUser = ref(null)
    const statusForm = ref({
      enabled: false,
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
      { id: 'media', path: '/admin', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'users'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    // ======================= User Management Methods =======================

    const isCurrentUser = (user) => {
      const currentUser = store.getters.currentUser
      return currentUser && currentUser.username === user.username
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

    const loadUsers = async () => {
      usersLoading.value = true
      try {
        const params = {
          page: userPagination.value.page,
          size: userPagination.value.size,
          username: userSearchForm.value.username || undefined,
          email: userSearchForm.value.email || undefined,
          role: userSearchForm.value.role || undefined,
          enabled: userSearchForm.value.enabled === '' ? undefined : userSearchForm.value.enabled === 'true',
          startDate: userSearchForm.value.startDate || undefined,
          endDate: userSearchForm.value.endDate || undefined
        }
        const response = await getUsers(params)
        userList.value = response.content || []
        userPagination.value.total = response.totalElements || 0
        userPagination.value.totalPages = response.totalPages || 0
        // Clear selections when page changes
        selectedUserIds.value = []
      } catch (error) {
        console.error('Failed to load users:', error)
        alert('加载用户列表失败: ' + (error.message || '未知错误'))
      } finally {
        usersLoading.value = false
      }
    }

    const searchUsers = () => {
      userPagination.value.page = 0
      loadUsers()
    }

    const resetUserSearch = () => {
      userSearchForm.value = {
        username: '',
        email: '',
        role: '',
        enabled: '',
        startDate: '',
        endDate: ''
      }
      userPagination.value.page = 0
      loadUsers()
    }

    const applyRouteFilters = () => {
      const enabled = route.query.enabled
      if (enabled === 'true' || enabled === 'false') {
        userSearchForm.value.enabled = enabled
      }
    }

    const changeUserPage = (newPage) => {
      userPagination.value.page = newPage
      loadUsers()
    }

    const viewUserDetail = async (user) => {
      try {
        const detail = await getUserDetail(user.id)
        selectedUser.value = detail
        showUserModal.value = true
      } catch (error) {
        console.error('Failed to load user detail:', error)
        alert('加载用户详情失败')
      }
    }

    const closeUserModal = () => {
      showUserModal.value = false
      selectedUser.value = null
    }

    // ======================= Status Change Modal Methods =======================

    const openStatusModal = (user, enabled) => {
      statusTargetUser.value = user
      isBatchStatus.value = false
      statusForm.value = { enabled, formTitle: '', reason: '', extraFieldsList: [] }
      showStatusModal.value = true
    }

    const openBatchStatusModal = (enabled) => {
      if (selectedUserIds.value.length === 0) return
      isBatchStatus.value = true
      statusTargetUser.value = null
      statusForm.value = { enabled, formTitle: '', reason: '', extraFieldsList: [] }
      showStatusModal.value = true
    }

    const closeStatusModal = () => {
      showStatusModal.value = false
      statusTargetUser.value = null
      isBatchStatus.value = false
    }

    const addStatusExtraField = () => {
      statusForm.value.extraFieldsList.push({ fieldName: '', fieldValue: '' })
    }

    const removeStatusExtraField = (index) => {
      statusForm.value.extraFieldsList.splice(index, 1)
    }

    const getStatusExtraFieldsJson = () => {
      const validFields = statusForm.value.extraFieldsList.filter(
        f => f.fieldName && f.fieldName.trim() !== ''
      )
      return validFields.length > 0 ? JSON.stringify(validFields) : undefined
    }

    const confirmStatusChange = async () => {
      if (!statusForm.value.reason || statusForm.value.reason.trim() === '') {
        alert('请填写理由')
        return
      }

      const formData = {
        formTitle: statusForm.value.formTitle || undefined,
        reason: statusForm.value.reason,
        extraFields: getStatusExtraFieldsJson()
      }

      if (isBatchStatus.value) {
        const count = selectedUserIds.value.length
        try {
          await Promise.all(selectedUserIds.value.map(userId => 
            updateUserStatus(userId, statusForm.value.enabled, formData)
          ))
          closeStatusModal()
          selectedUserIds.value = []
          await loadUsers()
          alert(`已成功${statusForm.value.enabled ? '启用' : '禁用'} ${count} 个用户`)
        } catch (error) {
          console.error('Batch status change failed:', error)
          alert(`批量${statusForm.value.enabled ? '启用' : '禁用'}失败: ` + (error.response?.data || error.message))
        }
      } else {
        try {
          await updateUserStatus(statusTargetUser.value.id, statusForm.value.enabled, formData)
          closeStatusModal()
          await loadUsers()
          alert(statusForm.value.enabled ? '用户已启用' : '用户已禁用')
        } catch (error) {
          console.error('Status change failed:', error)
          alert(`${statusForm.value.enabled ? '启用' : '禁用'}用户失败: ` + (error.response?.data || error.message))
        }
      }
    }

    const enableUser = (user) => {
      openStatusModal(user, true)
    }

    const disableUser = (user) => {
      openStatusModal(user, false)
    }

    // ======================= Batch Selection Methods =======================

    const isUserSelected = (userId) => {
      return selectedUserIds.value.includes(userId)
    }

    const toggleUserSelection = (userId) => {
      const index = selectedUserIds.value.indexOf(userId)
      if (index === -1) {
        selectedUserIds.value.push(userId)
      } else {
        selectedUserIds.value.splice(index, 1)
      }
    }

    const isAllSelected = computed(() => {
      const selectableUsers = userList.value.filter(u => !isCurrentUser(u))
      return selectableUsers.length > 0 && selectableUsers.every(u => selectedUserIds.value.includes(u.id))
    })

    const isPartialSelected = computed(() => {
      const selectableUsers = userList.value.filter(u => !isCurrentUser(u))
      const selectedCount = selectableUsers.filter(u => selectedUserIds.value.includes(u.id)).length
      return selectedCount > 0 && selectedCount < selectableUsers.length
    })

    const toggleSelectAll = () => {
      const selectableUsers = userList.value.filter(u => !isCurrentUser(u))
      if (isAllSelected.value) {
        // Deselect all
        selectedUserIds.value = []
      } else {
        // Select all selectable users
        selectedUserIds.value = selectableUsers.map(u => u.id)
      }
    }

    const batchEnableUsers = () => {
      openBatchStatusModal(true)
    }

    const batchDisableUsers = () => {
      openBatchStatusModal(false)
    }

    onMounted(() => {
      applyRouteFilters()
      loadUsers()
    })

    return {
      menuItems,
      isActiveRoute,
      handleLogout,
      // User management
      userList,
      usersLoading,
      userPagination,
      userSearchForm,
      showUserModal,
      selectedUser,
      selectedUserIds,
      isCurrentUser,
      formatDate,
      formatDateTime,
      searchUsers,
      resetUserSearch,
      changeUserPage,
      viewUserDetail,
      closeUserModal,
      enableUser,
      disableUser,
      // Status change modal
      showStatusModal,
      isBatchStatus,
      statusTargetUser,
      statusForm,
      closeStatusModal,
      addStatusExtraField,
      removeStatusExtraField,
      confirmStatusChange,
      // Batch operations
      isUserSelected,
      toggleUserSelection,
      isAllSelected,
      isPartialSelected,
      toggleSelectAll,
      batchEnableUsers,
      batchDisableUsers
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

/* Search Card - Wider padding */
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

.btn-info {
  background: #1890ff;
  color: #fff;
}

.btn-info:hover {
  background: #40a9ff;
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

.action-btn.info {
  color: #1890ff;
}

.action-btn.info:hover {
  background: #e6f7ff;
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

.user-detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.user-detail-row:last-child {
  border-bottom: none;
}

.user-detail-row .label {
  width: 100px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
}

.user-detail-row .value {
  flex: 1;
  color: #333;
  font-size: 14px;
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

/* Form textarea */
.form-textarea {
  resize: vertical;
  min-height: 60px;
}

/* Extra fields */
.extra-field-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.extra-field-input {
  flex: 1;
}

/* Warning/Info text */
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
</style>
