<template>
  <transition name="modal-fade">
    <div v-if="visible" class="chart-modal-overlay" @click.self="$emit('close')">
      <div class="chart-modal">
        <div class="chart-modal-header">
          <div>
            <h3>{{ title }}</h3>
            <span class="chart-modal-subtitle" v-if="subtitle">{{ subtitle }}</span>
          </div>
          <button class="chart-modal-close" @click="$emit('close')">×</button>
        </div>
        <div class="chart-modal-body">
          <slot></slot>
        </div>
        <div class="chart-modal-footer" v-if="$slots.footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'ChartDetailModal',
  props: {
    visible: { type: Boolean, default: false },
    title: { type: String, default: '' },
    subtitle: { type: String, default: '' },
  },
  emits: ['close'],
}
</script>

<style scoped>
.chart-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.chart-modal {
  background: #fff;
  border-radius: 16px;
  width: 800px;
  max-width: 92vw;
  max-height: 88vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
}

.chart-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1d2e;
}

.chart-modal-subtitle {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.chart-modal-close {
  background: #f5f5f5;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.chart-modal-close:hover {
  background: #e8e8e8;
  color: #333;
}

.chart-modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.chart-modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-fade-enter-active {
  animation: modalIn 0.3s ease-out;
}

.modal-fade-leave-active {
  animation: modalOut 0.2s ease-in;
}

@keyframes modalIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes modalOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

.modal-fade-enter-active .chart-modal {
  animation: modalSlide 0.3s ease-out;
}

@keyframes modalSlide {
  from {
    transform: translateY(20px) scale(0.95);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}
</style>
