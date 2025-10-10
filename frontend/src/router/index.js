import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Home.vue"),
    meta: { title: "首页" },
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
    meta: { title: "登录", guest: true },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/Register.vue"),
    meta: { title: "注册", guest: true },
  },
  {
    path: "/post/:id",
    name: "PostDetail",
    component: () => import("@/views/PostDetail.vue"),
    meta: { title: "文章详情" },
  },
  {
    path: "/post/create",
    name: "PostCreate",
    component: () => import("@/views/PostEdit.vue"),
    meta: { title: "创建文章", requiresAuth: true },
  },
  {
    path: "/post/:id/edit",
    name: "PostEdit",
    component: () => import("@/views/PostEdit.vue"),
    meta: { title: "编辑文章", requiresAuth: true },
  },
  {
    path: "/profile",
    name: "Profile",
    component: () => import("@/views/Profile.vue"),
    meta: { title: "个人中心", requiresAuth: true },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/views/NotFound.vue"),
    meta: { title: "页面不存在" },
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 博客系统` : "博客系统";

  // 检查token是否存在于localStorage中
  const token = localStorage.getItem("token");
  const isAuthenticated = !!token;

  // 需要认证的页面
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({
      path: "/login",
      query: { redirect: to.fullPath },
    });
  }
  // 已登录用户访问登录/注册页面,重定向到首页
  else if (to.meta.guest && isAuthenticated) {
    next("/");
  } else {
    next();
  }
});

export default router;
