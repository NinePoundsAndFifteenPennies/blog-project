<template>
  <span
    class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium transition-all duration-200 cursor-default"
    :style="tagStyle"
    :title="tag.description || tag.name"
  >
    <i v-if="tag.icon && showIcon" :class="getIconClass(tag.icon)" class="mr-1"></i>
    {{ tag.name }}
  </span>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'TagBadge',
  props: {
    tag: {
      type: Object,
      required: true
    },
    showIcon: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    // 计算标签样式
    const tagStyle = computed(() => {
      const color = props.tag.color || '#6B7280' // 默认灰色
      
      // 将颜色转换为 RGB 以便调整透明度
      const hexToRgb = (hex) => {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
        return result ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        } : { r: 107, g: 112, b: 128 } // 默认灰色 RGB
      }
      
      const rgb = hexToRgb(color)
      
      return {
        backgroundColor: `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.1)`,
        color: color,
        border: `1px solid rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.2)`
      }
    })

    // 获取Font Awesome图标类名
    // 支持多种格式：fa-java, fa-brands fa-java, fab fa-java 等
    const getIconClass = (icon) => {
      if (!icon) return ''
      
      // 如果已经包含 fa-brands、fa-solid 等前缀，直接返回
      if (icon.includes('fa-brands') || icon.includes('fa-solid') || icon.includes('fa-regular') || 
          icon.includes('fab ') || icon.includes('fas ') || icon.includes('far ')) {
        return icon
      }
      
      // 如果只是 fa-xxx 格式，添加 fa-brands 前缀（大多数品牌图标）
      if (icon.startsWith('fa-')) {
        return `fa-brands ${icon}`
      }
      
      // 其他情况返回原值
      return icon
    }

    return {
      tagStyle,
      getIconClass
    }
  }
}
</script>
