<template>
  <div class="flex items-center justify-center space-x-2 my-8">
    <!-- 上一页按钮 -->
    <button
      @click="changePage(currentPage - 1)"
      :disabled="currentPage === 1"
      class="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
      :class="{ 'hover:border-primary-500': currentPage > 1 }"
    >
      <svg
        class="w-5 h-5"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M15 19l-7-7 7-7"
        />
      </svg>
    </button>

    <!-- 页码按钮 -->
    <template v-for="page in displayPages" :key="page">
      <button
        v-if="page !== '...'"
        @click="changePage(page)"
        class="w-10 h-10 rounded-lg font-medium transition-all duration-200"
        :class="[
          page === currentPage
            ? 'bg-gradient-primary text-white shadow-lg'
            : 'border border-gray-300 text-gray-700 hover:bg-gray-50 hover:border-primary-500',
        ]"
      >
        {{ page }}
      </button>
      <span v-else class="px-2 text-gray-500">...</span>
    </template>

    <!-- 下一页按钮 -->
    <button
      @click="changePage(currentPage + 1)"
      :disabled="currentPage === totalPages"
      class="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
      :class="{ 'hover:border-primary-500': currentPage < totalPages }"
    >
      <svg
        class="w-5 h-5"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 5l7 7-7 7"
        />
      </svg>
    </button>

    <!-- 页面信息 -->
    <div class="ml-4 text-sm text-gray-600">
      共 <span class="font-semibold text-primary-600">{{ totalPages }}</span> 页
    </div>
  </div>
</template>

<script>
import { computed } from "vue";

export default {
  name: "Pagination",
  props: {
    currentPage: {
      type: Number,
      required: true,
      default: 1,
    },
    totalPages: {
      type: Number,
      required: true,
      default: 1,
    },
    maxVisiblePages: {
      type: Number,
      default: 5,
    },
  },
  emits: ["page-change"],
  setup(props, { emit }) {
    // 计算显示的页码
    const displayPages = computed(() => {
      const pages = [];
      const { currentPage, totalPages, maxVisiblePages } = props;

      if (totalPages <= maxVisiblePages) {
        // 总页数小于最大显示页数,显示所有页码
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        // 总页数大于最大显示页数,需要省略
        const halfVisible = Math.floor(maxVisiblePages / 2);

        if (currentPage <= halfVisible + 1) {
          // 当前页靠近开始
          for (let i = 1; i <= maxVisiblePages - 1; i++) {
            pages.push(i);
          }
          pages.push("...");
          pages.push(totalPages);
        } else if (currentPage >= totalPages - halfVisible) {
          // 当前页靠近结束
          pages.push(1);
          pages.push("...");
          for (let i = totalPages - maxVisiblePages + 2; i <= totalPages; i++) {
            pages.push(i);
          }
        } else {
          // 当前页在中间
          pages.push(1);
          pages.push("...");
          for (
            let i = currentPage - halfVisible + 1;
            i <= currentPage + halfVisible - 1;
            i++
          ) {
            pages.push(i);
          }
          pages.push("...");
          pages.push(totalPages);
        }
      }

      return pages;
    });

    const changePage = (page) => {
      if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
        emit("page-change", page);
      }
    };

    return {
      displayPages,
      changePage,
    };
  },
};
</script>
