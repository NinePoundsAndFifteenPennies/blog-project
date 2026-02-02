<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center p-4">
    <div class="max-w-md w-full">
      <!-- Logo and Title -->
      <div class="text-center mb-8 animate-fade-in">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-indigo-600 rounded-2xl mb-6 shadow-lg">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        <h2 class="text-4xl font-bold text-white mb-3">管理后台</h2>
        <p class="text-gray-400 text-lg">请使用管理员账户登录</p>
      </div>

      <!-- Login Form Card -->
      <div class="bg-gray-800 rounded-2xl p-8 md:p-10 shadow-xl animate-slide-up border border-gray-700">
        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- Username Field -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              用户名
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <input
                  v-model="formData.username"
                  type="text"
                  placeholder="请输入管理员用户名"
                  class="w-full bg-gray-700 border border-gray-600 text-white placeholder-gray-400 rounded-lg pl-10 pr-4 py-3 focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                  :class="{ 'border-red-500': errors.username }"
                  required
              />
            </div>
            <p v-if="errors.username" class="mt-1 text-sm text-red-400">{{ errors.username }}</p>
          </div>

          <!-- Password Field -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              密码
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input
                  v-model="formData.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  class="w-full bg-gray-700 border border-gray-600 text-white placeholder-gray-400 rounded-lg pl-10 pr-10 py-3 focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                  :class="{ 'border-red-500': errors.password }"
                  required
              />
              <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="!showPassword" class="w-5 h-5 text-gray-500 hover:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5 text-gray-500 hover:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-400">{{ errors.password }}</p>
          </div>

          <!-- Remember Me -->
          <div class="flex items-center">
            <label class="flex items-center">
              <input
                  v-model="formData.rememberMe"
                  type="checkbox"
                  class="w-4 h-4 text-indigo-600 border-gray-600 rounded focus:ring-indigo-500 bg-gray-700"
              />
              <span class="ml-2 text-sm text-gray-300">记住我</span>
            </label>
          </div>

          <!-- Error Message -->
          <div v-if="errorMessage" class="p-3 bg-red-900/50 border border-red-700 rounded-lg">
            <p class="text-sm text-red-300 flex items-center">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ errorMessage }}
            </p>
          </div>

          <!-- Submit Button -->
          <button
              type="submit"
              :disabled="loading"
              class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg flex items-center justify-center text-lg py-4 transition-colors"
              :class="{ 'opacity-70 cursor-not-allowed': loading }"
          >
            <span v-if="!loading" class="flex items-center space-x-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
              <span>管理员登录</span>
            </span>
            <span v-else class="flex items-center">
              <div class="w-5 h-5 mr-2 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              登录中...
            </span>
          </button>
        </form>
      </div>

      <!-- Back to Home -->
      <div class="text-center mt-8 animate-fade-in" style="animation-delay: 0.3s;">
        <router-link to="/" class="text-gray-400 hover:text-white text-sm inline-flex items-center space-x-2 hover:underline transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          <span>返回首页</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { adminLogin, getCurrentAdmin } from '@/api/admin'

export default {
  name: 'AdminLogin',
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    const loading = ref(false)
    const showPassword = ref(false)
    const errorMessage = ref('')

    const formData = reactive({
      username: '',
      password: '',
      rememberMe: false
    })

    const errors = reactive({
      username: '',
      password: ''
    })

    // 检查是否有来自URL的提示信息
    onMounted(() => {
      if (route.query.message) {
        errorMessage.value = route.query.message
      }
    })

    const handleLogin = async () => {
      // 简单的本地验证
      if (!formData.username || !formData.password) {
        errorMessage.value = '请输入用户名和密码';
        return;
      }

      loading.value = true
      errorMessage.value = ''

      try {
        // 调用管理员登录API
        const token = await adminLogin({
          username: formData.username,
          password: formData.password,
          rememberMe: formData.rememberMe
        })

        // 保存token
        localStorage.setItem('token', token)
        localStorage.setItem('rememberMe', formData.rememberMe.toString())
        store.commit('SET_TOKEN', { token, rememberMe: formData.rememberMe })

        // 获取管理员信息
        const user = await getCurrentAdmin()
        localStorage.setItem('user', JSON.stringify(user))
        store.commit('SET_USER', user)

        // 登录成功后跳转到管理后台
        const redirect = route.query.redirect || '/admin'
        router.push(redirect)

      } catch (error) {
        if (error.response?.status === 403) {
          errorMessage.value = '您没有管理员权限'
        } else {
          errorMessage.value = error.response?.data?.message || error.response?.data || '登录失败, 请检查用户名和密码'
        }
        console.error('管理员登录失败:', error)
      } finally {
        loading.value = false
      }
    }

    return {
      loading,
      showPassword,
      errorMessage,
      formData,
      errors,
      handleLogin
    }
  }
}
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

.animate-slide-up {
  animation: slideUp 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
