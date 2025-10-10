import axios from "axios";
import router from "@/router";

// 创建axios实例
const request = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage或sessionStorage获取token
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    if (token) {
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
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          localStorage.removeItem("rememberMe");
          sessionStorage.removeItem("token");
          sessionStorage.removeItem("user");
          router.push("/login");
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
