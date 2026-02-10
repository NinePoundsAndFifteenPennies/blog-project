<template>
  <div :class="['stat-card', { clickable: clickable }]" @click="handleClick">
    <div class="stat-info">
      <span class="stat-label">{{ label }}</span>
      <span class="stat-value">{{ formattedValue }}</span>
      <span class="stat-today" v-if="todayValue !== null">
        今日新增
        <span class="today-count">+{{ todayValue }}</span>
      </span>
    </div>
    <div class="stat-right">
      <div :class="['stat-icon', iconClass]" v-html="icon"></div>
      <span v-if="clickable" class="stat-detail-hint">点击查看详情 →</span>
    </div>
  </div>
</template>

<script>
import { formatLargeNumber } from '@/modules/admin/utils/chartUtils'

export default {
  name: 'StatCard',
  props: {
    label: { type: String, required: true },
    value: { type: Number, default: 0 },
    todayValue: { type: Number, default: null },
    iconClass: { type: String, default: 'icon-blue' },
    icon: { type: String, default: '' },
    clickable: { type: Boolean, default: false },
  },
  emits: ['click'],
  computed: {
    formattedValue() {
      return formatLargeNumber(this.value)
    },
  },
  methods: {
    handleClick() {
      if (this.clickable) {
        this.$emit('click')
      }
    },
  },
}
</script>

<style scoped>
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  border: 2px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  border-color: rgba(24, 144, 255, 0.3);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.15);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1d2e;
  margin-bottom: 4px;
}

.stat-today {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.today-count {
  color: #52c41a;
  font-weight: 600;
}

.stat-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon :deep(svg) {
  width: 28px;
  height: 28px;
}

.stat-detail-hint {
  font-size: 11px;
  color: #bbb;
  transition: color 0.2s;
  white-space: nowrap;
}

.stat-card.clickable:hover .stat-detail-hint {
  color: #1890ff;
}

.icon-blue {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.icon-green {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.icon-yellow {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
}

.icon-purple {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}
</style>
