// frontend/vue.config.js
const path = require('path')
const { defineConfig } = require('@vue/cli-service')

/**
 * 合并说明（设计目标）
 * - 默认在生产环境（NODE_ENV !== 'development'）下不启用 devServer，保证 cloud/server 适配不被破坏（与 main 行为一致）。
 * - 在本地开发（NODE_ENV === 'development'）或显式设置 VUE_APP_ENABLE_PROXY='true' 时启用本地代理（与 localdevelop 行为一致）。
 * - 提供一个可选的环境变量 VUE_APP_API_TARGET 来覆盖本地代理目标（方便在不同本地后端地址间切换）。
 *
 * 使用说明：
 * - 本地开发时一般运行 `npm run serve`，NODE_ENV 会是 'development'，会自动启用代理。
 * - 若需要在非 'development' 模式下强制启用代理，可在启动时传入：VUE_APP_ENABLE_PROXY=true npm run serve
 * - 若想改变代理后端地址（例如：远程调试），在启动前设置：VUE_APP_API_TARGET=http://your-backend:8080
 */

const isDev = process.env.NODE_ENV === 'development'
const enableProxy = isDev || process.env.VUE_APP_ENABLE_PROXY === 'true'

// 默认代理目标（可被 VUE_APP_API_TARGET 覆盖）
const defaultApiTarget = process.env.VUE_APP_API_TARGET || 'http://localhost:8080'

const devServer = enableProxy
  ? {
      port: 3000,
      proxy: {
        // 转发 /api 到后端
        '/api': {
          target: defaultApiTarget,
          changeOrigin: true,
          // 若后端对路径有严格要求，可以根据需要配置 pathRewrite
          // pathRewrite: { '^/api': '/api' },
        },
        // 新增 /uploads 代理（保持 localdevelop 行为）
        '/uploads': {
          target: defaultApiTarget,
          changeOrigin: true,
        },
      },
    }
  : undefined

module.exports = defineConfig({
  transpileDependencies: true,

  // 显式设置别名，确保 '@' 指向 src
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
  },

  // 仅当 enableProxy 为 true 时将 devServer 注入配置（生产环境不会有 devServer 节点）
  ...(devServer ? { devServer } : {}),

  css: {
    loaderOptions: {
      postcss: {
        postcssOptions: {
          plugins: [require('tailwindcss'), require('autoprefixer')],
        },
      },
    },
  },
})