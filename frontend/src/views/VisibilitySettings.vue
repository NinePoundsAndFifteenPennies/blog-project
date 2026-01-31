<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto">
          <!-- Header -->
          <div class="mb-6">
            <div class="flex items-center justify-between">
              <div>
                <h1 class="text-3xl font-bold text-gray-900">隐私设置</h1>
                <p class="text-gray-600 mt-1">设置谁可以查看您的关注信息</p>
              </div>
              <router-link to="/profile" class="btn-ghost">
                <svg class="w-5 h-5 mr-1 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                返回
              </router-link>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="card p-16 text-center bg-white shadow-md">
            <div class="spinner w-16 h-16 mx-auto"></div>
            <p class="text-gray-600 mt-4">加载中...</p>
          </div>

          <!-- Settings Form -->
          <div v-else class="card p-8 bg-white shadow-md">
            <!-- Tabs -->
            <div class="flex flex-wrap border-b border-gray-200 mb-8">
              <button
                v-for="type in types"
                :key="type.key"
                @click="currentType = type.key"
                :class="[
                  'px-6 py-3 font-medium transition-all duration-200 relative',
                  currentType === type.key 
                    ? 'text-primary-600' 
                    : 'text-gray-600 hover:text-gray-900'
                ]"
              >
                {{ type.label }}
                <div
                  v-if="currentType === type.key"
                  class="absolute bottom-0 left-0 w-full h-0.5 bg-primary-600"
                ></div>
              </button>
            </div>

            <!-- Current Type Settings -->
            <div v-if="settings[currentType]" class="space-y-6">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">
                {{ currentTypeName }} 可见性设置
              </h3>

              <!-- Public Option -->
              <label class="flex items-start space-x-3 p-4 rounded-lg border border-gray-200 hover:border-primary-300 transition-colors cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="settings[currentType].isPublic"
                  class="mt-1 w-5 h-5 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <div>
                  <span class="font-medium text-gray-900">公开</span>
                  <p class="text-sm text-gray-500 mt-1">所有人都可以查看</p>
                </div>
              </label>

              <!-- Friends Only -->
              <label class="flex items-start space-x-3 p-4 rounded-lg border border-gray-200 hover:border-primary-300 transition-colors cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="settings[currentType].visibleToFriends"
                  class="mt-1 w-5 h-5 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <div>
                  <span class="font-medium text-gray-900">仅朋友可见</span>
                  <p class="text-sm text-gray-500 mt-1">只有互相关注的用户可以查看</p>
                </div>
              </label>

              <!-- Following Only -->
              <label class="flex items-start space-x-3 p-4 rounded-lg border border-gray-200 hover:border-primary-300 transition-colors cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="settings[currentType].visibleToFollowing"
                  class="mt-1 w-5 h-5 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <div>
                  <span class="font-medium text-gray-900">仅我关注的人可见</span>
                  <p class="text-sm text-gray-500 mt-1">只有您关注的用户可以查看</p>
                </div>
              </label>

              <!-- Allowed Nicknames -->
              <div class="p-4 rounded-lg border border-gray-200">
                <label class="block font-medium text-gray-900 mb-2">
                  允许查看的用户
                </label>
                <p class="text-sm text-gray-500 mb-3">输入用户昵称，按回车添加（白名单）</p>
                <div class="flex flex-wrap gap-2 mb-3">
                  <span 
                    v-for="(nickname, index) in settings[currentType].allowedNicknames"
                    :key="'allowed-' + index"
                    class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800"
                  >
                    {{ nickname }}
                    <button 
                      @click="removeAllowedNickname(index)"
                      class="ml-2 text-green-600 hover:text-green-800"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </span>
                </div>
                <input
                  v-model="newAllowedNickname"
                  @keydown.enter.prevent="addAllowedNickname"
                  type="text"
                  placeholder="输入昵称后按回车添加"
                  class="input-field"
                />
              </div>

              <!-- Blocked Nicknames -->
              <div class="p-4 rounded-lg border border-gray-200">
                <label class="block font-medium text-gray-900 mb-2">
                  禁止查看的用户
                </label>
                <p class="text-sm text-gray-500 mb-3">输入用户昵称，按回车添加（黑名单，优先级最高）</p>
                <div class="flex flex-wrap gap-2 mb-3">
                  <span 
                    v-for="(nickname, index) in settings[currentType].blockedNicknames"
                    :key="'blocked-' + index"
                    class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-red-100 text-red-800"
                  >
                    {{ nickname }}
                    <button 
                      @click="removeBlockedNickname(index)"
                      class="ml-2 text-red-600 hover:text-red-800"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </span>
                </div>
                <input
                  v-model="newBlockedNickname"
                  @keydown.enter.prevent="addBlockedNickname"
                  type="text"
                  placeholder="输入昵称后按回车添加"
                  class="input-field"
                />
              </div>
            </div>

            <!-- Error Message -->
            <div v-if="errorMessage" class="mt-6 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p class="text-sm text-red-600 flex items-center">
                <svg class="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ errorMessage }}
              </p>
            </div>

            <!-- Success Message -->
            <div v-if="successMessage" class="mt-6 p-3 bg-green-50 border border-green-200 rounded-lg">
              <p class="text-sm text-green-600 flex items-center">
                <svg class="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                {{ successMessage }}
              </p>
            </div>

            <!-- Save Button -->
            <button
              @click="saveSettings"
              :disabled="saving"
              class="mt-8 w-full btn-primary flex items-center justify-center text-lg py-4"
              :class="{ 'opacity-70 cursor-not-allowed': saving }"
            >
              <span v-if="!saving" class="flex items-center space-x-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <span>保存设置</span>
              </span>
              <span v-else class="flex items-center">
                <div class="spinner w-5 h-5 mr-2"></div>
                保存中...
              </span>
            </button>
          </div>

          <!-- Help Text -->
          <div class="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <h4 class="font-medium text-blue-900 mb-2">可见性规则说明</h4>
            <ul class="text-sm text-blue-800 space-y-1">
              <li>• <strong>黑名单</strong> 优先级最高，即使其他设置允许也会被拒绝</li>
              <li>• <strong>公开</strong> 设置后所有人可见（黑名单除外）</li>
              <li>• <strong>白名单</strong> 中的用户即使未公开也可查看</li>
              <li>• <strong>仅朋友</strong> 表示互相关注的用户可见</li>
              <li>• <strong>仅我关注的人</strong> 表示您关注的用户可见</li>
              <li>• 您始终可以查看自己的信息</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import Header from '@/components/Header.vue'
