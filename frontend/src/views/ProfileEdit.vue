<template>
  <div class="min-h-screen bg-gray-50">
    <Header />

    <div class="pt-20 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto">
          <!-- Header -->
          <div class="mb-6">
            <div class="flex items-center justify-between">
              <div>
                <h1 class="text-3xl font-bold text-gray-900">编辑个人资料</h1>
                <p class="text-gray-600 mt-1">更新您的个人信息和账户设置</p>
              </div>
              <router-link to="/profile" class="btn-ghost">
                <svg class="w-5 h-5 mr-1 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                取消
              </router-link>
            </div>
          </div>

          <!-- Form Card -->
          <div class="card p-8 md:p-10 bg-white shadow-md">
            <form @submit.prevent="handleSubmit" class="space-y-6">
              <!-- Nickname Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  昵称 <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <input
                      v-model="formData.nickname"
                      type="text"
                      placeholder="请输入昵称（1-50字符，需唯一）"
                      class="input-field"
                      :class="{ 'input-error': errors.nickname }"
                      @blur="validateNickname"
                  />
                </div>
                <p v-if="errors.nickname" class="mt-1 text-sm text-red-600">{{ errors.nickname }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">昵称将在您的文章和评论中显示</p>
              </div>

              <!-- Bio Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  个人简介
                </label>
                <textarea
                    v-model="formData.bio"
                    placeholder="介绍一下自己吧（最多200字）"
                    rows="4"
                    class="input-field resize-none"
                    :class="{ 'input-error': errors.bio }"
                    @blur="validateBio"
                ></textarea>
                <div class="flex justify-between mt-1">
                  <p v-if="errors.bio" class="text-sm text-red-600">{{ errors.bio }}</p>
                  <p class="text-xs text-gray-500 ml-auto">{{ bioLength }}/200</p>
                </div>
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
                <p v-else class="mt-1 text-xs text-gray-500">邮箱需唯一且格式正确</p>
              </div>

              <!-- Social Link Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  社交链接
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                    </svg>
                  </div>
                  <input
                      v-model="formData.socialLink"
                      type="url"
                      placeholder="https://github.com/yourname"
                      class="input-field pl-10"
                      :class="{ 'input-error': errors.socialLink }"
                      @blur="validateSocialLink"
                  />
                </div>
                <p v-if="errors.socialLink" class="mt-1 text-sm text-red-600">{{ errors.socialLink }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">您的个人网站、GitHub等社交媒体链接</p>
              </div>

              <!-- Gender Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  性别
                </label>
                <input
                    v-model="formData.gender"
                    type="text"
                    placeholder="请输入性别（最多20字符）"
                    class="input-field"
                    :class="{ 'input-error': errors.gender }"
                    @blur="validateGender"
                />
                <p v-if="errors.gender" class="mt-1 text-sm text-red-600">{{ errors.gender }}</p>
              </div>

              <!-- Birthday Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  生日
                </label>
                <input
                    v-model="formData.birthday"
                    type="date"
                    :max="todayDate"
                    class="input-field"
                    :class="{ 'input-error': errors.birthday }"
                    @blur="validateBirthday"
                />
                <p v-if="errors.birthday" class="mt-1 text-sm text-red-600">{{ errors.birthday }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">格式：yyyy-MM-dd，不能是未来日期</p>
              </div>

              <!-- Location Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  所在地
                </label>
                <input
                    v-model="formData.location"
                    type="text"
                    placeholder="请输入所在地（最多100字符）"
                    class="input-field"
                    :class="{ 'input-error': errors.location }"
                    @blur="validateLocation"
                />
                <p v-if="errors.location" class="mt-1 text-sm text-red-600">{{ errors.location }}</p>
              </div>

              <!-- Divider -->
              <div class="relative my-8">
                <div class="absolute inset-0 flex items-center">
                  <div class="w-full border-t border-gray-200"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                  <span class="px-4 bg-white text-gray-500 font-medium">修改密码（可选）</span>
                </div>
              </div>

              <!-- Current Password Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  当前密码
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                      v-model="formData.currentPassword"
                      :type="showCurrentPassword ? 'text' : 'password'"
                      placeholder="如需修改密码，请输入当前密码"
                      class="input-field pl-10 pr-10"
                      :class="{ 'input-error': errors.currentPassword }"
                      @blur="validatePasswordChange"
                  />
                  <button
                      type="button"
                      @click="showCurrentPassword = !showCurrentPassword"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center"
                  >
                    <svg v-if="!showCurrentPassword" class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    <svg v-else class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  </button>
                </div>
                <p v-if="errors.currentPassword" class="mt-1 text-sm text-red-600">{{ errors.currentPassword }}</p>
              </div>

              <!-- New Password Field -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  新密码
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <input
                      v-model="formData.newPassword"
                      :type="showNewPassword ? 'text' : 'password'"
                      placeholder="请输入新密码（至少6位）"
                      class="input-field pl-10 pr-10"
                      :class="{ 'input-error': errors.newPassword }"
                      @blur="validatePasswordChange"
                  />
                  <button
                      type="button"
                      @click="showNewPassword = !showNewPassword"
                      class="absolute inset-y-0 right-0 pr-3 flex items-center"
                  >
                    <svg v-if="!showNewPassword" class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    <svg v-else class="w-5 h-5 text-gray-400 hover:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  </button>
                </div>
                <p v-if="errors.newPassword" class="mt-1 text-sm text-red-600">{{ errors.newPassword }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">如需修改密码，新密码长度至少6个字符</p>
              </div>

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
                  class="w-full btn-primary flex items-center justify-center text-lg py-4"
                  :class="{ 'opacity-70 cursor-not-allowed': loading }"
              >
                <span v-if="!loading" class="flex items-center space-x-2">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <span>保存修改</span>
                </span>
                <span v-else class="flex items-center">
                  <div class="spinner w-5 h-5 mr-2"></div>
                  保存中...
                </span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import Header from '@/components/Header.vue'
import { updateUserProfile } from '@/api/auth'

export default {
  name: 'ProfileEdit',
  components: {
    Header
  },
  setup() {
    const router = useRouter()
    const store = useStore()

    const loading = ref(false)
    const showCurrentPassword = ref(false)
    const showNewPassword = ref(false)
    const errorMessage = ref('')

    const currentUser = computed(() => store.getters.currentUser)

    const formData = reactive({
      nickname: '',
      bio: '',
      email: '',
      socialLink: '',
      gender: '',
      birthday: '',
      location: '',
      currentPassword: '',
      newPassword: ''
    })

    const errors = reactive({
      nickname: '',
      bio: '',
      email: '',
      socialLink: '',
      gender: '',
      birthday: '',
      location: '',
      currentPassword: '',
      newPassword: ''
    })

    // Today's date for birthday validation
    const todayDate = computed(() => {
      const today = new Date()
      return today.toISOString().split('T')[0]
    })

    // Bio character count
    const bioLength = computed(() => {
      return formData.bio ? formData.bio.length : 0
    })

    // Validation functions
    const validateNickname = () => {
      errors.nickname = ''
      const val = formData.nickname || ''
      if (!val.trim()) {
        errors.nickname = '请输入昵称'
        return false
      }
      if (val.length < 1 || val.length > 50) {
        errors.nickname = '昵称长度必须在1到50个字符之间'
        return false
      }
      return true
    }

    const validateBio = () => {
      errors.bio = ''
      const val = formData.bio || ''
      if (val.length > 200) {
        errors.bio = '个人简介长度不能超过200个字符'
        return false
      }
      return true
    }

    const validateEmail = () => {
      errors.email = ''
      const val = formData.email || ''
      if (val && val.trim()) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(val)) {
          errors.email = '请输入有效的邮箱地址'
          return false
        }
      }
      return true
    }

    const validateSocialLink = () => {
      errors.socialLink = ''
      const val = formData.socialLink || ''
      if (val && val.trim()) {
        if (val.length > 255) {
          errors.socialLink = '社交链接长度不能超过255个字符'
          return false
        }
        if (!/^https?:\/\/.*/.test(val)) {
          errors.socialLink = '社交链接必须以 http:// 或 https:// 开头'
          return false
        }
      }
      return true
    }

    const validateGender = () => {
      errors.gender = ''
      const val = formData.gender || ''
      if (val.length > 20) {
        errors.gender = '性别长度不能超过20个字符'
        return false
      }
      return true
    }

    const validateBirthday = () => {
      errors.birthday = ''
      const val = formData.birthday || ''
      if (val && val.trim()) {
        const birthday = new Date(val)
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        if (birthday > today) {
          errors.birthday = '生日不能是未来日期'
          return false
        }
      }
      return true
    }

    const validateLocation = () => {
      errors.location = ''
      const val = formData.location || ''
      if (val.length > 100) {
        errors.location = '所在地长度不能超过100个字符'
        return false
      }
      return true
    }

    const validatePasswordChange = () => {
      errors.currentPassword = ''
      errors.newPassword = ''

      const currentPwd = formData.currentPassword || ''
      const newPwd = formData.newPassword || ''

      // If either password field is filled, both must be filled
      if (currentPwd || newPwd) {
        if (!currentPwd) {
          errors.currentPassword = '请输入当前密码'
          return false
        }
        if (currentPwd.length < 6) {
          errors.currentPassword = '当前密码长度至少为6个字符'
          return false
        }
        if (!newPwd) {
          errors.newPassword = '请输入新密码'
          return false
        }
        if (newPwd.length < 6) {
          errors.newPassword = '新密码长度至少为6个字符'
          return false
        }
      }
      return true
    }

    const validateForm = () => {
      const isNicknameValid = validateNickname()
      const isBioValid = validateBio()
      const isEmailValid = validateEmail()
      const isSocialLinkValid = validateSocialLink()
      const isGenderValid = validateGender()
      const isBirthdayValid = validateBirthday()
      const isLocationValid = validateLocation()
      const isPasswordValid = validatePasswordChange()

      return (
        isNicknameValid &&
        isBioValid &&
        isEmailValid &&
        isSocialLinkValid &&
        isGenderValid &&
        isBirthdayValid &&
        isLocationValid &&
        isPasswordValid
      )
    }

    // Load current user data
    const loadUserData = () => {
      if (currentUser.value) {
        formData.nickname = currentUser.value.nickname || currentUser.value.username || ''
        formData.bio = currentUser.value.bio || ''
        formData.email = currentUser.value.email || ''
        formData.socialLink = currentUser.value.socialLink || ''
        formData.gender = currentUser.value.gender || ''
        formData.birthday = currentUser.value.birthday || ''
        formData.location = currentUser.value.location || ''
      }
    }

    // Handle form submission
    const handleSubmit = async () => {
      if (!validateForm()) return

      loading.value = true
      errorMessage.value = ''

      try {
        // Build update data - only include fields that should be sent
        const updateData = {}

        // Always include nickname if changed
        if (formData.nickname && formData.nickname.trim()) {
          updateData.nickname = formData.nickname.trim()
        }

        // Include bio (can be empty string to clear)
        if (formData.bio !== undefined) {
          updateData.bio = formData.bio
        }

        // Include email if changed
        if (formData.email && formData.email.trim() && formData.email !== currentUser.value.email) {
          updateData.email = formData.email.trim()
        }

        // Include social link (can be empty string to clear)
        if (formData.socialLink !== undefined) {
          updateData.socialLink = formData.socialLink
        }

        // Include gender (can be empty string to clear)
        if (formData.gender !== undefined) {
          updateData.gender = formData.gender
        }

        // Include birthday (can be empty string to clear)
        if (formData.birthday !== undefined) {
          updateData.birthday = formData.birthday
        }

        // Include location (can be empty string to clear)
        if (formData.location !== undefined) {
          updateData.location = formData.location
        }

        // Include password change if both fields are filled
        if (formData.currentPassword && formData.newPassword) {
          updateData.currentPassword = formData.currentPassword
          updateData.newPassword = formData.newPassword
        }

        // Call API to update profile
        const updatedUser = await updateUserProfile(updateData)

        // Update Vuex store
        store.commit('SET_USER', updatedUser)

        // Show success message and redirect
        alert('个人资料更新成功！')
        router.push('/profile')
      } catch (err) {
        console.error('更新失败:', err)
        errorMessage.value = err?.response?.data?.message || err?.message || '更新失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      loadUserData()
    })

    return {
      loading,
      showCurrentPassword,
      showNewPassword,
      errorMessage,
      formData,
      errors,
      todayDate,
      bioLength,
      validateNickname,
      validateBio,
      validateEmail,
      validateSocialLink,
      validateGender,
      validateBirthday,
      validateLocation,
      validatePasswordChange,
      handleSubmit
    }
  }
}
</script>
