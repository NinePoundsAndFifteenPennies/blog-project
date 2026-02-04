<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">📝</div>
          <span class="logo-text">博客管理</span>
        </div>
      </div>
      
      <nav class="nav-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.id"
          :to="item.path"
          :class="['nav-item', { active: isActiveRoute(item.id) }]"
        >
          <span class="nav-icon" v-html="item.icon"></span>
          <span class="nav-text">{{ item.label }}</span>
        </router-link>
      </nav>
    </aside>

    <!-- Main Content -->
    <div class="main-content">
      <!-- Topbar -->
      <header class="topbar">
        <h1 class="page-title">用户管理</h1>
        <div class="topbar-right">
          <!-- Notification Bell -->
          <div class="relative">
            <router-link to="/notifications" class="notification-btn">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <span v-if="unreadNotificationCount > 0" class="notification-badge">
                {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
              </span>
            </router-link>
          </div>
          <!-- User Avatar -->
          <div class="user-avatar" :style="userAvatarStyle">
            <img 
              v-if="userAvatarUrl && !avatarLoadError" 
              :src="userAvatarUrl" 
              :alt="adminName"
              @error="handleAvatarError"
              class="avatar-img"
            />
            <span v-else class="avatar-initial">{{ userInitial }}</span>
          </div>
        </div>
      </header>

      <!-- Content Area -->
      <div class="content-area">
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
              <button class="btn btn-info btn-sm" @click="batchSetAdmin">批量设为管理员</button>
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
                    v-if="user.role !== 'ADMIN'" 
                    class="action-btn info" 
                    @click="promoteToAdmin(user)"
                  >设为管理员</button>
                  <button 
                    v-if="user.role === 'ADMIN' && !isCurrentUser(user)" 
                    class="action-btn warning" 
                    @click="demoteToUser(user)"
                  >取消管理员</button>
                  <button 
                    v-if="user.enabled && !isCurrentUser(user)" 
                    class="action-btn danger" 
                    @click="disableUser(user)"
                  >禁用</button>
                  <button 
                    v-if="!user.enabled" 
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
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getUsers, getUserDetail, updateUserStatus, updateUserRole } from '@/api/admin'
import { getUnreadCount } from '@/api/notifications'

