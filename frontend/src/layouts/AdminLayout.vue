<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">📝</div>
          <span class="logo-text">博客管理</span>
        </div>
      </div>

      <nav class="nav-menu">
        <template v-for="item in menuItems" :key="item.id">
          <router-link
            v-if="item.path"
            :to="item.path"
            :class="['nav-item', { active: isActiveRoute(item.id) }]"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.label }}</span>
          </router-link>
          <a
            v-else
            href="#"
            :class="['nav-item', { active: currentView === item.id }]"
            @click.prevent="setCurrentView(item.id)"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.label }}</span>
          </a>
        </template>
      </nav>
    </aside>

    <div class="main-content">
      <header class="topbar">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div class="topbar-right">
          <div v-if="showSearch" class="search-box">
            <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input type="text" placeholder="搜索..." class="search-input" />
          </div>
          <router-link to="/notifications" class="notification-btn">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            <span v-if="unreadNotificationCount > 0" class="notification-badge">
              {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
            </span>
          </router-link>
          <div class="user-avatar">
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

      <div class="content-area">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { getNotificationUnreadCount } from '@/api/notifications'

export default {
  name: 'AdminLayout',
  props: {
    menuItems: {
      type: Array,
      required: true
    },
    pageTitle: {
      type: String,
      required: true
    },
    isActiveRoute: {
      type: Function,
      default: () => false
    },
    currentView: {
      type: String,
      default: ''
    },
    setCurrentView: {
      type: Function,
      default: () => {}
    },
    showSearch: {
      type: Boolean,
      default: false
    }
  },
  setup() {
    const store = useStore()
    const unreadNotificationCount = ref(0)
    const avatarLoadError = ref(false)

    const adminName = computed(() => {
      const user = store.getters.currentUser
      return user?.nickname || user?.username || '管理员'
    })

    const userInitial = computed(() => adminName.value.charAt(0).toUpperCase())

    const userAvatarUrl = computed(() => {
      const user = store.getters.currentUser
      if (!user?.avatarUrl) return null
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      return user.avatarUrl.startsWith('http') ? user.avatarUrl : `${baseUrl}${user.avatarUrl}`
    })

    const handleAvatarError = () => {
      avatarLoadError.value = true
    }

    const loadNotificationCount = async () => {
      try {
        const response = await getNotificationUnreadCount()
        unreadNotificationCount.value = response.count || 0
      } catch (error) {
        console.error('Failed to load notification count:', error)
      }
    }

    onMounted(() => {
      loadNotificationCount()
    })

    return {
      adminName,
      userInitial,
      userAvatarUrl,
      avatarLoadError,
      handleAvatarError,
      unreadNotificationCount
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

.search-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 8px 12px;
  width: 240px;
}

.search-icon {
  width: 18px;
  height: 18px;
  color: #999;
  margin-right: 8px;
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  width: 100%;
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
  text-decoration: none;
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
</style>
