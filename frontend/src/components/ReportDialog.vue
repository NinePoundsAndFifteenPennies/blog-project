<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 z-[9999] flex items-center justify-center" @click.self="handleClose">
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
      <!-- Header -->
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900">
          举报{{ targetType === 'POST' ? '文章' : '评论' }}
        </h3>
        <button @click="handleClose" class="text-gray-400 hover:text-gray-600 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Body -->
      <div class="px-6 py-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">举报原因 <span class="text-red-500">*</span></label>
        <textarea
          v-model="reason"
          rows="4"
          maxlength="500"
          class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all resize-none"
          placeholder="请详细描述举报原因..."
          :disabled="submitting"
        ></textarea>
        <div class="flex justify-between items-center mt-1">
          <span v-if="error" class="text-sm text-red-500">{{ error }}</span>
          <span v-else class="text-sm text-gray-400">请提供具体的举报理由</span>
          <span class="text-sm text-gray-400">{{ reason.length }}/500</span>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t border-gray-100 flex justify-end space-x-3">
        <button
          @click="handleClose"
          :disabled="submitting"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
        >
          取消
        </button>
        <button
          @click="handleSubmit"
          :disabled="submitting || !reason.trim()"
          class="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center space-x-1"
        >
          <svg v-if="submitting" class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
          </svg>
          <span>{{ submitting ? '提交中...' : '提交举报' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { submitReport } from '@/api/reports'

export default {
  name: 'ReportDialog',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    },
    targetType: {
      type: String,
      required: true,
      validator: (value) => ['POST', 'COMMENT'].includes(value)
    },
    targetId: {
      type: Number,
      required: true
    }
  },
  emits: ['close', 'submitted'],
  setup(props, { emit }) {
    const reason = ref('')
    const submitting = ref(false)
    const error = ref('')

    watch(() => props.isOpen, (newVal) => {
      if (newVal) {
        reason.value = ''
        error.value = ''
        submitting.value = false
      }
    })

    const handleClose = () => {
      if (!submitting.value) {
        emit('close')
      }
    }

    const handleSubmit = async () => {
      if (!reason.value.trim()) {
        error.value = '请输入举报原因'
        return
      }

      submitting.value = true
      error.value = ''

      try {
        await submitReport({
          targetType: props.targetType,
          targetId: props.targetId,
          reason: reason.value.trim()
        })
        emit('submitted')
        emit('close')
        alert('举报提交成功，我们将尽快处理')
      } catch (err) {
        const message = err.response?.data || err.message || '提交失败，请稍后重试'
        error.value = typeof message === 'string' ? message : '提交失败，请稍后重试'
      } finally {
        submitting.value = false
      }
    }

    return {
      reason,
      submitting,
      error,
      handleClose,
      handleSubmit
    }
  }
}
</script>
