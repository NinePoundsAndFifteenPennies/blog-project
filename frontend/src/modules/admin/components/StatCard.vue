<template>
  <div class="stat-card">
    <div class="stat-info">
      <span class="stat-label">{{ label }}</span>
      <span class="stat-value">{{ formattedValue }}</span>
      <span class="stat-today" v-if="todayValue !== null">
        今日新增
        <span class="today-count">+{{ todayValue }}</span>
      </span>
    </div>
    <div :class="['stat-icon', iconClass]" v-html="icon"></div>
  </div>
</template>

<script>
export default {
  name: 'StatCard',
  props: {
    label: { type: String, required: true },
    value: { type: Number, default: 0 },
    todayValue: { type: Number, default: null },
    iconClass: { type: String, default: 'icon-blue' },
    icon: { type: String, default: '' },
  },
  computed: {
    formattedValue() {
      if (this.value >= 10000) {
        return (this.value / 10000).toFixed(1) + '万'
      }
      return this.value.toLocaleString()
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
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
