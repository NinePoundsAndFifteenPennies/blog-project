<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Page Header -->
          <div class="card p-6 mb-6 backdrop-blur-sm bg-white/90">
            <div class="flex items-center justify-center">
              <h1 class="text-2xl font-bold text-gray-900">我的私信</h1>
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
                <!-- Load More Button -->
                <div v-if="hasMoreMessages" class="text-center mb-4">
                  <button
                    @click="loadMoreMessages"
                    :disabled="loadingMore"
                    class="px-4 py-2 text-sm text-primary-600 hover:bg-primary-50 rounded-lg transition-colors disabled:text-gray-400"
                  >
                    {{ loadingMore ? '加载中...' : '加载更多消息' }}
                  </button>
                </div>

                <div
                  v-for="message in messages"
                  :key="message.id"
                  :class="[
                    'flex flex-col',
                    message.sentByMe ? 'items-end' : 'items-start'
                  ]"
                >
                  <!-- Time above message -->
                  <div class="text-xs mb-1 text-gray-400">
                    {{ formatTime(message.createdAt) }}
                  </div>
                  
                  <!-- Message bubble -->
                  <div
                    :class="[
                      'max-w-[70%] rounded-lg px-4 py-2',
                      message.sentByMe 
                        ? 'bg-primary-600' 
                        : 'bg-gray-100'
                    ]"
                  >
                    <div 
                      :class="[
                        'markdown-body text-sm',
                        message.sentByMe ? 'text-white' : 'text-gray-900'
                      ]" 
                      v-html="renderMarkdown(message.content)"
                    ></div>
                  </div>
                  
                  <!-- Read status below message -->
                  <div 
                    v-if="message.sentByMe"
                    class="text-xs mt-1 text-gray-400"
                  >
                    <span v-if="message.read" class="text-green-500">已读</span>
                    <span v-else>未读</span>
                  </div>
                </div>
              </template>
            </div>

            <!-- Message Input -->
            <div class="p-4 border-t border-gray-200">
              <div v-if="!canSend && !isFriend" class="text-center text-amber-600 text-sm mb-4 p-3 bg-amber-50 rounded-lg">
                在对方回复前，您无法发送第二条消息
              </div>
              
              <!-- Toolbar -->
              <div class="mb-2 flex flex-wrap gap-1 pb-2 border-b border-gray-100">
                <EmojiPicker @select="insertEmoji" title="表情" />
                
                <div class="w-px h-6 bg-gray-300 mx-1"></div>
                
                <!-- Image button (placeholder) -->
                <button type="button" class="toolbar-btn" title="图片 (即将推出)" disabled>
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </button>
                
                <!-- Attachment button (placeholder) -->
                <button type="button" class="toolbar-btn" title="附件 (即将推出)" disabled>
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                  </svg>
                </button>
              </div>
              
              <div class="flex space-x-3">
                <textarea
                  ref="messageTextarea"
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
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { marked } from 'marked'
import Header from '@/components/Header.vue'
import EmojiPicker from '@/components/EmojiPicker.vue'
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
  components: { Header, EmojiPicker },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const loading = ref(true)
    const loadingMessages = ref(false)
    const loadingMore = ref(false)
    const sending = ref(false)
    const conversations = ref([])
    const messages = ref([])
    const newMessage = ref('')
    const unreadCount = ref(0)
    const messagesContainer = ref(null)
    const messageTextarea = ref(null)
    
    // Pagination for messages
    const currentPage = ref(0)
    const totalPages = ref(1)
    const PAGE_SIZE = 20
    const hasMoreMessages = computed(() => currentPage.value < totalPages.value - 1)
    
    // Selected partner info
    const selectedPartnerId = ref(null)
    const partnerUsername = ref('')
    const partnerName = ref('')
    const partnerAvatarUrl = ref('')
    const partnerAvatarError = ref(false)
    const isFriend = ref(false)
    const canSend = ref(true)
    
    // Polling for real-time updates
    let pollingInterval = null
    const POLLING_INTERVAL = 5000 // 5 seconds

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

    const loadMessages = async (append = false) => {
      if (!selectedPartnerId.value) return
      
      if (append) {
        loadingMore.value = true
      } else {
        loadingMessages.value = true
        currentPage.value = 0
      }
      
      try {
        const res = await getConversation(selectedPartnerId.value, { 
          page: currentPage.value, 
          size: PAGE_SIZE 
        })
        
        // Messages come in DESC order (newest first), reverse for display (oldest at top)
        const fetchedMessages = (res.content || []).slice().reverse()
        totalPages.value = res.totalPages || 1
        
        if (append) {
          // Prepend older messages at the beginning (they're already reversed)
          messages.value = [...fetchedMessages, ...messages.value]
        } else {
          messages.value = fetchedMessages
        }
        
        // Mark as read
        await markAsRead(selectedPartnerId.value)
        
        // Check if can send message
        const canSendRes = await checkCanSend(selectedPartnerId.value)
        canSend.value = canSendRes.canSend

        // Scroll to bottom only for initial load
        if (!append) {
          await nextTick()
          scrollToBottom()
          // Start polling only on initial load, not on load more
          startPolling()
        }
      } catch (error) {
        console.error('加载消息失败:', error)
      } finally {
        loadingMessages.value = false
        loadingMore.value = false
      }
    }
    
    const loadMoreMessages = async () => {
      if (loadingMore.value || !hasMoreMessages.value) return
      currentPage.value++
      await loadMessages(true)
    }
    
    // Poll for new messages (simple real-time simulation)
    const POLL_SIZE = 50 // Larger size to ensure we don't miss messages
    
    const pollMessages = async () => {
      if (!selectedPartnerId.value) return
      
      try {
        const res = await getConversation(selectedPartnerId.value, { page: 0, size: POLL_SIZE })
        // Messages come in DESC order (newest first)
        const polledMessages = (res.content || []).slice().reverse()
        
        if (polledMessages.length === 0 || messages.value.length === 0) return
        
        // Get the ID of our newest message (messages are sorted by ID ascending, so last one is newest)
        const newestExistingId = messages.value[messages.value.length - 1].id
        
        // Only add messages that are NEWER than our newest message (higher ID)
        // This prevents polling from adding older messages that should come from "load more"
        const newMsgs = polledMessages.filter(m => m.id > newestExistingId)
        
        if (newMsgs.length > 0) {
          // Append new messages at the end (they're newer)
          messages.value = [...messages.value, ...newMsgs]
          
          await nextTick()
          scrollToBottom()
          
          // Mark as read
          await markAsRead(selectedPartnerId.value)
        }
        
        // Update read status of existing messages
        const polledMessagesMap = new Map(polledMessages.map(m => [m.id, m]))
        messages.value = messages.value.map(msg => {
          const updated = polledMessagesMap.get(msg.id)
          if (updated) {
            return { ...msg, read: updated.read }
          }
          return msg
        })
      } catch (error) {
        console.error('轮询消息失败:', error)
      }
    }
    
    const startPolling = () => {
      stopPolling()
      pollingInterval = setInterval(pollMessages, POLLING_INTERVAL)
    }
    
    const stopPolling = () => {
      if (pollingInterval) {
        clearInterval(pollingInterval)
        pollingInterval = null
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
      
      // Save to sessionStorage for persistence
      saveConversationState()
      
      loadMessages()
    }

    const closeConversation = () => {
      stopPolling()
      selectedPartnerId.value = null
      partnerUsername.value = ''
      partnerName.value = ''
      partnerAvatarUrl.value = ''
      messages.value = []
      newMessage.value = ''
      currentPage.value = 0
      
      // Clear saved state
      clearConversationState()
      
      // Refresh conversations list
      loadConversations()
    }
    
    // Session storage for conversation persistence
    const STORAGE_KEY = 'messages_conversation_state'
    
    const saveConversationState = () => {
      const state = {
        partnerId: selectedPartnerId.value,
        username: partnerUsername.value,
        name: partnerName.value,
        avatarUrl: partnerAvatarUrl.value,
        friend: isFriend.value
      }
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state))
    }
    
    const clearConversationState = () => {
      sessionStorage.removeItem(STORAGE_KEY)
    }
    
    const restoreConversationState = () => {
      try {
        const saved = sessionStorage.getItem(STORAGE_KEY)
        if (saved) {
          const state = JSON.parse(saved)
          if (state.partnerId) {
            selectedPartnerId.value = state.partnerId
            partnerUsername.value = state.username || ''
            partnerName.value = state.name || ''
            partnerAvatarUrl.value = state.avatarUrl || ''
            isFriend.value = state.friend || false
            return true
          }
        }
      } catch (e) {
        console.error('Failed to restore conversation state:', e)
      }
      return false
    }
    
    // Emoji insertion
    const insertEmoji = (emoji) => {
      const textarea = messageTextarea.value
      if (!textarea) {
        newMessage.value += emoji
        return
      }

      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const beforeText = newMessage.value.substring(0, start)
      const afterText = newMessage.value.substring(end)

      newMessage.value = beforeText + emoji + afterText

      // Set cursor position after emoji
      nextTick(() => {
        textarea.focus()
        const newPosition = start + emoji.length
        textarea.setSelectionRange(newPosition, newPosition)
      })
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
        
        // Save state
        saveConversationState()
        
        await loadMessages()
        
        // Clear query params but keep the page
        router.replace({ path: '/messages' })
      }
    }, { immediate: true })

    onMounted(async () => {
      await loadConversations()
      
      // Restore conversation state on page refresh
      if (!route.query.userId && restoreConversationState()) {
        try {
          await loadMessages()
        } catch (error) {
          console.error('Failed to restore conversation:', error)
          // Clear invalid state and show conversation list
          clearConversationState()
          selectedPartnerId.value = null
          partnerUsername.value = ''
          partnerName.value = ''
          partnerAvatarUrl.value = ''
        }
      }
    })
    
    onBeforeUnmount(() => {
      stopPolling()
    })

    return {
      loading,
      loadingMessages,
      loadingMore,
      sending,
      conversations,
      messages,
      newMessage,
      unreadCount,
      messagesContainer,
      messageTextarea,
      selectedPartnerId,
      partnerUsername,
      partnerName,
      partnerAvatarUrl,
      partnerAvatarError,
      partnerInitial,
      isFriend,
      canSend,
      hasMoreMessages,
      currentUser,
      getAvatarUrl,
      formatTime,
      renderMarkdown,
      openConversation,
      closeConversation,
      sendNewMessage,
      loadMoreMessages,
      insertEmoji
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

/* Ensure code in sent messages (white text) has proper styling */
.bg-primary-600 .markdown-body :deep(code) {
  background-color: rgba(255, 255, 255, 0.2);
}

.toolbar-btn {
  @apply px-2 py-1.5 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100
  transition-colors duration-150 flex items-center justify-center min-w-[28px];
}

.toolbar-btn:disabled {
  @apply text-gray-400 cursor-not-allowed hover:bg-transparent hover:text-gray-400;
}

.toolbar-btn:active:not(:disabled) {
  @apply bg-gray-200;
}
</style>
