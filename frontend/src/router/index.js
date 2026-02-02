import { createRouter, createWebHistory } from "vue-router";

// Helper functions to save and restore scroll positions using sessionStorage
const SCROLL_POSITIONS_KEY = "scrollPositions";

function getScrollPositions() {
    try {
        const data = sessionStorage.getItem(SCROLL_POSITIONS_KEY);
        return data ? JSON.parse(data) : {};
    } catch {
        return {};
    }
}

function saveScrollPosition(key, position) {
    try {
        const positions = getScrollPositions();
        positions[key] = position;
        const keys = Object.keys(positions);
        if (keys.length > 50) {
            const keysToRemove = keys.slice(0, keys.length - 40);
            keysToRemove.forEach((k) => delete positions[k]);
        }
        sessionStorage.setItem(SCROLL_POSITIONS_KEY, JSON.stringify(positions));
    } catch {
        // Ignore storage errors
    }
}

function getScrollPosition(key) {
    const positions = getScrollPositions();
    return positions[key] || null;
}

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
        path: "/profile/edit",
        name: "ProfileEdit",
        component: () => import("@/views/ProfileEdit.vue"),
        meta: { title: "编辑个人资料", requiresAuth: true },
    },
    {
        path: "/my-comments",
        name: "MyComments",
        component: () => import("@/views/MyComments.vue"),
        meta: { title: "我的评论", requiresAuth: true },
    },
    {
        path: "/user/:username",
        name: "UserProfile",
        component: () => import("@/views/UserProfile.vue"),
        meta: { title: "用户主页" },
    },
    {
        path: "/comment/:id/edit",
        name: "CommentEdit",
        component: () => import("@/views/CommentEdit.vue"),
        meta: { title: "编辑评论", requiresAuth: true },
    },
    {
        path: "/comment/:id/reply",
        name: "ReplyCreate",
        component: () => import("@/views/ReplyCreate.vue"),
        meta: { title: "写回复", requiresAuth: true },
    },
    {
        path: "/tags/:tagName",
        name: "TagPosts",
        component: () => import("@/views/TagPosts.vue"),
        meta: { title: "标签文章" },
    },
    {
        path: "/search",
        name: "Search",
        component: () => import("@/views/Search.vue"),
        meta: { title: "搜索结果" },
    },
    {
        path: "/user/:userId/follow/:type",
        name: "FollowList",
        component: () => import("@/views/FollowList.vue"),
        meta: { title: "关注列表" },
    },
    {
        path: "/settings",
        name: "Settings",
        component: () => import("@/views/Settings.vue"),
        meta: { title: "设置", requiresAuth: true },
    },
    {
        path: "/settings/visibility",
        name: "VisibilitySettings",
        component: () => import("@/views/VisibilitySettings.vue"),
        meta: { title: "隐私设置", requiresAuth: true },
    },
    {
        path: "/messages",
        name: "Messages",
        component: () => import("@/views/Messages.vue"),
        meta: { title: "私信", requiresAuth: true },
    },
    {
        path: "/notifications",
        name: "Notifications",
        component: () => import("@/views/Notifications.vue"),
        meta: { title: "通知", requiresAuth: true },
    },
    // -------- 管理后台路由 --------
    {
        path: "/admin/login",
        name: "AdminLogin",
        redirect: { name: "Login", query: { redirect: "/admin" } },
    },
    {
        path: "/admin",
        name: "AdminDashboard",
        component: () => import("@/views/admin/AdminDashboard.vue"),
        meta: { title: "管理后台", requiresAdmin: true },
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
        return new Promise((resolve) => {
            // 如果是浏览器前进/后退，使用浏览器保存的位置
            if (savedPosition) {
                // 延迟恢复，确保内容已加载
                setTimeout(() => {
                    resolve(savedPosition);
                }, 300);
                return;
            }

            // 检查 sessionStorage 中是否有保存的位置（用于刷新）
            const savedPos = getScrollPosition(to.fullPath);
            if (savedPos) {
                // 延迟恢复，确保内容已加载
                setTimeout(() => {
                    resolve({ top: savedPos.top, left: savedPos.left, behavior: "auto" });
                }, 300);
                return;
            }

            // 如果是同一路由但查询参数不同（如分页），保持当前位置
            if (to.path === from.path && JSON.stringify(to.query) !== JSON.stringify(from.query)) {
                resolve(false);
                return;
            }

            // 其他情况滚动到顶部
            resolve({ top: 0 });
        });
    },
});

// Save scroll position before leaving a route
router.beforeEach((to, from, next) => {
    // 保存当前页面的滚动位置
    if (from.fullPath && from.fullPath !== "/") {
        saveScrollPosition(from.fullPath, {
            top: window.scrollY || document.documentElement.scrollTop,
            left: window.scrollX || document.documentElement.scrollLeft,
        });
    }

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 博客系统` : "博客系统";

    // 检查token是否存在于localStorage中
    const token = localStorage.getItem("token");
    const isAuthenticated = !!token;

    // 获取用户信息检查角色（安全解析localStorage数据）
    let user = null;
    let isAdmin = false;
    try {
        const userStr = localStorage.getItem("user");
        if (userStr) {
            user = JSON.parse(userStr);
            isAdmin = user && typeof user.role === "string" && user.role === "ADMIN";
        }
    } catch {
        // 如果解析失败，忽略并使用默认值
    }

    // 需要管理员权限的页面
    if (to.meta.requiresAdmin) {
        if (!isAuthenticated) {
            // 未登录，跳转到统一登录页面
            return next({
                path: "/login",
                query: { redirect: to.fullPath },
            });
        } else if (!isAdmin) {
            // 已登录但非管理员，跳转首页并可以显示提示
            return next({
                path: "/",
            });
        } else {
            return next();
        }
    }
    // 需要认证的页面
    else if (to.meta.requiresAuth && !isAuthenticated) {
        return next({
            path: "/login",
            query: { redirect: to.fullPath },
        });
    }
    // 已登录用户访问登录/注册页面,重定向到首页
    else if (to.meta.guest && isAuthenticated) {
        return next("/");
    } else {
        return next();
    }
});

// 路由切换完成后再次尝试恢复滚动位置
router.afterEach((to) => {
    // 使用 nextTick 确保 DOM 已更新
    setTimeout(() => {
        const savedPos = getScrollPosition(to.fullPath);
        if (savedPos && savedPos.top > 0) {
            window.scrollTo({
                top: savedPos.top,
                left: savedPos.left,
                behavior: "auto"
            });
        }
    }, 100);
});

// 页面卸载前保存滚动位置（处理刷新）
if (typeof window !== "undefined") {
    // 使用多个事件确保能捕获到刷新操作
    const saveCurrentScroll = () => {
        const currentPath = router.currentRoute.value?.fullPath;
        if (currentPath) {
            saveScrollPosition(currentPath, {
                top: window.scrollY || document.documentElement.scrollTop,
                left: window.scrollX || document.documentElement.scrollLeft,
            });
        }
    };

    window.addEventListener("beforeunload", saveCurrentScroll);

    // 定期保存（作为备份方案）
    setInterval(saveCurrentScroll, 1000);
}

export default router;