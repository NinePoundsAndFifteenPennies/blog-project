<template>
  <div
    class="min-h-screen bg-gradient-to-br from-primary-50 via-white to-secondary-50 flex items-center justify-center p-4"
  >
    <div class="text-center">
      <!-- 404 Animation -->
      <div class="mb-8">
        <div
          class="text-9xl font-bold bg-gradient-primary bg-clip-text text-transparent animate-pulse"
        >
          404
        </div>
      </div>

      <!-- Icon -->
      <div class="mb-6">
        <svg
          class="w-32 h-32 mx-auto text-primary-300"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      </div>

      <!-- Message -->
      <h1 class="text-3xl font-bold text-gray-900 mb-4">页面不存在</h1>
      <p class="text-gray-600 mb-8 max-w-md mx-auto">
        抱歉,您访问的页面不存在或已被删除。请检查网址是否正确,或返回首页浏览其他内容。
      </p>

      <!-- Actions -->
      <div
        class="flex flex-col sm:flex-row items-center justify-center space-y-3 sm:space-y-0 sm:space-x-4"
      >
        <router-link to="/" class="btn-primary">
          <svg
            class="w-5 h-5 mr-2 inline"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            />
          </svg>
          返回首页
        </router-link>
        <button @click="goBack" class="btn-secondary">
          <svg
            class="w-5 h-5 mr-2 inline"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          返回上一页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router";

export default {
  name: "NotFound",
  setup() {
    const router = useRouter();

    const goBack = () => {
      if (window.history.length > 2) {
        router.go(-1);
      } else {
        router.push("/");
      }
    };

    return {
      goBack,
    };
  },
};
</script>
