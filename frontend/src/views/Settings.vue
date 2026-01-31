<template>
  <div class="min-h-screen">
    <Header />

    <div class="pt-24 pb-12">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto">
          <!-- Header -->
          <div class="mb-8">
            <h1 class="text-3xl font-bold text-gray-900">设置</h1>
            <p class="text-gray-600 mt-2">管理您的账户和隐私设置</p>
          </div>

          <!-- Settings Options -->
          <div class="space-y-4">
            <!-- Edit Profile -->
            <router-link 
              to="/profile/edit"
              class="card p-6 bg-white hover:shadow-lg transition-all duration-200 flex items-center justify-between group"
            >
              <div class="flex items-center space-x-4">
                <div class="w-12 h-12 rounded-full bg-primary-100 flex items-center justify-center">
                  <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 group-hover:text-primary-600 transition-colors">编辑资料</h3>
                  <p class="text-gray-500 text-sm">修改您的个人信息、昵称、简介等</p>
                </div>
              </div>
              <svg class="w-5 h-5 text-gray-400 group-hover:text-primary-600 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </router-link>

            <!-- Privacy Settings -->
            <router-link 
              to="/settings/visibility"
              class="card p-6 bg-white hover:shadow-lg transition-all duration-200 flex items-center justify-between group"
            >
              <div class="flex items-center space-x-4">
                <div class="w-12 h-12 rounded-full bg-purple-100 flex items-center justify-center">
                  <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 group-hover:text-purple-600 transition-colors">隐私设置</h3>
                  <p class="text-gray-500 text-sm">控制谁可以查看您的关注、粉丝和朋友列表</p>
                </div>
              </div>
              <svg class="w-5 h-5 text-gray-400 group-hover:text-purple-600 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Header from '@/components/Header.vue'

export default {
  name: 'Settings',
  components: { Header }
}
</script>
