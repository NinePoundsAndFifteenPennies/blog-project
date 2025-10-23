import { createStore } from 'vuex'
import { login, register, getCurrentUser } from '@/api/auth'
import router from '@/router'

export default createStore({
    state: {
        user: JSON.parse(localStorage.getItem('user')) || null,
        token: localStorage.getItem('token') || null,
        rememberMe: localStorage.getItem('rememberMe') === 'true',
    },

    getters: {
        currentUser: state => state.user,
        isLoggedIn: state => !!state.token,
    },

    mutations: {
        SET_USER(state, user) {
            state.user = user
            if (user) {
                localStorage.setItem('user', JSON.stringify(user))
            } else {
                localStorage.removeItem('user')
            }
        },

        SET_TOKEN(state, { token, rememberMe }) {
            state.token = token
            state.rememberMe = rememberMe
            if (token) {
                localStorage.setItem('token', token)
                localStorage.setItem('rememberMe', rememberMe.toString())
            } else {
                localStorage.removeItem('token')
                localStorage.removeItem('rememberMe')
            }
        },

        UPDATE_USER_AVATAR(state, avatarUrl) {
            if (state.user) {
                state.user.avatarUrl = avatarUrl
                localStorage.setItem('user', JSON.stringify(state.user))
            }
        },

        CLEAR_AUTH(state) {
            state.user = null
            state.token = null
            state.rememberMe = false
            localStorage.removeItem('user')
            localStorage.removeItem('token')
            localStorage.removeItem('rememberMe')
        }
    },

    actions: {
        // --- 登录操作 (重构) ---
        async login({ commit, dispatch }, credentials) {
            try {
                // 1. 调用API获取token (传递rememberMe参数)
                const token = await login(credentials)
                commit('SET_TOKEN', { token, rememberMe: credentials.rememberMe || false })

                // 2. 获取token后，再调用API获取用户信息
                await dispatch('fetchCurrentUser')

            } catch (error) {
                // 如果出错，确保清除所有认证信息
                commit('CLEAR_AUTH')
                // 将错误继续抛出，让组件可以捕获并显示提示
                throw error
            }
        },

        // --- 注册操作 (重构) ---
        // eslint-disable-next-line no-unused-vars
        async register({ commit }, userData) {
            // 注册API只返回成功消息，不自动登录
            // 我们只需要调用它，不做任何状态变更
            return register(userData)
        },

        // --- 登出操作 ---
        logout({ commit }) {
            commit('CLEAR_AUTH')
            // 登出后跳转到登录页
            router.push({ name: 'Login' })
        },

        // --- 获取当前用户信息 ---
        async fetchCurrentUser({ commit }) {
            try {
                // 如果有token，才去获取用户信息
                if (this.getters.isLoggedIn) {
                    const user = await getCurrentUser()
                    commit('SET_USER', user)
                    return user
                }
            } catch (error) {
                // 如果获取用户信息失败 (例如token过期)，则清除所有登录状态
                commit('CLEAR_AUTH')
                throw error
            }
        }
    }
})