export default {
  name: 'AdminUserManagement',
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= Notification State =======================
    const unreadNotificationCount = ref(0)

    // ======================= User Avatar State =======================
    const avatarLoadError = ref(false)

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

    const adminName = computed(() => {
      const user = store.getters.currentUser
      return user?.nickname || user?.username || '管理员'
    })

    const userInitial = computed(() => {
      return adminName.value.charAt(0).toUpperCase()
    })

    const userAvatarUrl = computed(() => {
      const user = store.getters.currentUser
      if (!user?.avatarUrl) return null
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      return user.avatarUrl.startsWith('http') ? user.avatarUrl : `${baseUrl}${user.avatarUrl}`
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
      return id === 'users'
    }

    const handleAvatarError = () => {
      avatarLoadError.value = true
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

    const enableUser = async (user) => {
      if (!confirm(`确定要启用用户 "${user.username}" 吗？`)) return
      try {
        await updateUserStatus(user.id, true)
        await loadUsers()
        alert('用户已启用')
      } catch (error) {
        console.error('Failed to enable user:', error)
        alert('启用用户失败: ' + (error.response?.data || error.message))
      }
    }

    const disableUser = async (user) => {
      if (!confirm(`确定要禁用用户 "${user.username}" 吗？\n禁用后该用户将无法登录，其内容将对外隐藏。`)) return
      try {
        await updateUserStatus(user.id, false)
        await loadUsers()
        alert('用户已禁用')
      } catch (error) {
        console.error('Failed to disable user:', error)
        alert('禁用用户失败: ' + (error.response?.data || error.message))
      }
    }

    const promoteToAdmin = async (user) => {
      if (!confirm(`确定要将用户 "${user.username}" 设为管理员吗？`)) return
      try {
        await updateUserRole(user.id, 'ADMIN')
        await loadUsers()
        alert('已将用户设为管理员')
      } catch (error) {
        console.error('Failed to promote user:', error)
        alert('设置管理员失败: ' + (error.response?.data || error.message))
      }
    }

    const demoteToUser = async (user) => {
      if (!confirm(`确定要取消用户 "${user.username}" 的管理员权限吗？`)) return
      try {
        await updateUserRole(user.id, 'USER')
        await loadUsers()
        alert('已取消管理员权限')
      } catch (error) {
        console.error('Failed to demote user:', error)
        alert('取消管理员失败: ' + (error.response?.data || error.message))
      }
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

    const batchEnableUsers = async () => {
      if (selectedUserIds.value.length === 0) return
      if (!confirm(`确定要批量启用 ${selectedUserIds.value.length} 个用户吗？`)) return
      
      const count = selectedUserIds.value.length
      try {
        await Promise.all(selectedUserIds.value.map(userId => updateUserStatus(userId, true)))
        selectedUserIds.value = []
        await loadUsers()
        alert(`已成功启用 ${count} 个用户`)
      } catch (error) {
        console.error('Batch enable failed:', error)
        alert('批量启用失败: ' + (error.response?.data || error.message))
      }
    }

    const batchDisableUsers = async () => {
      if (selectedUserIds.value.length === 0) return
      if (!confirm(`确定要批量禁用 ${selectedUserIds.value.length} 个用户吗？\n禁用后这些用户将无法登录。`)) return
      
      const count = selectedUserIds.value.length
      try {
        await Promise.all(selectedUserIds.value.map(userId => updateUserStatus(userId, false)))
        selectedUserIds.value = []
        await loadUsers()
        alert(`已成功禁用 ${count} 个用户`)
      } catch (error) {
        console.error('Batch disable failed:', error)
        alert('批量禁用失败: ' + (error.response?.data || error.message))
      }
    }

    const batchSetAdmin = async () => {
      if (selectedUserIds.value.length === 0) return
      if (!confirm(`确定要将 ${selectedUserIds.value.length} 个用户设为管理员吗？`)) return
      
      const count = selectedUserIds.value.length
      try {
        await Promise.all(selectedUserIds.value.map(userId => updateUserRole(userId, 'ADMIN')))
        selectedUserIds.value = []
        await loadUsers()
        alert(`已成功将 ${count} 个用户设为管理员`)
      } catch (error) {
        console.error('Batch set admin failed:', error)
        alert('批量设置管理员失败: ' + (error.response?.data || error.message))
      }
    }

    // Load notification count
    const loadNotificationCount = async () => {
      try {
        const response = await getUnreadCount()
        unreadNotificationCount.value = response.count || 0
      } catch (error) {
        console.error('Failed to load notification count:', error)
      }
    }

    onMounted(() => {
      loadUsers()
      loadNotificationCount()
    })

    return {
      adminName,
      userInitial,
      userAvatarUrl,
      avatarLoadError,
      handleAvatarError,
      unreadNotificationCount,
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
      promoteToAdmin,
      demoteToUser,
      // Batch operations
      isUserSelected,
      toggleUserSelection,
      isAllSelected,
      isPartialSelected,
      toggleSelectAll,
      batchEnableUsers,
      batchDisableUsers,
      batchSetAdmin
    }
  }
}
</script>

<style scoped>
/* Layout */
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
}

/* Sidebar */
.sidebar {
  width: 240px;
  background-color: #1a1d2e;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: #1890ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
}

.nav-menu {
  padding: 16px 0;
  flex: 1;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.active {
  background: #1890ff;
  color: #fff;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
}

/* Topbar */
.topbar {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1d2e;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.notification-btn:hover {
  background: #e8e8e8;
  color: #1890ff;
}

.notification-btn svg {
  width: 20px;
  height: 20px;
}

.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #f5222d;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-initial {
  text-transform: uppercase;
}

/* Content Area */
.content-area {
  padding: 24px;
  flex: 1;
}

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
</style>
