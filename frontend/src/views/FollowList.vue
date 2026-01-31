<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Header -->
          <div class="card p-6 mb-6 backdrop-blur-sm bg-white/90 animate-fade-in">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <button 
                  @click="goBack" 
                  class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                  </svg>
                </button>
                <div>
                  <h1 class="text-2xl font-bold text-gray-900">{{ pageTitle }}</h1>
                  <p class="text-gray-500 text-sm">{{ username }} 的{{ listTypeName }}</p>
                </div>
              </div>
            </div>

            <!-- Tabs -->
            <div class="flex border-b border-gray-200 mt-6">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                @click="switchTab(tab.key)"
                :class="[
                  'px-6 py-3 font-medium transition-all duration-200 relative',
                  activeTab === tab.key 
                    ? 'text-primary-600' 
                    : 'text-gray-600 hover:text-gray-900'
                ]"
              >
                {{ tab.label }}
                <div
                  v-if="activeTab === tab.key"
                  class="absolute bottom-0 left-0 w-full h-0.5 bg-primary-600"
                ></div>
              </button>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="card p-16 text-center backdrop-blur-sm bg-white/90">
            <div class="spinner w-16 h-16 mx-auto"></div>
            <p class="text-gray-600 mt-4">加载中...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="users.length === 0" class="card p-16 text-center backdrop-blur-sm bg-white/90">
            <div class="inline-flex items-center justify-center w-24 h-24 rounded-full bg-gray-100 mb-6">
              <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-700 mb-2">{{ emptyMessage }}</h3>
            <p class="text-gray-500">{{ emptyDescription }}</p>
          </div>

          <!-- User List -->
          <div v-else class="space-y-4 animate-slide-up">
            <div
              v-for="user in users"
              :key="user.id"
              class="card p-4 hover:shadow-glow transition-all duration-300 backdrop-blur-sm bg-white/90"
            >
              <div class="flex items-center space-x-4">
                <!-- Avatar -->
                <router-link :to="`/user/${user.username}`" class="shrink-0">
                  <div 
                    class="w-14 h-14 rounded-full flex items-center justify-center text-white text-xl font-bold shadow-sm overflow-hidden bg-primary-600"
                  >
                    <img 
                      v-if="user.avatarUrl" 
                      :src="getFullAvatarUrl(user.avatarUrl)" 
                      :alt="user.username"
                      class="w-full h-full object-cover"
                      @error="(e) => e.target.style.display = 'none'"
                    />
                    <span v-else>{{ getUserInitial(user) }}</span>
                  </div>
                </router-link>

                <!-- User Info -->
                <div class="flex-1 min-w-0">
                  <router-link 
                    :to="`/user/${user.username}`"
                    class="block"
                  >
                    <h3 class="text-lg font-semibold text-gray-900 hover:text-primary-600 transition-colors truncate">
                      {{ user.nickname || user.username }}
                    </h3>
                    <p class="text-gray-500 text-sm">@{{ user.username }}</p>
                  </router-link>
                  <p v-if="user.bio" class="text-gray-600 text-sm mt-1 line-clamp-2">
                    {{ user.bio }}
                  </p>
                </div>

                <!-- Friend Badge & Follow Button -->
                <div class="flex items-center space-x-3 shrink-0">
                  <span 
                    v-if="user.friend" 
                    class="px-2 py-1 text-xs font-medium text-green-700 bg-green-100 rounded-full"
                  >
                    互相关注
                  </span>
                  <FollowButton
                    v-if="!isCurrentUser(user.id)"
                    :userId="user.id"
                    :initialFollowing="user.isFollowing === true"
                    :initialFriend="user.friend === true"
                    @follow-change="handleFollowChange(user, $event)"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-8">
            <Pagination
              :current-page="currentPage"
              :total-pages="totalPages"
              @page-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import Header from '@/components/Header.vue'
import Pagination from '@/components/Pagination.vue'
import FollowButton from '@/components/FollowButton.vue'
import { getFollowingList, getFollowersList, getFriendsList } from '@/api/follow'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'FollowList',
  components: { Header, Pagination, FollowButton },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    
    const loading = ref(true)
    const users = ref([])
    const currentPage = ref(1)
    const totalPages = ref(1)
    const pageSize = 20

    const currentUser = computed(() => store.getters.currentUser)
    const userId = computed(() => Number(route.params.userId))
    const username = computed(() => route.query.username || '用户')
    const activeTab = computed(() => route.params.type || 'following')

    const tabs = [
      { key: 'following', label: '关注' },
      { key: 'followers', label: '粉丝' },
      { key: 'friends', label: '朋友' }
    ]

    const listTypeName = computed(() => {
      const names = {
        following: '关注列表',
        followers: '粉丝列表',
        friends: '朋友列表'
      }
      return names[activeTab.value] || '关注列表'
    })

    const pageTitle = computed(() => listTypeName.value)

    const emptyMessage = computed(() => {
      const messages = {
        following: '暂无关注',
        followers: '暂无粉丝',
        friends: '暂无朋友'
      }
      return messages[activeTab.value] || '暂无数据'
    })

    const emptyDescription = computed(() => {
      const descriptions = {
        following: '该用户还没有关注任何人',
        followers: '该用户还没有粉丝',
        friends: '该用户还没有互相关注的朋友'
      }
      return descriptions[activeTab.value] || ''
    })

    const getUserInitial = (user) => {
      const name = user.nickname || user.username || 'U'
      return name.charAt(0).toUpperCase()
    }

    const isCurrentUser = (id) => {
      return currentUser.value && currentUser.value.id === id
    }

    const loadUsers = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize
        }

        let response
        switch (activeTab.value) {
          case 'followers':
            response = await getFollowersList(userId.value, params)
            break
          case 'friends':
            response = await getFriendsList(userId.value, params)
            break
          default:
            response = await getFollowingList(userId.value, params)
        }

        users.value = response.content || []
        totalPages.value = response.totalPages || 1
      } catch (error) {
        console.error('加载列表失败:', error)
        users.value = []
      } finally {
        loading.value = false
      }
    }

    const switchTab = (tab) => {
      router.push({
        name: 'FollowList',
        params: { userId: userId.value, type: tab },
        query: { username: username.value }
      })
    }

    const handlePageChange = (page) => {
      currentPage.value = page
      loadUsers()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    const handleFollowChange = (user, event) => {
      user.friend = event.friend
    }

    const goBack = () => {
      router.back()
    }

    onMounted(() => {
      loadUsers()
    })

    watch([userId, activeTab], () => {
      currentPage.value = 1
      loadUsers()
    })

    return {
      loading,
      users,
      currentPage,
      totalPages,
      username,
      activeTab,
      tabs,
      listTypeName,
      pageTitle,
      emptyMessage,
      emptyDescription,
      getUserInitial,
      getFullAvatarUrl,
      isCurrentUser,
      switchTab,
      handlePageChange,
      handleFollowChange,
      goBack
    }
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
