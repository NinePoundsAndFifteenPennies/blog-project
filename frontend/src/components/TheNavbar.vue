<template>
  <nav class="sticky top-0 z-50 w-full bg-white/80 backdrop-blur-md border-b border-gray-100 transition-all duration-300">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <router-link to="/" class="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary-600 to-primary-800 hover:opacity-80 transition-opacity">
          我的博客
        </router-link>
        
        <div class="hidden md:flex items-center space-x-8">
          <router-link 
            v-for="(item, index) in navItems" 
            :key="index"
            :to="item.path"
            class="text-gray-600 hover:text-primary-600 font-medium transition-colors relative group"
            active-class="text-primary-600"
          >
            {{ item.name }}
            <span class="absolute -bottom-1 left-0 w-0 h-0.5 bg-primary-600 group-hover:w-full transition-all duration-300"></span>
          </router-link>
        </div>

        <!-- Mobile menu button -->
        <button 
          @click="mobileMenuOpen = !mobileMenuOpen"
          class="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path v-if="!mobileMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Mobile menu -->
      <div v-if="mobileMenuOpen" class="md:hidden pb-4">
        <div class="flex flex-col space-y-2">
          <router-link 
            v-for="(item, index) in navItems" 
            :key="index"
            :to="item.path"
            class="px-4 py-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg font-medium transition-colors"
            active-class="text-primary-600 bg-primary-50"
            @click="mobileMenuOpen = false"
          >
            {{ item.name }}
          </router-link>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import { ref } from 'vue'

export default {
  name: "TheNavbar",
  setup() {
    const mobileMenuOpen = ref(false)
    const navItems = [
      { name: '主页', path: '/' },
      { name: '登录', path: '/login' },
      { name: '注册', path: '/register' }
    ]
    return { navItems, mobileMenuOpen }
  }
};
</script>
