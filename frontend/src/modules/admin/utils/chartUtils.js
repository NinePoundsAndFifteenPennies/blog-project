/**
 * 图表配色方案和工具函数
 * 用于仪表盘各类图表的统一风格配置
 */

// 主题色板
export const COLORS = {
  primary: '#1890ff',
  success: '#52c41a',
  warning: '#faad14',
  danger: '#ff4d4f',
  purple: '#722ed1',
  cyan: '#13c2c2',
  magenta: '#eb2f96',
  geekblue: '#2f54eb',
}

// 十六进制颜色转 rgba
export function hexToRgba(hex, alpha = 1) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 格式化大数字（10000以上转换为"万"）
export function formatLargeNumber(value) {
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万'
  }
  return value.toLocaleString()
}

// 面积折线图数据集默认配置
export function createAreaDataset(label, data, color) {
  return {
    label,
    data,
    borderColor: color,
    backgroundColor: hexToRgba(color, 0.1),
    borderWidth: 2,
    pointRadius: 3,
    pointHoverRadius: 5,
    pointBackgroundColor: '#fff',
    pointBorderColor: color,
    pointBorderWidth: 2,
    fill: true,
    tension: 0.4,
  }
}

// 柱状图数据集默认配置
export function createBarDataset(label, data, color) {
  return {
    label,
    data,
    backgroundColor: hexToRgba(color, 0.7),
    hoverBackgroundColor: color,
    borderRadius: 4,
    borderSkipped: false,
  }
}

// 水平柱状图渐变色列表
export function generateBarColors(count) {
  const palette = [
    COLORS.primary, COLORS.geekblue, COLORS.cyan,
    COLORS.success, COLORS.purple, COLORS.magenta,
    COLORS.warning, COLORS.danger,
  ]
  const colors = []
  for (let i = 0; i < count; i++) {
    colors.push(hexToRgba(palette[i % palette.length], 0.75))
  }
  return colors
}
