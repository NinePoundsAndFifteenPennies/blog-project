// vue.config.js  —— 请确保文件名精确为 vue.config.js
const path = require('path')
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,

    // 显式设置别名，确保 '@' 指向 src
    configureWebpack: {
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src')
            }
        }
    },

    devServer: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                // pathRewrite 不必要时可以省略，此处保留原样（并保持等价）
                pathRewrite: { '^/api': '/api' }
            }
        }
    },

    css: {
        loaderOptions: {
            postcss: {
                postcssOptions: {
                    plugins: [require('tailwindcss'), require('autoprefixer')]
                }
            }
        }
    }
})
