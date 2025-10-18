// frontend/vue.config.js

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
            },
            // 新增这条代理规则
            '/uploads': {
                target: 'http://localhost:8080',
                changeOrigin: true,
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