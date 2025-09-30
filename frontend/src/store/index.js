import { createStore } from "vuex";
import { login, register, getCurrentUser } from "@/api/auth";

export default createStore({
  state: {
    user: JSON.parse(localStorage.getItem("user")) || null,
    token: localStorage.getItem("token") || null,
    isAuthenticated: !!localStorage.getItem("token"),
  },

  getters: {
    currentUser: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
    userId: (state) => state.user?.id,
  },

  mutations: {
    SET_USER(state, user) {
      state.user = user;
      state.isAuthenticated = !!user;
      if (user) {
        localStorage.setItem("user", JSON.stringify(user));
      } else {
        localStorage.removeItem("user");
      }
    },

    SET_TOKEN(state, token) {
      state.token = token;
      if (token) {
        localStorage.setItem("token", token);
      } else {
        localStorage.removeItem("token");
      }
    },

    CLEAR_AUTH(state) {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      localStorage.removeItem("user");
      localStorage.removeItem("token");
    },
  },

  actions: {
    // 登录操作
    async login({ commit }, credentials) {
      try {
        const response = await login(credentials);
        // 假设后端返回 { token, user }
        commit("SET_TOKEN", response.token);
        commit("SET_USER", response.user);
        return response;
      } catch (error) {
        commit("CLEAR_AUTH");
        throw error;
      }
    },

    // 注册操作
    async register({ commit }, userData) {
      try {
        const response = await register(userData);
        // 注册成功后自动登录
        if (response.token) {
          commit("SET_TOKEN", response.token);
          commit("SET_USER", response.user);
        }
        return response;
      } catch (error) {
        throw error;
      }
    },

    // 登出操作
    logout({ commit }) {
      commit("CLEAR_AUTH");
    },

    // 获取当前用户信息
    async fetchCurrentUser({ commit }) {
      try {
        const user = await getCurrentUser();
        commit("SET_USER", user);
        return user;
      } catch (error) {
        commit("CLEAR_AUTH");
        throw error;
      }
    },
  },
});
