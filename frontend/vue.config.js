// vue.config.js
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
