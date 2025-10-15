<template>
  <header
      class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
      :class="[scrolled ? 'bg-white/90 backdrop-blur-md shadow-md' : 'bg-transparent']"
  >
    <nav class="container mx-auto px-4 py-4">
      <div class="flex items-center justify-between">
        <!-- Logo -->
        <router-link to="/" class="flex items-center space-x-2 group">
          <div class="w-10 h-10 bg-gradient-primary rounded-lg flex items-center justify-center transform group-hover:rotate-12 transition-transform duration-300">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
          <span class="text-xl font-bold bg-gradient-primary bg-clip-text text-transparent">
            博客系统
          </span>
        </router-link>

        <!-- Desktop Navigation -->
        <div class="hidden md:flex items-center space-x-8">
          <router-link
              to="/"
              class="text-gray-700 hover:text-primary-600 font-medium transition-colors duration-200"
              active-class="text-primary-600"
          >
            首页
          </router-link>

          <router-link
              v-if="isLoggedIn"
              to="/post/create"
              class="text-gray-700 hover:text-primary-600 font-medium transition-colors duration-200"
              active-class="text-primary-600"
          >
            写文章
          </router-link>
        </div>

        <!-- Right Section -->
        <div class="hidden md:flex items-center space-x-4">
          <!-- Search Bar -->
          <div class="relative">
            <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索文章..."
                class="w-64 px-4 py-2 pl-10 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200"
                @keyup.enter="handleSearch"
            >
            <svg class="w-5 h-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>

          <!-- User Menu -->
          <div v-if="isLoggedIn" class="relative">
            <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center space-x-2 hover:opacity-80 transition-opacity duration-200"
            >
              <div 
                class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold overflow-hidden bg-gradient-primary"
              >
                <img 
                  v-if="currentUser?.avatarUrl && !avatarLoadError" 
                  :src="currentUser.avatarUrl" 
                  :alt="currentUser.username"
                  :key="currentUser.avatarUrl"
                  class="w-full h-full object-cover"
                  @error="handleAvatarError"
                  @load="handleAvatarLoad"
                />
                <span v-else>{{ userInitial }}</span>
              </div>
              <svg class="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <!-- Dropdown Menu -->
            <transition name="fade">
              <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-2 border border-gray-100"
              >
                <router-link
                    to="/profile"
                    class="block px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                    @click="showUserMenu = false"
                >
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span>个人中心</span>
                  </div>
                </router-link>
                <button
                    @click="handleLogout"
                    class="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-50 transition-colors duration-200"
                >
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    <span>退出登录</span>
                  </div>
                </button>
              </div>
            </transition>
          </div>

          <!-- Login/Register Buttons -->
          <div v-else class="flex items-center space-x-3">
            <router-link to="/login" class="btn-ghost">
              登录
            </router-link>
            <router-link to="/register" class="btn-primary">
              注册
            </router-link>
          </div>
        </div>

        <!-- Mobile Menu Button -->
        <button
            @click="showMobileMenu = !showMobileMenu"
            class="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors duration-200"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path v-if="!showMobileMenu" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Mobile Menu -->
      <transition name="slide-up">
        <div v-if="showMobileMenu" class="md:hidden mt-4 pb-4 border-t border-gray-200 pt-4">
          <div class="space-y-3">
            <router-link
                to="/"
                class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                @click="showMobileMenu = false"
            >
              首页
            </router-link>
            <router-link
                v-if="isLoggedIn"
                to="/post/create"
                class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                @click="showMobileMenu = false"
            >
              写文章
            </router-link>

            <div v-if="isLoggedIn" class="border-t border-gray-200 pt-3 mt-3">
              <router-link
                  to="/profile"
                  class="block px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                  @click="showMobileMenu = false"
              >
                个人中心
              </router-link>
              <button
                  @click="handleLogout"
                  class="w-full text-left px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              >
                退出登录
              </button>
            </div>

            <div v-else class="flex flex-col space-y-2">
              <router-link to="/login" class="btn-ghost text-center" @click="showMobileMenu = false">
                登录
              </router-link>
              <router-link to="/register" class="btn-primary text-center" @click="showMobileMenu = false">
                注册
              </router-link>
            </div>
          </div>
        </div>
      </transition>
    </nav>
  </header>
</template>

<script>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'Header',
  setup() {
    const store = useStore()
    const router = useRouter()

    const scrolled = ref(false)
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const searchQuery = ref('')
    const avatarLoadError = ref(false)

    const isLoggedIn = computed(() => store.getters.isLoggedIn)
    const currentUser = computed(() => store.getters.currentUser)
    const userInitial = computed(() => {
      return currentUser.value?.username?.charAt(0).toUpperCase() || 'U'
    })

    // 处理搜索
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        // TODO: 搜索功能暂未实现
        console.log('搜索:', searchQuery.value)
        searchQuery.value = ''
      }
    }

    // 处理头像加载错误
    const handleAvatarError = () => {
      avatarLoadError.value = true
    }

    const handleAvatarLoad = () => {
      avatarLoadError.value = false
    }

    // 处理登出
    const handleLogout = () => {
      store.dispatch('logout')
      showUserMenu.value = false
      showMobileMenu.value = false
      router.push('/login')
    }

    // 监听滚动
    const handleScroll = () => {
      scrolled.value = window.scrollY > 20
    }

    // 点击外部关闭菜单
    const handleClickOutside = (e) => {
      if (!e.target.closest('.relative')) {
        showUserMenu.value = false
      }
    }

    onMounted(() => {
      window.addEventListener('scroll', handleScroll)
      document.addEventListener('click', handleClickOutside)
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
      document.removeEventListener('click', handleClickOutside)
    })

    return {
      scrolled,
      showUserMenu,
      showMobileMenu,
      searchQuery,
      isLoggedIn,
      currentUser,
      userInitial,
      avatarLoadError,
      handleSearch,
      handleLogout,
      handleAvatarError,
      handleAvatarLoad
    }
  }
}
</script>