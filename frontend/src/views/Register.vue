<template>
  <div class="min-h-screen bg-gradient-to-br from-primary-50 via-white to-secondary-50 flex items-center justify-center p-4">
    <div class="max-w-md w-full">
      <!-- Logo and Title -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-primary rounded-2xl mb-4 shadow-lg">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </div>
        <h2 class="text-3xl font-bold text-gray-900 mb-2">创建账户</h2>
        <p class="text-gray-600">加入我们,开始你的创作之旅</p>
      </div>

      <!-- Register Form Card -->
      <div class="card p-8">
        <form @submit.prevent="handleRegister" class="space-y-5">
          <!-- Username Field -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              用户名
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <input
                  v-model="formData.username"
                  type="text"
                  placeholder="请输入用户名"
                  class="input-field pl-10"
                  :class="{ 'input-error': errors.username }"
                  @blur="validateUsername"
              />
            </div>
            <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
          </div>

          <!-- Email Field -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              邮箱
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <input
                  v-model="formData.email"
                  type="email"
                  placeholder="请输入邮箱"
                  class="input-field pl-10"
                  :class="{ 'input-error': errors.email }"
                  @blur="validateEmail"
              />
            </div>
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
          </div>

          <!-- Password Field -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input
                  v-model="formData.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码(至少6位)"
                  class="input-field pl-10 pr-10"
                  :class="{ 'input-error': errors.password }"
                  @input="validatePassword"
              />
              <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="!showPassword" class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            <!-- Password Strength Indicator -->
            <div v-if="formData.password" class="mt-2">
              <div class="flex gap-1">
                <div
                    v-for="i in 4"
                    :key="i"
                    class="h-1 flex-1 rounded-full transition-colors duration-200"
                    :class="i <= passwordStrength ? getStrengthColor(passwordStrength) : 'bg-gray-200'"
                ></div>
              </div>
              <p class="text-xs mt-1" :class="getStrengthTextColor(passwordStrength)">
                密码强度: {{ getStrengthText(passwordStrength) }}
              </p>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <!-- Confirm Password Field -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              确认密码
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <input
                  v-model="formData.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  placeholder="请再次输入密码"
                  class="input-field pl-10 pr-10"
                  :class="{ 'input-error': errors.confirmPassword }"
                  @blur="validateConfirmPassword"
              />
              <button
                  type="button"
                  @click="showConfirmPassword = !showConfirmPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="!showConfirmPassword" class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-600">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Terms Agreement -->
          <div class="flex items-start">
            <input
                v-model="formData.agreeToTerms"
                type="checkbox"
                class="w-4 h-4 mt-1 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
            />
            <label class="ml-2 text-sm text-gray-700">
              我已阅读并同意
              <a href="#" class="text-primary-600 hover:text-primary-700 font-medium">用户协议</a>
              和
              <a href="#" class="text-primary-600 hover:text-primary-700 font-medium">隐私政策</a>
            </label>
          </div>
          <p v-if="errors.agreeToTerms" class="text-sm text-red-600">{{ errors.agreeToTerms }}</p>

          <!-- Error Message -->
          <div v-if="errorMessage" class="p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600 flex items-center">
              <svg class="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ errorMessage }}
            </p>
          </div>

          <!-- Submit Button -->
          <button
              type="submit"
              :disabled="loading"
              class="w-full btn-primary flex items-center justify-center"
              :class="{ 'opacity-70 cursor-not-allowed': loading }"
          >
            <span v-if="!loading">注册</span>
            <span v-else class="flex items-center">
              <div class="spinner w-5 h-5 mr-2"></div>
              注册中...
            </span>
          </button>
        </form>

        <!-- Divider -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-4 bg-white text-gray-500">或</span>
          </div>
        </div>

        <!-- Login Link -->
        <div class="text-center">
          <p class="text-gray-600">
            已有账户?
            <router-link to="/login" class="text-primary-600 hover:text-primary-700 font-medium">
              立即登录
            </router-link>
          </p>
        </div>
      </div>

      <!-- Back to Home -->
      <div class="text-center mt-6">
        <router-link to="/" class="text-gray-600 hover:text-gray-900 text-sm flex items-center justify-center">
          <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'Register',
  setup() {
    const store = useStore()
    const router = useRouter()

    const loading = ref(false)
    const showPassword = ref(false)
    const showConfirmPassword = ref(false)
    const errorMessage = ref('')

    const formData = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      agreeToTerms: false
    })

    const errors = reactive({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      agreeToTerms: ''
    })

    // 计算密码强度
    const passwordStrength = computed(() => {
      const password = formData.password
      if (!password) return 0

      let strength = 0
      if (password.length >= 6) strength++
      if (password.length >= 10) strength++
      if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
      if (/\d/.test(password)) strength++
      if (/[^a-zA-Z0-9]/.test(password)) strength++

      return Math.min(strength, 4)
    })

    const getStrengthColor = (strength) => {
      const colors = ['bg-red-500', 'bg-orange-500', 'bg-yellow-500', 'bg-green-500']
      return colors[strength - 1] || 'bg-gray-200'
    }

    const getStrengthTextColor = (strength) => {
      const colors = ['text-red-600', 'text-orange-600', 'text-yellow-600', 'text-green-600']
      return colors[strength - 1] || 'text-gray-600'
    }

    const getStrengthText = (strength) => {
      const texts = ['弱', '一般', '中等', '强']
      return texts[strength - 1] || '非常弱'
    }

    // 验证用户名
    const validateUsername = () => {
      errors.username = ''
      if (!formData.username.trim()) {
        errors.username = '请输入用户名'
        return false
      }
      if (formData.username.length < 3) {
        errors.username = '用户名至少3个字符'
        return false
      }
      if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
        errors.username = '用户名只能包含字母、数字和下划线'
        return false
      }
      return true
    }

    // 验证邮箱
    const validateEmail = () => {
      errors.email = ''
      if (!formData.email.trim()) {
        errors.email = '请输入邮箱'
        return false
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(formData.email)) {
        errors.email = '请输入有效的邮箱地址'
        return false
      }
      return true
    }

    // 验证密码
    const validatePassword = () => {
      errors.password = ''
      if (!formData.password) {
        errors.password = '请输入密码'
        return false
      }
      if (formData.password.length < 6) {
        errors.password = '密码至少6个字符'
        return false
      }
      return true
    }

    // 验证确认密码
    const validateConfirmPassword = () => {
      errors.confirmPassword = ''
      if (!formData.confirmPassword) {
        errors.confirmPassword = '请再次输入密码'
        return false
      }
      if (formData.password !== formData.confirmPassword) {
        errors.confirmPassword = '两次输入的密码不一致'
        return false
      }
      return true
    }

    // 验证整个表单
    const validateForm = () => {
      const isUsernameValid = validateUsername()
      const isEmailValid = validateEmail()
      const isPasswordValid = validatePassword()
      const isConfirmPasswordValid = validateConfirmPassword()

      errors.agreeToTerms = ''
      if (!formData.agreeToTerms) {
        errors.agreeToTerms = '请阅读并同意用户协议和隐私政策'
      }

      return (
          isUsernameValid &&
          isEmailValid &&
          isPasswordValid &&
          isConfirmPasswordValid &&
          formData.agreeToTerms
      )
    }

    // 处理注册
    const handleRegister = async () => {
      if (!validateForm()) return

      loading.value = true
      errorMessage.value = ''

      try {
        // TODO: 调用store的register action
        await store.dispatch('register', {
          username: formData.username,
          email: formData.email,
          password: formData.password
        })

        // 注册成功后跳转到首页
        router.push('/')
      } catch (error) {
        errorMessage.value = error.response?.data?.message || '注册失败,请稍后重试'
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }

    return {
      loading,
      showPassword,
      showConfirmPassword,
      errorMessage,
      formData,
      errors,
      passwordStrength,
      getStrengthColor,
      getStrengthTextColor,
      getStrengthText,
      validateUsername,
      validateEmail,
      validatePassword,
      validateConfirmPassword,
      handleRegister
    }
  }
}
</script>