import { getVisibilitySettings, updateVisibilitySettings } from '@/api/follow'

export default {
  name: 'VisibilitySettings',
  components: { Header },
  setup() {
    const loading = ref(true)
    const saving = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')
    const currentType = ref('following')
    const newAllowedNickname = ref('')
    const newBlockedNickname = ref('')

    const types = [
      { key: 'following', label: '关注列表' },
      { key: 'followers', label: '粉丝列表' },
      { key: 'friends', label: '朋友列表' },
      { key: 'stats', label: '统计数据' }
    ]

    const settings = reactive({
      following: {
        isPublic: true,
        visibleToFriends: false,
        visibleToFollowing: false,
        allowedNicknames: [],
        blockedNicknames: []
      },
      followers: {
        isPublic: true,
        visibleToFriends: false,
        visibleToFollowing: false,
        allowedNicknames: [],
        blockedNicknames: []
      },
      friends: {
        isPublic: true,
        visibleToFriends: false,
        visibleToFollowing: false,
        allowedNicknames: [],
        blockedNicknames: []
      },
      stats: {
        isPublic: true,
        visibleToFriends: false,
        visibleToFollowing: false,
        allowedNicknames: [],
        blockedNicknames: []
      }
    })

    const currentTypeName = computed(() => {
      const names = {
        following: '关注列表',
        followers: '粉丝列表',
        friends: '朋友列表',
        stats: '统计数据'
      }
      return names[currentType.value] || ''
    })

    const loadSettings = async () => {
      loading.value = true
      try {
        const data = await getVisibilitySettings()
        
        // 合并服务器返回的设置
        Object.keys(settings).forEach(key => {
          if (data[key]) {
            settings[key] = {
              ...settings[key],
              ...data[key],
              allowedNicknames: data[key].allowedNicknames || [],
              blockedNicknames: data[key].blockedNicknames || []
            }
          }
        })
      } catch (error) {
        console.error('加载设置失败:', error)
        errorMessage.value = '加载设置失败，请刷新页面重试'
      } finally {
        loading.value = false
      }
    }

    const saveSettings = async () => {
      saving.value = true
      errorMessage.value = ''
      successMessage.value = ''

      try {
        await updateVisibilitySettings(settings)
        successMessage.value = '设置保存成功'
        
        // 3秒后清除成功消息
        setTimeout(() => {
          successMessage.value = ''
        }, 3000)
      } catch (error) {
        console.error('保存设置失败:', error)
        errorMessage.value = error.response?.data?.message || '保存失败，请稍后重试'
      } finally {
        saving.value = false
      }
    }

    const addAllowedNickname = () => {
      const nickname = newAllowedNickname.value.trim()
      if (nickname && !settings[currentType.value].allowedNicknames.includes(nickname)) {
        settings[currentType.value].allowedNicknames.push(nickname)
        newAllowedNickname.value = ''
      }
    }

    const removeAllowedNickname = (index) => {
      settings[currentType.value].allowedNicknames.splice(index, 1)
    }

    const addBlockedNickname = () => {
      const nickname = newBlockedNickname.value.trim()
      if (nickname && !settings[currentType.value].blockedNicknames.includes(nickname)) {
        settings[currentType.value].blockedNicknames.push(nickname)
        newBlockedNickname.value = ''
      }
    }

    const removeBlockedNickname = (index) => {
      settings[currentType.value].blockedNicknames.splice(index, 1)
    }

    onMounted(() => {
      loadSettings()
    })

    return {
      loading,
      saving,
      errorMessage,
      successMessage,
      currentType,
      types,
      settings,
      currentTypeName,
      newAllowedNickname,
      newBlockedNickname,
      saveSettings,
      addAllowedNickname,
      removeAllowedNickname,
      addBlockedNickname,
      removeBlockedNickname
    }
  }
}
</script>
