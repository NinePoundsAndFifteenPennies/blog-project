<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <!-- Page Header -->
          <div class="card p-6 mb-6 backdrop-blur-sm bg-white/90">
            <div class="flex items-center justify-between">
              <h1 class="text-2xl font-bold text-gray-900">我的通知</h1>
              <button
                v-if="unreadCount > 0"
                @click="handleMarkAllAsRead"
                :disabled="markingAllRead"
                class="px-4 py-2 text-sm text-primary-600 hover:bg-primary-50 rounded-lg transition-colors disabled:text-gray-400"
              >
                {{ markingAllRead ? '处理中...' : '全部标为已读' }}
              </button>
            </div>
          </div>

          <!-- Filter Tabs -->
          <div class="card mb-6 backdrop-blur-sm bg-white/90">
            <div class="flex border-b border-gray-200">
              <button
                v-for="tab in tabs"
                :key="tab.value"
                @click="currentFilter = tab.value"
                :class="[
                  'flex-1 py-4 px-4 text-center font-medium transition-colors duration-200',
                  currentFilter === tab.value
                    ? 'text-primary-600 border-b-2 border-primary-600'
                    : 'text-gray-500 hover:text-gray-700'
                ]"
              >
                <span class="flex items-center justify-center space-x-2">
                  <span v-html="tab.icon"></span>
                  <span>{{ tab.label }}</span>
                </span>
              </button>
            </div>
          </div>

          <!-- Notifications List -->
          <div v-if="loading" class="card p-8 text-center backdrop-blur-sm bg-white/90">
            <div class="spinner w-12 h-12 mx-auto"></div>
            <p class="text-gray-600 mt-4">加载中...</p>
          </div>

          <div v-else-if="notifications.length === 0" class="card p-16 text-center backdrop-blur-sm bg-white/90">
            <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 mb-6">
              <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-700 mb-2">暂无通知</h3>
            <p class="text-gray-500">{{ getEmptyMessage() }}</p>
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              @click="handleNotificationClick(notification)"
              :class="[
                'card p-4 backdrop-blur-sm cursor-pointer hover:shadow-md transition-all duration-200',
                notification.read ? 'bg-gray-50/90' : 'bg-white/90'
              ]"
            >
              <div class="flex items-start space-x-4">
                <!-- Type Icon -->
                <div
                  :class="[
                    'w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0',
                    getTypeStyle(notification.type).bgColor
                  ]"
                >
                  <span v-html="getTypeStyle(notification.type).icon"></span>
                </div>

                <!-- Avatar with Hover Card (for non-system notifications) -->
                <UserProfileHoverCard 
                  v-if="notification.actorUsername && !isSystemNotification(notification)"
                  :username="notification.actorUsername"
                  :user-data="getActorData(notification)"
                  @click.stop
                >
                  <div
                    class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600 flex-shrink-0 cursor-pointer"
                  >
                    <img
                      v-if="notification.actorAvatarUrl"
                      :src="getAvatarUrl(notification.actorAvatarUrl)"
                      :alt="notification.actorNickname || notification.actorUsername"
                      class="w-full h-full object-cover"
                    />
                    <span v-else>{{ getInitial(notification.actorNickname || notification.actorUsername) }}</span>
                  </div>
                </UserProfileHoverCard>
                
                <!-- System notification avatar (person icon) -->
                <div
                  v-else-if="isSystemNotification(notification)"
                  class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-indigo-400 flex-shrink-0"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                
                <!-- Fallback avatar -->
                <div
                  v-else
                  class="w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold shadow-sm ring-2 ring-white overflow-hidden bg-primary-600 flex-shrink-0"
                >
                  <span>{{ getInitial(notification.actorNickname || notification.actorUsername) }}</span>
                </div>

                <!-- Content -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1">
                    <p class="text-sm text-gray-900">
                      <span class="font-semibold">{{ getNotificationActorName(notification) }}</span>
                      <span class="text-gray-600"> {{ getNotificationText(notification) }}</span>
                    </p>
                    <div class="flex items-center space-x-2 flex-shrink-0 ml-4">
                      <!-- Quick Actions for comment-related notifications (on the right) -->
                      <div 
                        v-if="isCommentNotification(notification)" 
                        class="flex items-center space-x-2"
                        @click.stop
                      >
                        <!-- Like Button -->
                        <button 
                          @click="handleQuickLike(notification)"
                          :disabled="notification.liking"
                          class="p-1.5 text-gray-400 hover:text-red-500 transition-colors rounded-full hover:bg-red-50"
                          :class="{ 'text-red-500': notification.commentLiked }"
                          title="点赞"
                        >
                          <svg 
                            class="w-4 h-4" 
                            :fill="notification.commentLiked ? 'currentColor' : 'none'" 
                            stroke="currentColor" 
                            viewBox="0 0 24 24"
                          >
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                          </svg>
                        </button>

                        <!-- Reply Button -->
                        <button 
                          @click="handleQuickReply(notification)"
                          class="p-1.5 text-gray-400 hover:text-primary-600 transition-colors rounded-full hover:bg-primary-50"
                          title="回复"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                          </svg>
                        </button>

                        <!-- Delete Button (only for POST_COMMENTED - comments on your articles) -->
                        <button 
                          v-if="notification.type === 'POST_COMMENTED'"
                          @click="handleQuickDelete(notification)"
                          :disabled="notification.deleting"
                          class="p-1.5 text-gray-400 hover:text-red-600 transition-colors rounded-full hover:bg-red-50"
                          title="删除评论"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                      
                      <span class="text-xs text-gray-400">{{ formatTime(notification.createdAt) }}</span>
                    </div>
                  </div>
                  
                  <!-- Additional content preview -->
                  <p v-if="notification.content" class="text-sm text-gray-500 truncate">
                    "{{ notification.content }}"
                  </p>
                  <p v-if="notification.postTitle" class="text-sm text-gray-500 truncate">
                    📄 {{ notification.postTitle }}
                  </p>
                </div>

                <!-- Unread indicator -->
                <div v-if="!notification.read" class="w-2 h-2 bg-primary-600 rounded-full flex-shrink-0"></div>
              </div>
            </div>

            <!-- Load More -->
            <div v-if="hasMore" class="text-center py-4">
              <button
                @click="loadMore"
                :disabled="loadingMore"
                class="px-6 py-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors disabled:text-gray-400"
              >
                {{ loadingMore ? '加载中...' : '加载更多' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 审核/删除详情弹窗 -->
    <Teleport to="body">
      <transition name="fade">
        <div
          v-if="showDetailModal"
          class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
          @click.self="closeDetailModal"
        >
          <div class="bg-white rounded-2xl shadow-xl max-w-xl w-full max-h-[90vh] overflow-hidden">
            <!-- 弹窗头部 -->
            <div
              :class="[
                'px-6 py-4 border-b',
                detailModalData.formType === 'DELETION' ? 'bg-red-50' : 'bg-yellow-50'
              ]"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-3">
                  <!-- 图标 -->
                  <div
                    :class="[
                      'w-10 h-10 rounded-full flex items-center justify-center',
                      (detailModalData.formType === 'DELETION' || detailModalData.formType === 'COMMENT_DELETION') ? 'bg-red-100' : 'bg-yellow-100'
                    ]"
                  >
                    <svg
                      v-if="detailModalData.formType === 'DELETION' || detailModalData.formType === 'COMMENT_DELETION'"
                      class="w-5 h-5 text-red-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                    <svg
                      v-else
                      class="w-5 h-5 text-yellow-600"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ detailModalData.formType === 'COMMENT_DELETION' ? '评论处理结果' : (detailModalData.formType === 'DELETION' ? '文章已被删除' : '文章未通过审核') }}
                  </h3>
                </div>
                <button
                  @click="closeDetailModal"
                  class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            <!-- 弹窗内容 -->
            <div class="p-6 space-y-6 overflow-y-auto max-h-[60vh]">
              <!-- 加载中 -->
              <div v-if="loadingDetailModal" class="text-center py-8">
                <div class="spinner w-8 h-8 mx-auto"></div>
                <p class="text-gray-500 mt-2">加载中...</p>
              </div>

              <template v-else>
                <!-- 评论信息（评论删除） -->
                <div v-if="detailModalData.formType === 'COMMENT_DELETION'" class="bg-gray-50 rounded-xl p-4 space-y-3">
                  <h4 class="text-sm font-medium text-gray-500 uppercase tracking-wide">评论信息</h4>
                  <div class="space-y-2">
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">评论内容</span>
                      <span class="text-gray-900 font-medium">{{ detailModalData.commentContentPreview || '无内容预览' }}</span>
                    </div>
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">所属文章</span>
                      <span class="text-gray-900">{{ detailModalData.postTitle || '未知文章' }}</span>
                    </div>
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">状态</span>
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                        已删除
                      </span>
                    </div>
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">处理时间</span>
                      <span class="text-gray-900">{{ formatDetailTime(detailModalData.createdAt) }}</span>
                    </div>
                  </div>
                  <!-- 查看文章按钮（评论被删除后文章可能还存在） -->
                  <button
                    v-if="detailModalData.postId"
                    @click="goToPost(detailModalData.postId)"
                    class="mt-2 text-sm text-primary-600 hover:text-primary-700 font-medium inline-flex items-center"
                  >
                    查看所属文章
                    <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </button>
                </div>

                <!-- 文章信息（文章相关通知） -->
                <div v-else class="bg-gray-50 rounded-xl p-4 space-y-3">
                  <h4 class="text-sm font-medium text-gray-500 uppercase tracking-wide">文章信息</h4>
                  <div class="space-y-2">
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">文章标题</span>
                      <span class="text-gray-900 font-medium">{{ detailModalData.postTitle || '未知文章' }}</span>
                    </div>
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">状态</span>
                      <span
                        :class="[
                          'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
                          detailModalData.formType === 'DELETION' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'
                        ]"
                      >
                        {{ detailModalData.formType === 'DELETION' ? '已删除' : '已拒绝' }}
                      </span>
                    </div>
                    <div class="flex items-start">
                      <span class="text-gray-500 w-20 flex-shrink-0">处理时间</span>
                      <span class="text-gray-900">{{ formatDetailTime(detailModalData.createdAt) }}</span>
                    </div>
                  </div>
                  <!-- 查看文章按钮（如果文章还存在） -->
                  <button
                    v-if="detailModalData.postId && detailModalData.formType !== 'DELETION'"
                    @click="goToPost(detailModalData.postId)"
                    class="mt-2 text-sm text-primary-600 hover:text-primary-700 font-medium inline-flex items-center"
                  >
                    查看文章详情
                    <svg class="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </button>
                </div>

                <!-- 处理原因 -->
                <div class="space-y-3">
                  <h4 class="text-sm font-medium text-gray-500 uppercase tracking-wide">处理原因</h4>
                  <div class="bg-gray-50 rounded-xl p-4">
                    <p class="text-gray-700 whitespace-pre-wrap">{{ detailModalData.reason || '未填写原因' }}</p>
                  </div>
                </div>

                <!-- 扩展信息 -->
                <div v-if="parsedExtraFields.length > 0" class="space-y-3">
                  <h4 class="text-sm font-medium text-gray-500 uppercase tracking-wide">扩展信息</h4>
                  <div class="bg-gray-50 rounded-xl p-4 space-y-2">
                    <div
                      v-for="(field, index) in parsedExtraFields"
                      :key="index"
                      class="flex items-start"
                    >
                      <span class="text-gray-500 min-w-[100px] flex-shrink-0">{{ field.fieldName }}</span>
                      <span class="text-gray-900">{{ field.fieldValue }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <!-- 弹窗底部 -->
            <div class="px-6 py-4 border-t bg-gray-50 flex justify-end space-x-3">
              <button
                v-if="detailModalData.postId && detailModalData.formType !== 'DELETION'"
                @click="goToPost(detailModalData.postId)"
                class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                查看文章详情
              </button>
              <button
                @click="closeDetailModal"
                class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '@/components/Header.vue'
import UserProfileHoverCard from '@/components/UserProfileHoverCard.vue'
import { 
  getNotifications, 
  getNotificationUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  getFormByPostId,
  getFormByNotificationId
} from '@/api/notifications'
import { likeComment, unlikeComment, deleteComment } from '@/api/comments'
import { getFullAvatarUrl } from '@/utils/avatar'

export default {
  name: 'Notifications',
  components: { Header, UserProfileHoverCard },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const loading = ref(true)
    const loadingMore = ref(false)
    const markingAllRead = ref(false)
    const notifications = ref([])
    const unreadCount = ref(0)
    const currentPage = ref(0)
    const totalPages = ref(1)
    const currentFilter = ref('all')
    const PAGE_SIZE = 20

    // 详情弹窗状态
    const showDetailModal = ref(false)
    const loadingDetailModal = ref(false)
    const detailModalData = ref({
      formType: '',
      postId: null,
      postTitle: '',
      commentContentPreview: '',
      reason: '',
      extraFields: '',
      createdAt: ''
    })

    const hasMore = computed(() => currentPage.value < totalPages.value - 1)
    
    // 解析扩展字段
    const parsedExtraFields = computed(() => {
      try {
        if (!detailModalData.value.extraFields) return []
        return JSON.parse(detailModalData.value.extraFields)
      } catch (e) {
        return []
      }
    })

    const tabs = [
      { 
        value: 'all', 
        label: '全部',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>'
      },
      { 
        value: 'comments', 
        label: '评论',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>'
      },
      { 
        value: 'likes', 
        label: '点赞',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>'
      },
      { 
        value: 'follows', 
        label: '关注',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>'
      },
      { 
        value: 'messages', 
        label: '私信',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>'
      },
      { 
        value: 'system', 
        label: '系统',
        icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>'
      }
    ]

    const getAvatarUrl = (url) => getFullAvatarUrl(url)

    const getInitial = (name) => (name || 'U').charAt(0).toUpperCase()

    // Helper to get actor data for UserProfileHoverCard
    const getActorData = (notification) => {
      return {
        username: notification.actorUsername,
        nickname: notification.actorNickname,
        avatarUrl: getAvatarUrl(notification.actorAvatarUrl)
      }
    }

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

    const getTypeStyle = (type) => {
      switch (type) {
        case 'POST_LIKED':
        case 'COMMENT_LIKED':
          return { 
            bgColor: 'bg-red-100', 
            icon: '<svg class="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>'
          }
        case 'POST_COMMENTED':
        case 'COMMENT_REPLIED':
          return { 
            bgColor: 'bg-blue-100', 
            icon: '<svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>'
          }
        case 'FOLLOWED':
          return { 
            bgColor: 'bg-green-100', 
            icon: '<svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" /></svg>'
          }
        case 'MESSAGE_RECEIVED':
          return { 
            bgColor: 'bg-purple-100', 
            icon: '<svg class="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>'
          }
        case 'POST_APPROVED':
          return { 
            bgColor: 'bg-green-100', 
            icon: '<svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>'
          }
        case 'POST_REJECTED':
          return { 
            bgColor: 'bg-yellow-100', 
            icon: '<svg class="w-5 h-5 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>'
          }
        case 'POST_DELETED':
          return { 
            bgColor: 'bg-red-100', 
            icon: '<svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>'
          }
        case 'COMMENT_DELETED':
          return { 
            bgColor: 'bg-red-100', 
            icon: '<svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>'
          }
        default:
          return { 
            bgColor: 'bg-gray-100', 
            icon: '<svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>'
          }
      }
    }

    const getNotificationText = (notification) => {
      switch (notification.type) {
        case 'POST_LIKED':
          return '赞了你的文章'
        case 'POST_COMMENTED':
          return '评论了你的文章'
        case 'COMMENT_LIKED':
          return '赞了你的评论'
        case 'COMMENT_REPLIED':
          return '回复了你的评论'
        case 'FOLLOWED':
          return '关注了你'
        case 'MESSAGE_RECEIVED':
          return '给你发送了一条私信'
        case 'POST_APPROVED':
          return '您的文章已通过审核'
        case 'POST_REJECTED':
          return '您的文章未通过审核'
        case 'POST_DELETED':
          return '您的文章因违规被删除'
        case 'COMMENT_DELETED':
          return '您的评论因违规被删除'
        default:
          return ''
      }
    }

    // 判断是否为系统通知
    const isSystemNotification = (notification) => {
      return ['POST_APPROVED', 'POST_REJECTED', 'POST_DELETED', 'COMMENT_DELETED'].includes(notification.type)
    }

    // 获取通知显示的名称
    const getNotificationActorName = (notification) => {
      if (isSystemNotification(notification)) {
        return '系统管理员'
      }
      return notification.actorNickname || notification.actorUsername
    }

    const getEmptyMessage = () => {
      switch (currentFilter.value) {
        case 'comments':
          return '暂无评论相关的通知'
        case 'likes':
          return '暂无点赞相关的通知'
        case 'follows':
          return '暂无关注相关的通知'
        case 'messages':
          return '暂无私信相关的通知'
        case 'system':
          return '暂无系统相关的通知'
        default:
          return '当有人与你互动时，通知将会显示在这里'
      }
    }

    const loadNotifications = async (append = false) => {
      if (append) {
        loadingMore.value = true
      } else {
        loading.value = true
        currentPage.value = 0
      }

      try {
        const res = await getNotifications({
          page: currentPage.value,
          size: PAGE_SIZE,
          filter: currentFilter.value
        })

        if (append) {
          notifications.value = [...notifications.value, ...(res.content || [])]
        } else {
          notifications.value = res.content || []
        }
        totalPages.value = res.totalPages || 1

        // Also update unread count
        const countRes = await getNotificationUnreadCount()
        unreadCount.value = countRes.count || 0
      } catch (error) {
        console.error('加载通知失败:', error)
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    const loadMore = async () => {
      if (loadingMore.value || !hasMore.value) return
      currentPage.value++
      await loadNotifications(true)
    }

    const handleNotificationClick = async (notification) => {
      // Mark as read
      if (!notification.read) {
        try {
          await markNotificationAsRead(notification.id)
          notification.read = true
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }

      // Navigate based on type
      switch (notification.type) {
        case 'POST_LIKED':
          if (notification.postId) {
            router.push(`/post/${notification.postId}`)
          }
          break
        case 'POST_COMMENTED':
          if (notification.postId) {
            // Navigate to post with comment anchor (the comment that was made)
            const commentAnchor = notification.commentId ? `#comment-${notification.commentId}` : ''
            // Include parentCommentId for sub-comments (replies)
            if (notification.parentCommentId) {
              router.push({
                path: `/post/${notification.postId}`,
                query: { expandComment: notification.parentCommentId },
                hash: commentAnchor
              })
            } else {
              router.push(`/post/${notification.postId}${commentAnchor}`)
            }
          }
          break
        case 'COMMENT_LIKED':
        case 'COMMENT_REPLIED':
          if (notification.postId) {
            // Navigate to post with comment anchor
            const commentAnchor = notification.commentId ? `#comment-${notification.commentId}` : ''
            // Include parentCommentId for sub-comments (replies)
            if (notification.parentCommentId) {
              router.push({
                path: `/post/${notification.postId}`,
                query: { expandComment: notification.parentCommentId },
                hash: commentAnchor
              })
            } else {
              router.push(`/post/${notification.postId}${commentAnchor}`)
            }
          }
          break
        case 'FOLLOWED':
          if (notification.actorUsername) {
            router.push(`/user/${notification.actorUsername}`)
          }
          break
        case 'MESSAGE_RECEIVED':
          router.push({
            path: '/messages',
            query: {
              userId: notification.actorId,
              username: notification.actorUsername,
              nickname: notification.actorNickname,
              avatar: notification.actorAvatarUrl
            }
          })
          break
        case 'POST_APPROVED':
          // 审核通过，直接跳转文章详情
          if (notification.postId) {
            router.push(`/post/${notification.postId}`)
          }
          break
        case 'POST_REJECTED':
        case 'POST_DELETED':
        case 'COMMENT_DELETED':
          // 审核拒绝、文章删除或评论删除，打开详情弹窗
          openDetailModal(notification)
          break
      }
    }

    const handleMarkAllAsRead = async () => {
      markingAllRead.value = true
      try {
        await markAllNotificationsAsRead(currentFilter.value)
        // Update local state based on filter
        const typesForFilter = getTypesForFilter(currentFilter.value)
        notifications.value = notifications.value.map(n => {
          // If 'all' filter or notification type matches filter, mark as read
          if (!typesForFilter || typesForFilter.includes(n.type)) {
            return { ...n, read: true }
          }
          return n
        })
        // Refresh unread count from server
        const countRes = await getNotificationUnreadCount()
        unreadCount.value = countRes.count || 0
      } catch (error) {
        console.error('标记全部已读失败:', error)
      } finally {
        markingAllRead.value = false
      }
    }
    
    // Helper function to get notification types for filter
    const getTypesForFilter = (filter) => {
      switch (filter) {
        case 'comments':
          return ['POST_COMMENTED', 'COMMENT_REPLIED']
        case 'likes':
          return ['POST_LIKED', 'COMMENT_LIKED']
        case 'follows':
          return ['FOLLOWED']
        case 'messages':
          return ['MESSAGE_RECEIVED']
        default:
          return null // all types
      }
    }

    // Check if notification is comment-related (for quick actions)
    const isCommentNotification = (notification) => {
      return ['POST_COMMENTED', 'COMMENT_REPLIED'].includes(notification.type)
    }

    // Handle quick reply - navigate to post with reply intent
    const handleQuickReply = async (notification) => {
      // Mark as read first
      if (!notification.read) {
        try {
          await markNotificationAsRead(notification.id)
          notification.read = true
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }

      // Navigate to post page with comment anchor and reply intent
      if (notification.postId) {
        const commentAnchor = notification.commentId ? `#comment-${notification.commentId}` : ''
        router.push({
          path: `/post/${notification.postId}${commentAnchor}`,
          query: { replyTo: notification.commentId }
        })
      }
    }

    // Handle quick like for comment
    const handleQuickLike = async (notification) => {
      if (!notification.commentId || notification.liking) return
      
      notification.liking = true
      try {
        if (notification.commentLiked) {
          await unlikeComment(notification.commentId)
          notification.commentLiked = false
        } else {
          await likeComment(notification.commentId)
          notification.commentLiked = true
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
      } finally {
        notification.liking = false
      }
    }

    // Handle quick delete for comment (only for comments on your articles)
    const handleQuickDelete = async (notification) => {
      if (!notification.commentId || notification.deleting) return
      
      // Confirm deletion
      if (!confirm('确定要删除这条评论吗？')) return
      
      notification.deleting = true
      try {
        await deleteComment(notification.commentId)
        // Remove from list after successful delete
        const index = notifications.value.findIndex(n => n.id === notification.id)
        if (index !== -1) {
          notifications.value.splice(index, 1)
        }
        // Mark as read if it wasn't
        if (!notification.read) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
      } catch (error) {
        console.error('删除评论失败:', error)
        alert('删除评论失败，请稍后重试')
      } finally {
        notification.deleting = false
      }
    }

    // 打开详情弹窗（拒绝/删除/待修订通知）
    const openDetailModal = async (notification) => {
      showDetailModal.value = true
      loadingDetailModal.value = true
      
      // 尝试从通知内容中提取文章标题
      let extractedTitle = notification.postTitle || ''
      let extractedCommentPreview = ''
      if (!extractedTitle && notification.content) {
        // 从内容 "您的文章「xxx」未通过审核" 或 "您在文章「xxx」的评论「yyy」" 中提取
        const match = notification.content.match(/「(.+?)」/)
        if (match) {
          extractedTitle = match[1]
        }
        // 对于评论删除，尝试提取评论预览
        const commentMatch = notification.content.match(/评论「(.+?)」/)
        if (commentMatch) {
          extractedCommentPreview = commentMatch[1]
        }
      }
      
      // 根据通知类型确定formType
      let formType = 'REJECTION'
      if (notification.type === 'POST_DELETED') {
        formType = 'DELETION'
      } else if (notification.type === 'COMMENT_DELETED') {
        formType = 'COMMENT_DELETION'
      }
      
      // 设置基本信息
      detailModalData.value = {
        formType: formType,
        postId: notification.postId,
        postTitle: extractedTitle,
        commentContentPreview: extractedCommentPreview,
        reason: '',
        extraFields: '',
        createdAt: notification.createdAt
      }

      try {
        let form = null
        
        // 对于评论删除，直接通过通知ID查找表单
        // 对于文章相关的通知，首先尝试通过postId获取表单信息
        if (notification.type !== 'COMMENT_DELETED' && notification.postId) {
          try {
            form = await getFormByPostId(notification.postId)
          } catch (e) {
            // postId不存在或找不到表单，继续尝试其他方式
          }
        }
        
        // 如果还没找到表单，通过通知ID查找
        if (!form && notification.id) {
          try {
            form = await getFormByNotificationId(notification.id)
          } catch (e) {
            // 找不到表单
          }
        }
        
        if (form) {
          detailModalData.value = {
            ...detailModalData.value,
            postTitle: form.postTitle || detailModalData.value.postTitle,
            commentContentPreview: form.commentContentPreview || detailModalData.value.commentContentPreview,
            reason: form.reason || '',
            extraFields: form.extraFields || '',
            createdAt: form.createdAt || notification.createdAt
          }
        }
      } catch (error) {
        console.error('获取表单详情失败:', error)
        // 即使失败也显示基本信息
      }
      
      loadingDetailModal.value = false
    }

    // 关闭详情弹窗
    const closeDetailModal = () => {
      showDetailModal.value = false
    }

    // 跳转到文章详情
    const goToPost = (postId) => {
      closeDetailModal()
      router.push(`/post/${postId}`)
    }

    // 格式化详情时间
    const formatDetailTime = (timeStr) => {
      if (!timeStr) return '未知时间'
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // Watch for filter changes
    watch(currentFilter, () => {
      loadNotifications()
    })

    onMounted(async () => {
      // 处理路由参数
      if (route.query.tab) {
        currentFilter.value = route.query.tab
      }
      
      await loadNotifications()
      
      // 如果有通知ID参数，查找并打开详情弹窗
      if (route.query.notificationId) {
        const notificationId = parseInt(route.query.notificationId)
        const targetNotification = notifications.value.find(n => n.id === notificationId)
        if (targetNotification && (targetNotification.type === 'POST_REJECTED' || targetNotification.type === 'POST_DELETED')) {
          openDetailModal(targetNotification)
        } else {
          // 通知不在当前列表中，尝试通过ID直接获取表单
          try {
            const form = await getFormByNotificationId(notificationId)
            showDetailModal.value = true
            detailModalData.value = {
              formType: form.formType || 'REJECTION',
              postId: form.postId,
              postTitle: form.postTitle || '',
              reason: form.reason || '',
              extraFields: form.extraFields || '',
              createdAt: form.createdAt || ''
            }
          } catch (e) {
            console.error('无法获取通知详情:', e)
          }
        }
        // 清除URL参数但不触发导航
        router.replace({ path: '/notifications', query: { tab: currentFilter.value } })
      }
    })

    return {
      loading,
      loadingMore,
      markingAllRead,
      notifications,
      unreadCount,
      currentFilter,
      hasMore,
      tabs,
      getAvatarUrl,
      getInitial,
      getActorData,
      formatTime,
      getTypeStyle,
      getNotificationText,
      getNotificationActorName,
      isSystemNotification,
      getEmptyMessage,
      isCommentNotification,
      loadMore,
      handleNotificationClick,
      handleMarkAllAsRead,
      handleQuickReply,
      handleQuickLike,
      handleQuickDelete,
      // 详情弹窗
      showDetailModal,
      loadingDetailModal,
      detailModalData,
      parsedExtraFields,
      closeDetailModal,
      goToPost,
      formatDetailTime
    }
  }
}
</script>
