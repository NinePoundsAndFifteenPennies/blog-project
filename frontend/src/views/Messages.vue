<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Page Header -->
          <div class="card p-6 mb-6 backdrop-blur-sm bg-white/90">
            <div class="flex items-center justify-between">
              <h1 class="text-2xl font-bold text-gray-900">私信</h1>
              <div class="flex items-center space-x-2 text-sm text-gray-500">
                <span v-if="unreadCount > 0" class="px-2 py-1 bg-red-100 text-red-600 rounded-full font-medium">
                  {{ unreadCount }} 条未读
                </span>
              </div>
            </div>
          </div>

          <!-- Conversation View (when a partner is selected) -->
          <div v-if="selectedPartnerId" class="card backdrop-blur-sm bg-white/90">
            <!-- Conversation Header -->
            <div class="p-4 border-b border-gray-200 flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <button @click="closeConversation" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
                  <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                  </svg>
                </button>
                <div
                  class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600"
                >
                  <img
                    v-if="partnerAvatarUrl && !partnerAvatarError"
                    :src="partnerAvatarUrl"
                    :alt="partnerName"
                    class="w-full h-full object-cover"
                    @error="partnerAvatarError = true"
                  />
                  <span v-else>{{ partnerInitial }}</span>
                </div>
                <div>
                  <div class="font-semibold text-gray-900">{{ partnerName }}</div>
                  <div class="text-xs text-gray-500">@{{ partnerUsername }}</div>
                </div>
              </div>
              <div v-if="isFriend" class="text-xs px-2 py-1 bg-green-100 text-green-700 rounded-full">
                朋友
              </div>
            </div>

            <!-- Messages List -->
            <div ref="messagesContainer" class="h-96 overflow-y-auto p-4 space-y-4">
              <div v-if="loadingMessages" class="flex justify-center py-8">
                <div class="spinner w-8 h-8"></div>
              </div>

              <div v-else-if="messages.length === 0" class="text-center text-gray-500 py-8">
                暂无消息，开始对话吧！
              </div>

              <template v-else>
                <div
                  v-for="message in messages"
                  :key="message.id"
                  :class="[
                    'flex',
                    message.sentByMe ? 'justify-end' : 'justify-start'
                  ]"
                >
                  <div
                    :class="[
                      'max-w-[70%] rounded-lg px-4 py-2',
                      message.sentByMe 
                        ? 'bg-primary-600 text-white' 
                        : 'bg-gray-100 text-gray-900'
                    ]"
                  >
                    <div class="markdown-body text-sm" v-html="renderMarkdown(message.content)"></div>
                    <div 
                      :class="[
                        'flex items-center justify-end space-x-2 mt-1 text-xs',
                        message.sentByMe ? 'text-primary-200' : 'text-gray-400'
                      ]"
                    >
                      <span>{{ formatTime(message.createdAt) }}</span>
                      <span v-if="message.sentByMe && message.read" class="text-green-300">已读</span>
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <!-- Message Input -->
            <div class="p-4 border-t border-gray-200">
              <div v-if="!canSend && !isFriend" class="text-center text-amber-600 text-sm mb-4 p-3 bg-amber-50 rounded-lg">
                在对方回复前，您无法发送第二条消息
              </div>
              <div class="flex space-x-3">
                <textarea
                  v-model="newMessage"
                  :disabled="!canSend && !isFriend"
                  class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none disabled:bg-gray-100"
                  rows="2"
                  placeholder="输入消息内容..."
                  @keydown.enter.meta="sendNewMessage"
                  @keydown.enter.ctrl="sendNewMessage"
                ></textarea>
                <button
                  @click="sendNewMessage"
                  :disabled="!newMessage.trim() || sending || (!canSend && !isFriend)"
                  class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed self-end"
                >
                  {{ sending ? '...' : '发送' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Conversations List (when no partner is selected) -->
          <div v-else>
            <div v-if="loading" class="card p-8 text-center backdrop-blur-sm bg-white/90">
              <div class="spinner w-12 h-12 mx-auto"></div>
              <p class="text-gray-600 mt-4">加载中...</p>
            </div>

            <div v-else-if="conversations.length === 0" class="card p-16 text-center backdrop-blur-sm bg-white/90">
              <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-primary-100 mb-6">
                <svg class="w-10 h-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h3 class="text-xl font-bold text-gray-700 mb-2">暂无私信</h3>
              <p class="text-gray-500">访问用户主页即可开始私信对话</p>
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="conv in conversations"
                :key="conv.partnerId"
                @click="openConversation(conv)"
                class="card p-4 backdrop-blur-sm bg-white/90 cursor-pointer hover:shadow-md transition-shadow"
              >
                <div class="flex items-center space-x-4">
                  <div
                    class="w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600 flex-shrink-0"
                  >
                    <img
                      v-if="conv.partnerAvatarUrl"
                      :src="getAvatarUrl(conv.partnerAvatarUrl)"
                      :alt="conv.partnerNickname || conv.partnerUsername"
                      class="w-full h-full object-cover"
                    />
                    <span v-else>{{ (conv.partnerNickname || conv.partnerUsername || 'U').charAt(0).toUpperCase() }}</span>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between mb-1">
                      <div class="flex items-center space-x-2">
                        <span class="font-semibold text-gray-900 truncate">
                          {{ conv.partnerNickname || conv.partnerUsername }}
                        </span>
                        <span v-if="conv.friend" class="text-xs px-1.5 py-0.5 bg-green-100 text-green-700 rounded">朋友</span>
                      </div>
                      <span class="text-xs text-gray-400">{{ formatTime(conv.lastMessageTime) }}</span>
                    </div>
                    <div class="flex items-center justify-between">
                      <p class="text-sm text-gray-600 truncate">
                        <span v-if="conv.lastMessageSentByMe" class="text-gray-400">我: </span>
                        {{ conv.lastMessageContent }}
                      </p>
                      <span 
                        v-if="conv.unreadCount > 0" 
                        class="ml-2 px-2 py-0.5 text-xs font-bold bg-red-500 text-white rounded-full flex-shrink-0"
                      >
                        {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { marked } from 'marked'
import Header from '@/components/Header.vue'
import { 
  getConversations, 
  getConversation, 
  sendMessage, 
  markAsRead, 
  getUnreadCount,
  canSendMessage as checkCanSend 
} from '@/api/messages'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Messages',
  components: { Header },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const loading = ref(true)
    const loadingMessages = ref(false)
    const sending = ref(false)
    const conversations = ref([])
    const messages = ref([])
    const newMessage = ref('')
    const unreadCount = ref(0)
    const messagesContainer = ref(null)
    
    // Selected partner info
    const selectedPartnerId = ref(null)
    const partnerUsername = ref('')
    const partnerName = ref('')
    const partnerAvatarUrl = ref('')
    const partnerAvatarError = ref(false)
    const isFriend = ref(false)
    const canSend = ref(true)

    const currentUser = computed(() => store.getters.currentUser)

    const partnerInitial = computed(() => 
      (partnerName.value || partnerUsername.value || 'U').charAt(0).toUpperCase()
    )

    const getAvatarUrl = (url) => getFullAvatarUrl(url)

    const formatTime = (timeStr) => {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMs / 3600000)
      const diffDays = Math.floor(diffMs / 86400000)

      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24) return `${diffHours}小时前`
      if (diffDays < 7) return `${diffDays}天前`
      
      return date.toLocaleDateString('zh-CN')
    }

    const renderMarkdown = (content) => {
      try {
        return marked(content || '')
      } catch {
        return content || ''
      }
    }

    const loadConversations = async () => {
      loading.value = true
      try {
        conversations.value = await getConversations()
        const countRes = await getUnreadCount()
        unreadCount.value = countRes.count || 0
      } catch (error) {
        console.error('加载会话列表失败:', error)
      } finally {
        loading.value = false
      }
    }

    const loadMessages = async () => {
      if (!selectedPartnerId.value) return
      
      loadingMessages.value = true
      try {
        const res = await getConversation(selectedPartnerId.value, { page: 0, size: 50 })
        messages.value = res.content || []
        
        // Mark as read
        await markAsRead(selectedPartnerId.value)
        
        // Check if can send message
        const canSendRes = await checkCanSend(selectedPartnerId.value)
        canSend.value = canSendRes.canSend

        // Scroll to bottom
        await nextTick()
        scrollToBottom()
      } catch (error) {
        console.error('加载消息失败:', error)
      } finally {
        loadingMessages.value = false
      }
    }

    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    const openConversation = (conv) => {
      selectedPartnerId.value = conv.partnerId
      partnerUsername.value = conv.partnerUsername
      partnerName.value = conv.partnerNickname || conv.partnerUsername
      partnerAvatarUrl.value = getFullAvatarUrl(conv.partnerAvatarUrl)
      partnerAvatarError.value = false
      isFriend.value = conv.friend
      
      loadMessages()
    }

    const closeConversation = () => {
      selectedPartnerId.value = null
      partnerUsername.value = ''
      partnerName.value = ''
      partnerAvatarUrl.value = ''
      messages.value = []
      newMessage.value = ''
      
      // Refresh conversations list
      loadConversations()
    }

    const sendNewMessage = async () => {
      if (!newMessage.value.trim() || sending.value) return
      if (!canSend.value && !isFriend.value) return

      sending.value = true
      try {
        const message = await sendMessage(selectedPartnerId.value, newMessage.value.trim())
        messages.value.push(message)
        newMessage.value = ''
        
        // Update canSend status
        const canSendRes = await checkCanSend(selectedPartnerId.value)
        canSend.value = canSendRes.canSend

        await nextTick()
        scrollToBottom()
      } catch (error) {
        console.error('发送消息失败:', error)
        if (error.response?.status === 400) {
          alert(error.response.data.message || '发送失败')
        } else {
          alert('发送消息失败，请稍后重试')
        }
      } finally {
        sending.value = false
      }
    }

    // Check for userId in route query params (for starting a new conversation)
    watch(() => route.query.userId, async (newUserId) => {
      if (newUserId) {
        // Open conversation with this user
        selectedPartnerId.value = parseInt(newUserId)
        partnerUsername.value = route.query.username || ''
        partnerName.value = route.query.nickname || route.query.username || ''
        partnerAvatarUrl.value = getFullAvatarUrl(route.query.avatar)
        partnerAvatarError.value = false
        isFriend.value = route.query.friend === 'true'
        
        await loadMessages()
        
        // Clear query params
        router.replace({ path: '/messages' })
      }
    }, { immediate: true })

    onMounted(() => {
      loadConversations()
    })

    return {
      loading,
      loadingMessages,
      sending,
      conversations,
      messages,
      newMessage,
      unreadCount,
      messagesContainer,
      selectedPartnerId,
      partnerUsername,
      partnerName,
      partnerAvatarUrl,
      partnerAvatarError,
      partnerInitial,
      isFriend,
      canSend,
      currentUser,
      getAvatarUrl,
      formatTime,
      renderMarkdown,
      openConversation,
      closeConversation,
      sendNewMessage
    }
  }
}
</script>

<style scoped>
.markdown-body {
  font-size: 0.875rem;
  line-height: 1.5;
}

.markdown-body :deep(p) {
  margin: 0;
}

.markdown-body :deep(code) {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-size: 0.8em;
}
</style>
