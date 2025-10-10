import axios from "axios";
import router from "@/router";
import store from "@/store";
import { jwtDecode } from "jwt-decode";

// 创建axios实例
const request = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 标志位：防止多个请求同时刷新token
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// 检查token是否即将过期（在5分钟内过期）
const isTokenExpiringSoon = (token) => {
  try {
    const decoded = jwtDecode(token);
    const currentTime = Date.now() / 1000;
    const expirationTime = decoded.exp;
    const timeUntilExpiry = expirationTime - currentTime;
    
    // 如果token在5分钟内过期，返回true
    return timeUntilExpiry < 300; // 300秒 = 5分钟
  } catch (error) {
    return false;
  }
};

// 刷新token的函数
const refreshAuthToken = async (rememberMe) => {
  try {
    const response = await axios.post('/api/users/refresh-token', {
      rememberMe: rememberMe
    }, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    const newToken = response.data.accessToken;
    localStorage.setItem('token', newToken);
    store.commit('SET_TOKEN', { token: newToken, rememberMe });
    return newToken;
  } catch (error) {
    throw error;
  }
};

// 请求拦截器
request.interceptors.request.use(
  async (config) => {
    // 从localStorage获取token
    let token = localStorage.getItem("token");
    
    if (token) {
      // 检查token是否即将过期
      if (isTokenExpiringSoon(token)) {
        const rememberMe = localStorage.getItem('rememberMe') === 'true';
        
        if (!isRefreshing) {
          isRefreshing = true;
          
          try {
            // 刷新token
            const newToken = await refreshAuthToken(rememberMe);
            token = newToken;
            isRefreshing = false;
            processQueue(null, newToken);
          } catch (error) {
            isRefreshing = false;
            processQueue(error, null);
            // 刷新失败，清除认证信息
            store.commit('CLEAR_AUTH');
            router.push({ 
              path: "/login", 
              query: { message: "登录已过期，请重新登录" } 
            });
            return Promise.reject(error);
          }
        } else {
          // 如果正在刷新，将请求加入队列
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then(newToken => {
            config.headers.Authorization = `Bearer ${newToken}`;
            return config;
          }).catch(error => {
            return Promise.reject(error);
          });
        }
      }
      
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    // 处理错误响应
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // token过期或无效,清除所有认证信息并跳转到登录页
          store.commit('CLEAR_AUTH');
          // 跳转到登录页，并在query中添加提示信息
          router.push({ 
            path: "/login", 
            query: { 
              redirect: router.currentRoute.value.fullPath,
              message: "登录已过期，请重新登录" 
            } 
          });
          break;
        case 403:
          console.error("没有权限访问");
          break;
        case 404:
          console.error("请求的资源不存在");
          break;
        case 500:
          console.error("服务器错误");
          break;
        default:
          console.error("请求失败:", error.response.data.message);
      }
    } else if (error.request) {
      console.error("网络错误,请检查网络连接");
    } else {
      console.error("请求配置错误:", error.message);
    }
    return Promise.reject(error);
  }
);

export default request;
