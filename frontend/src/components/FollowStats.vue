<template>
  <div class="flex items-center justify-center md:justify-start space-x-8">
    <!-- 关注数 -->
    <div 
      class="text-center cursor-pointer hover:opacity-80 transition-opacity"
      @click="handleClick('following')"
    >
      <div class="text-3xl font-bold text-primary-600 mb-1">
        {{ formatCount(stats.followingCount) }}
      </div>
      <div class="text-sm text-gray-600 font-medium">关注</div>
    </div>

    <div class="w-px h-12 bg-gray-200"></div>

    <!-- 粉丝数 -->
    <div 
      class="text-center cursor-pointer hover:opacity-80 transition-opacity"
      @click="handleClick('followers')"
    >
      <div class="text-3xl font-bold text-orange-600 mb-1">
        {{ formatCount(stats.followerCount) }}
      </div>
      <div class="text-sm text-gray-600 font-medium">粉丝</div>
    </div>

    <div class="w-px h-12 bg-gray-200"></div>

    <!-- 朋友数 -->
    <div 
      class="text-center cursor-pointer hover:opacity-80 transition-opacity"
      @click="handleClick('friends')"
    >
      <div class="text-3xl font-bold text-purple-600 mb-1">
        {{ formatCount(stats.friendCount) }}
      </div>
      <div class="text-sm text-gray-600 font-medium">朋友</div>
    </div>
  </div>
</template>

<script>
import { reactive, onMounted, watch } from 'vue'
import { getFollowStats } from '@/api/follow'

export default {
  name: 'FollowStats',
  props: {
    userId: {
      type: Number,
      required: true
    }
  },
  emits: ['click', 'stats-loaded'],
  setup(props, { emit }) {
    const stats = reactive({
      followingCount: 0,
      followerCount: 0,
      friendCount: 0,
      isFollowing: null,
      isFriend: null
    })

    const formatCount = (count) => {
      if (count === -1) return '-'
      if (count >= 10000) return (count / 10000).toFixed(1) + '万'
      return count
    }

    const loadStats = async () => {
      try {
        const data = await getFollowStats(props.userId)
        stats.followingCount = data.followingCount
        stats.followerCount = data.followerCount
        stats.friendCount = data.friendCount
        stats.isFollowing = data.isFollowing
        stats.isFriend = data.isFriend
        // 通知父组件关注状态已加载
        emit('stats-loaded', {
          isFollowing: data.isFollowing,
          isFriend: data.isFriend
        })
      } catch (error) {
        console.error('加载关注统计失败:', error)
      }
    }

    const handleClick = (type) => {
      emit('click', type)
    }

    const refresh = () => {
      loadStats()
    }

    onMounted(() => {
      loadStats()
    })

    // 监听userId变化
    watch(() => props.userId, () => {
      loadStats()
    })

    return {
      stats,
      formatCount,
      handleClick,
      refresh
    }
  }
}
</script>
