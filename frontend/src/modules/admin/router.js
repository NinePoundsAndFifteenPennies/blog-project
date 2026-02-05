export default [
    {
        path: "/admin/login",
        name: "AdminLogin",
        redirect: { name: "Login", query: { redirect: "/admin" } },
    },
    {
        path: "/admin",
        name: "AdminDashboard",
        component: () => import("@/modules/admin/views/AdminDashboard.vue"),
        meta: { title: "管理后台", requiresAdmin: true },
    },
    {
        path: "/admin/users",
        name: "AdminUserManagement",
        component: () => import("@/modules/admin/views/AdminUserManagement.vue"),
        meta: { title: "用户管理 - 管理后台", requiresAdmin: true },
    },
    {
        path: "/admin/posts",
        name: "AdminPostManagement",
        component: () => import("@/modules/admin/views/AdminPostManagement.vue"),
        meta: { title: "文章管理 - 管理后台", requiresAdmin: true },
    },
    {
        path: "/admin/comments",
        name: "AdminCommentManagement",
        component: () => import("@/modules/admin/views/AdminCommentManagement.vue"),
        meta: { title: "评论管理 - 管理后台", requiresAdmin: true },
    },
];
