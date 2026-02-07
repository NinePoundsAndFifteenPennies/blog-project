<template>
  <AdminLayout
    :menu-items="menuItems"
    :page-title="currentPageTitle"
    :current-view="currentView"
    :set-current-view="setCurrentView"
    :show-search="true"
  >
        <!-- Dashboard View with Welcome Animation -->
        <div v-if="currentView === 'dashboard'" class="view-dashboard">
          <!-- Welcome Banner with Animation -->
          <transition name="welcome-fade" appear>
            <div v-if="showWelcome" class="welcome-banner">
              <div class="welcome-content">
                <div class="welcome-icon">👋</div>
                <div class="welcome-text">
                  <h2 class="welcome-title">
                    <span class="typing-text">{{ typedWelcome }}</span>
                    <span class="cursor">|</span>
                  </h2>
                  <p class="welcome-subtitle">欢迎回到管理后台，祝您工作愉快！</p>
                </div>
              </div>
              <button class="welcome-close" @click="showWelcome = false">×</button>
            </div>
          </transition>

          <!-- Stats Grid -->
          <div class="stats-grid">
            <div class="stat-card" v-for="stat in statsData" :key="stat.label">
              <div class="stat-info">
                <span class="stat-label">{{ stat.label }}</span>
                <span class="stat-value">{{ stat.value }}</span>
                <span :class="['stat-trend', stat.trendUp ? 'trend-up' : 'trend-down']">
                  <span class="trend-arrow">{{ stat.trendUp ? '↑' : '↓' }}</span>
                  {{ stat.trend }}
                </span>
              </div>
              <div :class="['stat-icon', stat.iconClass]" v-html="stat.icon"></div>
            </div>
          </div>

          <!-- Recent Articles -->
          <div class="card">
            <div class="card-header">
              <h3>最新文章</h3>
              <button class="btn btn-primary">
                <span>+ 新建文章</span>
              </button>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>标题</th>
                  <th>作者</th>
                  <th>分类</th>
                  <th>状态</th>
                  <th>发布日期</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="article in recentArticles" :key="article.id">
                  <td>{{ article.title }}</td>
                  <td>{{ article.author }}</td>
                  <td>{{ article.category }}</td>
                  <td>
                    <span :class="['badge', getBadgeClass(article.status)]">
                      {{ article.statusText }}
                    </span>
                  </td>
                  <td>{{ article.date }}</td>
                  <td class="actions">
                    <button class="action-btn">编辑</button>
                    <button class="action-btn danger">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Articles View -->
        <div v-if="currentView === 'articles'" class="view-articles">
          <div class="card">
            <div class="card-header">
              <div class="filter-tabs">
                <button 
                  v-for="filter in articleFilters" 
                  :key="filter.id"
                  :class="['filter-btn', { active: currentFilter === filter.id }]"
                  @click="currentFilter = filter.id"
                >
                  {{ filter.label }}
                </button>
              </div>
              <button class="btn btn-primary">+ 新建文章</button>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>标题</th>
                  <th>作者</th>
                  <th>分类</th>
                  <th>状态</th>
                  <th>发布日期</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="article in recentArticles" :key="article.id">
                  <td>{{ article.title }}</td>
                  <td>{{ article.author }}</td>
                  <td>{{ article.category }}</td>
                  <td>
                    <span :class="['badge', getBadgeClass(article.status)]">
                      {{ article.statusText }}
                    </span>
                  </td>
                  <td>{{ article.date }}</td>
                  <td class="actions">
                    <button class="action-btn">编辑</button>
                    <button class="action-btn danger">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Categories View -->
        <div v-if="currentView === 'categories'" class="view-categories">
          <div class="card">
            <div class="card-header">
              <h3>分类管理</h3>
              <button class="btn btn-primary">+ 新建分类</button>
            </div>
            <div class="category-grid">
              <div class="category-card" v-for="cat in categories" :key="cat.id">
                <h4>{{ cat.name }}</h4>
                <span class="category-count">{{ cat.count }} 篇文章</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tags View -->
        <div v-if="currentView === 'tags'" class="view-tags">
          <div class="card">
            <div class="card-header">
              <h3>标签管理</h3>
              <button class="btn btn-primary">+ 新建标签</button>
            </div>
            <div class="tags-list">
              <span class="tag-item" v-for="tag in tags" :key="tag.id">
                {{ tag.name }} ({{ tag.count }})
              </span>
            </div>
          </div>
        </div>

        <!-- Comments View -->
        <div v-if="currentView === 'comments'" class="view-comments">
          <div class="card">
            <div class="card-header">
              <div class="filter-tabs">
                <button 
                  v-for="filter in commentFilters" 
                  :key="filter.id"
                  :class="['filter-btn', { active: currentCommentFilter === filter.id }]"
                  @click="currentCommentFilter = filter.id"
                >
                  {{ filter.label }}
                </button>
              </div>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>内容</th>
                  <th>作者</th>
                  <th>文章</th>
                  <th>状态</th>
                  <th>时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="comment in comments" :key="comment.id">
                  <td class="comment-content">{{ comment.content }}</td>
                  <td>{{ comment.author }}</td>
                  <td>{{ comment.article }}</td>
                  <td>
                    <span :class="['badge', getBadgeClass(comment.status)]">
                      {{ comment.statusText }}
                    </span>
                  </td>
                  <td>{{ comment.date }}</td>
                  <td class="actions">
                    <button class="action-btn">通过</button>
                    <button class="action-btn danger">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Media View -->
        <div v-if="currentView === 'media'" class="view-media">
          <div class="card">
            <div class="card-header">
              <h3>媒体库</h3>
              <button class="btn btn-primary">+ 上传文件</button>
            </div>
            <div class="media-grid">
              <div class="media-item" v-for="i in 8" :key="i">
                <div class="media-placeholder">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Settings View -->
        <div v-if="currentView === 'settings'" class="view-settings">
          <div class="settings-section">
            <div class="card">
              <div class="card-header">
                <h3>基本设置</h3>
              </div>
              <div class="settings-form">
                <div class="form-group">
                  <label>网站名称</label>
                  <input type="text" class="form-input" value="我的博客" />
                </div>
                <div class="form-group">
                  <label>网站描述</label>
                  <textarea class="form-input" rows="3">分享技术与生活</textarea>
                </div>
                <div class="form-group">
                  <label>网站关键词</label>
                  <input type="text" class="form-input" value="博客, 技术, 生活" />
                </div>
              </div>
            </div>
          </div>

          <div class="settings-section">
            <div class="card">
              <div class="card-header">
                <h3>评论设置</h3>
              </div>
              <div class="settings-form">
                <div class="form-group switch-group">
                  <label>开启评论</label>
                  <div class="switch active">
                    <div class="switch-handle"></div>
                  </div>
                </div>
                <div class="form-group switch-group">
                  <label>评论审核</label>
                  <div class="switch">
                    <div class="switch-handle"></div>
                  </div>
                </div>
                <div class="form-group switch-group">
                  <label>允许访客评论</label>
                  <div class="switch active">
                    <div class="switch-handle"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="settings-actions">
            <button class="btn btn-primary">保存设置</button>
          </div>
        </div>

        <!-- Quick Links -->
        <div class="quick-links" v-if="currentView === 'dashboard'">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminDashboard',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const currentView = ref('dashboard')
    const currentFilter = ref('all')
    const currentCommentFilter = ref('all')

    // ======================= Welcome Animation State =======================
    const showWelcome = ref(true)
    const typedWelcome = ref('')
    const welcomeText = ref('')
    let typingInterval = null

    const adminName = computed(() => {
      const user = store.getters.currentUser
      return user?.nickname || user?.username || '管理员'
    })

    const currentPageTitle = computed(() => {
      const titles = {
        dashboard: '仪表盘',
        articles: '文章管理',
        categories: '分类管理',
        tags: '标签管理',
        comments: '评论管理',
        media: '媒体库',
        users: '用户管理',
        settings: '系统设置'
      }
      return titles[currentView.value] || '仪表盘'
    })

    const menuItems = [
      { id: 'dashboard', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin/categories', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin/tags', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin/comments', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'media', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const statsData = [
      { label: '总文章数', value: '128', trend: '+12%', trendUp: true, iconClass: 'icon-blue', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { label: '总用户数', value: '1,024', trend: '+8%', trendUp: true, iconClass: 'icon-green', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { label: '总评论数', value: '2,456', trend: '+15%', trendUp: true, iconClass: 'icon-yellow', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { label: '今日访问', value: '342', trend: '-3%', trendUp: false, iconClass: 'icon-purple', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>' }
    ]

    const articleFilters = [
      { id: 'all', label: '全部' },
      { id: 'published', label: '已发布' },
      { id: 'draft', label: '草稿' },
      { id: 'pending', label: '待审核' }
    ]

    const commentFilters = [
      { id: 'all', label: '全部' },
      { id: 'pending', label: '待审核' },
      { id: 'approved', label: '已通过' },
      { id: 'spam', label: '垃圾评论' }
    ]

    const recentArticles = [
      { id: 1, title: 'Vue 3 组合式 API 入门指南', author: 'Admin', category: '技术', status: 'published', statusText: '已发布', date: '2024-01-15' },
      { id: 2, title: 'Spring Boot 安全最佳实践', author: 'Admin', category: '后端', status: 'published', statusText: '已发布', date: '2024-01-14' },
      { id: 3, title: '如何构建高性能的 Web 应用', author: 'Admin', category: '架构', status: 'draft', statusText: '草稿', date: '2024-01-13' },
      { id: 4, title: 'Docker 容器化部署指南', author: 'Admin', category: '运维', status: 'pending', statusText: '待审核', date: '2024-01-12' },
      { id: 5, title: 'MySQL 性能优化技巧', author: 'Admin', category: '数据库', status: 'published', statusText: '已发布', date: '2024-01-11' }
    ]

    const categories = [
      { id: 1, name: '技术', count: 45 },
      { id: 2, name: '后端', count: 32 },
      { id: 3, name: '前端', count: 28 },
      { id: 4, name: '架构', count: 15 },
      { id: 5, name: '运维', count: 12 },
      { id: 6, name: '数据库', count: 8 }
    ]

    const tags = [
      { id: 1, name: 'Vue', count: 24 },
      { id: 2, name: 'React', count: 18 },
      { id: 3, name: 'JavaScript', count: 35 },
      { id: 4, name: 'TypeScript', count: 22 },
      { id: 5, name: 'Node.js', count: 15 },
      { id: 6, name: 'Spring', count: 28 },
      { id: 7, name: 'Docker', count: 12 },
      { id: 8, name: 'Kubernetes', count: 8 }
    ]

    const comments = [
      { id: 1, content: '非常棒的文章，学到了很多！', author: '用户A', article: 'Vue 3 入门指南', status: 'approved', statusText: '已通过', date: '2024-01-15' },
      { id: 2, content: '请问有相关的代码示例吗？', author: '用户B', article: 'Spring Boot 安全', status: 'pending', statusText: '待审核', date: '2024-01-14' },
      { id: 3, content: '文章内容很实用，感谢分享', author: '用户C', article: 'Docker 部署指南', status: 'approved', statusText: '已通过', date: '2024-01-13' }
    ]

    const getBadgeClass = (status) => {
      const classes = {
        published: 'badge-success',
        active: 'badge-success',
        approved: 'badge-success',
        draft: 'badge-warning',
        pending: 'badge-info',
        inactive: 'badge-danger',
        spam: 'badge-danger'
      }
      return classes[status] || 'badge-default'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    const setCurrentView = (viewId) => {
      currentView.value = viewId
    }

    // ======================= Welcome Animation Methods =======================
    const startTypingAnimation = () => {
      const fullText = `您好，${adminName.value}！`
      welcomeText.value = fullText
      typedWelcome.value = ''
      let index = 0
      
      typingInterval = setInterval(() => {
        if (index < fullText.length) {
          typedWelcome.value += fullText[index]
          index++
        } else {
          clearInterval(typingInterval)
        }
      }, 100)
    }

    onMounted(() => {
      startTypingAnimation()
    })

    onUnmounted(() => {
      if (typingInterval) {
        clearInterval(typingInterval)
      }
    })

    return {
      currentView,
      currentFilter,
      currentCommentFilter,
      adminName,
      currentPageTitle,
      menuItems,
      statsData,
      articleFilters,
      commentFilters,
      recentArticles,
      categories,
      tags,
      comments,
      getBadgeClass,
      handleLogout,
      setCurrentView,
      // Welcome animation
      showWelcome,
      typedWelcome
    }
  }
}
</script>

<style scoped>

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1d2e;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-up {
  color: #52c41a;
}

.trend-down {
  color: #ff4d4f;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 28px;
  height: 28px;
}

.icon-blue {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.icon-green {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.icon-yellow {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
}

.icon-purple {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

/* Card */
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1d2e;
  margin: 0;
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: #1890ff;
  color: #fff;
}

.btn-primary:hover {
  background: #40a9ff;
  transform: translateY(-1px);
}

/* Filter Tabs */
.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-btn {
  padding: 6px 16px;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.filter-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

/* Data Table */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 20px;
  font-size: 13px;
  font-weight: 500;
  color: #999;
  border-bottom: 2px solid #f0f0f0;
}

.data-table td {
  padding: 16px 20px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}

.data-table tr:hover {
  background: #fafafa;
}

.comment-content {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Badges */
.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.badge-warning {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
}

.badge-info {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.badge-danger {
  background: rgba(255, 77, 79, 0.1);
  color: #ff4d4f;
}

.badge-default {
  background: #f5f5f5;
  color: #999;
}

/* Action Buttons */
.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 12px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.action-btn.danger:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}

/* Category Grid */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding: 20px;
}

.category-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s;
  cursor: pointer;
}

.category-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.category-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #1a1d2e;
}

.category-count {
  font-size: 13px;
  color: #999;
}

/* Tags List */
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px;
}

.tag-item {
  background: #f5f7fa;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-item:hover {
  background: #1890ff;
  color: #fff;
}

/* Media Grid */
.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  padding: 20px;
}

.media-item {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.media-item:hover {
  transform: scale(1.05);
}

.media-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.media-placeholder svg {
  width: 40px;
  height: 40px;
  color: #ccc;
}

/* Settings */
.settings-section {
  margin-bottom: 24px;
}

.settings-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
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
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.switch-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch {
  width: 44px;
  height: 24px;
  background: #e8e8e8;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
}

.switch.active {
  background: #1890ff;
}

.switch-handle {
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: left 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.switch.active .switch-handle {
  left: 22px;
}

.settings-actions {
  display: flex;
  justify-content: flex-end;
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

/* ======================= User Management Styles ======================= */

/* Search Form */
.search-card {
  margin-bottom: 20px;
}

.search-form {
  padding: 16px 0;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-row .form-group {
  flex: 1;
  min-width: 180px;
  margin-bottom: 0;
}

.search-btn-group {
  display: flex;
  align-items: flex-end;
}

/* Loading Container */
.loading-container {
  padding: 40px;
  text-align: center;
}

.loading-spinner {
  color: #666;
  font-size: 14px;
}

/* Empty Row */
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

/* Action Button Variants */
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

/* Button secondary variant */
.btn-secondary {
  background: #fff;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  border-color: #1890ff;
  color: #1890ff;
}

/* Modal Overlay */
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

/* User Detail Row */
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

/* Actions column - make it wider for more buttons */
.data-table td.actions {
  white-space: nowrap;
}

.data-table .actions .action-btn {
  margin-right: 4px;
}

/* ======================= Welcome Banner Styles ======================= */
.welcome-banner {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 50%, #69c0ff 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 1;
}

.welcome-icon {
  font-size: 48px;
  animation: wave 1.5s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-10deg); }
}

.welcome-text {
  color: #fff;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}

.typing-text {
  display: inline-block;
}

.cursor {
  display: inline-block;
  width: 3px;
  margin-left: 2px;
  animation: blink 0.8s step-end infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.welcome-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.welcome-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 1;
}

.welcome-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.welcome-fade-enter-active {
  animation: slideDown 0.5s ease-out;
}

.welcome-fade-leave-active {
  animation: slideUp 0.3s ease-in;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-20px);
  }
}

</style>
