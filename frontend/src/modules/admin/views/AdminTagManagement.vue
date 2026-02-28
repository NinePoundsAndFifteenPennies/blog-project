<template>
  <AdminLayout
    :menu-items="menuItems"
    page-title="标签管理"
    :is-active-route="isActiveRoute"
  >
    <!-- Search Form -->
    <div class="card search-card">
      <div class="card-header">
        <h3>搜索条件</h3>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="resetSearch">重置</button>
        </div>
      </div>
      <div class="search-form">
        <div class="form-row">
          <div class="form-group">
            <label>标签名称</label>
            <input type="text" v-model="searchForm.name" class="form-input" placeholder="搜索标签名称">
          </div>
          <div class="form-group">
            <label>创建者</label>
            <input type="text" v-model="searchForm.createdBy" class="form-input" placeholder="创建者用户名">
          </div>
          <div class="form-group">
            <label>创建开始日期</label>
            <input type="date" v-model="searchForm.startDate" class="form-input">
          </div>
          <div class="form-group">
            <label>创建结束日期</label>
            <input type="date" v-model="searchForm.endDate" class="form-input">
          </div>
          <div class="form-group search-btn-group">
            <button class="btn btn-primary" @click="searchTags">搜索</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Tag List -->
    <div class="card">
      <div class="card-header">
        <h3>标签列表 <span v-if="pagination.total > 0">({{ pagination.total }})</span></h3>
        <div class="header-actions">
          <button class="btn btn-primary btn-sm" @click="openCreateModal">+ 新建标签</button>
          <template v-if="selectedTagIds.length > 0">
            <span class="selected-count">已选择 {{ selectedTagIds.length }} 项</span>
            <button class="btn btn-warning btn-sm" @click="openBatchDeleteModal('SOFT_DELETE')">批量软删除</button>
            <button class="btn btn-danger btn-sm" @click="openBatchDeleteModal('HARD_DELETE')">批量硬删除</button>
          </template>
        </div>
      </div>
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">加载中...</div>
      </div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th class="checkbox-col">
              <input
                type="checkbox"
                :checked="isAllSelected"
                @change="toggleSelectAll"
                :indeterminate="isPartialSelected"
              >
            </th>
            <th>ID</th>
            <th>标签名称</th>
            <th>描述</th>
            <th>颜色</th>
            <th>图标</th>
            <th>文章数</th>
            <th>创建者</th>
            <th>排序</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="tagList.length === 0">
            <td colspan="11" class="empty-row">暂无标签数据</td>
          </tr>
          <tr v-for="tag in tagList" :key="tag.id" :class="{ 'selected-row': isTagSelected(tag.id) }">
            <td class="checkbox-col">
              <input
                type="checkbox"
                :checked="isTagSelected(tag.id)"
                @change="toggleTagSelection(tag.id)"
              >
            </td>
            <td>{{ tag.id }}</td>
            <td>
              <span class="tag-name-cell" @click="viewTagDetail(tag)">
                <span v-if="tag.color" class="tag-color-dot" :style="{ backgroundColor: tag.color }"></span>
                {{ tag.name }}
              </span>
            </td>
            <td class="desc-col">{{ truncateText(tag.description, 30) }}</td>
            <td>
              <span v-if="tag.color" class="color-preview" :style="{ backgroundColor: tag.color }">{{ tag.color }}</span>
              <span v-else class="text-muted">-</span>
            </td>
            <td>{{ tag.icon || '-' }}</td>
            <td>
              <span 
                class="post-count-badge clickable" 
                :class="getPostCountClass(tag.postCount)"
                @click="toggleExpandPosts(tag)"
                :title="tag.postCount > 0 ? '点击展开/收起关联文章' : ''"
              >
                {{ tag.postCount }}
                <span v-if="tag.postCount > 0" class="expand-arrow">{{ isTagExpanded(tag.id) ? '▲' : '▼' }}</span>
              </span>
              <div v-if="isTagExpanded(tag.id) && expandedPostsMap[tag.id]" class="expanded-posts">
                <div v-for="post in expandedPostsMap[tag.id]" :key="post.postId" class="expanded-post-item">
                  <router-link :to="`/post/${post.postId}`" class="post-link-sm" target="_blank">
                    <span class="post-id-badge-sm">#{{ post.postId }}</span>
                    {{ truncateText(post.postTitle, 20) }}
                  </router-link>
                </div>
              </div>
              <div v-if="isTagExpanded(tag.id) && expandedPostsLoading[tag.id]" class="expanded-posts">
                <span class="text-muted">加载中...</span>
              </div>
            </td>
            <td>{{ tag.createdByNickname || tag.createdByUsername || '-' }}</td>
            <td>{{ tag.sortOrder != null ? tag.sortOrder : '-' }}</td>
            <td>{{ formatDate(tag.createdAt) }}</td>
            <td class="actions">
              <button class="action-btn" @click="viewTagDetail(tag)">详情</button>
              <button class="action-btn success" @click="openEditModal(tag)">编辑</button>
              <button class="action-btn warning" @click="openDeleteModal(tag, 'SOFT_DELETE')">软删除</button>
              <button class="action-btn danger" @click="openDeleteModal(tag, 'HARD_DELETE')">硬删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div v-if="pagination.totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="pagination.page === 0"
          @click="changePage(pagination.page - 1)"
        >上一页</button>
        <span class="page-info">
          第 {{ pagination.page + 1 }} / {{ pagination.totalPages }} 页
        </span>
        <button 
          class="page-btn" 
          :disabled="pagination.page >= pagination.totalPages - 1"
          @click="changePage(pagination.page + 1)"
        >下一页</button>
      </div>
    </div>

    <!-- Tag Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>标签详情</h3>
          <button class="close-btn" @click="closeDetailModal">&times;</button>
        </div>
        <div class="modal-body" v-if="selectedTag">
          <div class="detail-row">
            <span class="label">ID:</span>
            <span class="value">{{ selectedTag.id }}</span>
          </div>
          <div class="detail-row">
            <span class="label">名称:</span>
            <span class="value">
              <span v-if="selectedTag.color" class="tag-color-dot" :style="{ backgroundColor: selectedTag.color }"></span>
              {{ selectedTag.name }}
            </span>
          </div>
          <div class="detail-row">
            <span class="label">描述:</span>
            <span class="value">{{ selectedTag.description || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">颜色:</span>
            <span class="value">
              <span v-if="selectedTag.color" class="color-preview" :style="{ backgroundColor: selectedTag.color }">{{ selectedTag.color }}</span>
              <span v-else>-</span>
            </span>
          </div>
          <div class="detail-row">
            <span class="label">图标:</span>
            <span class="value">{{ selectedTag.icon || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">排序:</span>
            <span class="value">{{ selectedTag.sortOrder != null ? selectedTag.sortOrder : '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">文章数:</span>
            <span class="value">
              <span class="post-count-badge" :class="getPostCountClass(selectedTag.postCount)">
                {{ selectedTag.postCount }}
              </span>
            </span>
          </div>
          <!-- 关联文章列表 -->
          <div v-if="selectedTag.posts && selectedTag.posts.length > 0" class="detail-row detail-row-block">
            <span class="label">关联文章:</span>
            <div class="associated-posts-list">
              <div v-for="post in selectedTag.posts" :key="post.postId" class="associated-post-item">
                <router-link :to="`/post/${post.postId}`" class="post-link" target="_blank">
                  <span class="post-id-badge">#{{ post.postId }}</span>
                  {{ post.postTitle }}
                </router-link>
              </div>
            </div>
          </div>
          <div v-else class="detail-row">
            <span class="label">关联文章:</span>
            <span class="value text-muted">暂无关联文章</span>
          </div>
          <div class="detail-row">
            <span class="label">创建者:</span>
            <span class="value">{{ selectedTag.createdByNickname || selectedTag.createdByUsername || '-' }} (@{{ selectedTag.createdByUsername }})</span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDateTime(selectedTag.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">更新时间:</span>
            <span class="value">{{ formatDateTime(selectedTag.updatedAt) }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="openEditModalFromDetail">编辑</button>
          <button class="btn btn-warning" @click="openDeleteModalFromDetail('SOFT_DELETE')">软删除</button>
          <button class="btn btn-danger" @click="openDeleteModalFromDetail('HARD_DELETE')">硬删除</button>
          <button class="btn btn-secondary" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showFormModal" class="modal-overlay" @click.self="closeFormModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑标签' : '创建标签' }}</h3>
          <button class="close-btn" @click="closeFormModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标签名称 <span class="required">*</span></label>
            <input type="text" v-model="tagForm.name" class="form-input" placeholder="请输入标签名称">
          </div>
          <div class="form-group">
            <label>标签描述 <span class="optional">(可选)</span></label>
            <textarea v-model="tagForm.description" class="form-input form-textarea" placeholder="请输入标签描述" rows="3"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>颜色 <span class="optional">(可选)</span></label>
              <div class="color-input-group">
                <input type="color" v-model="colorPickerValue" class="color-picker" @input="onColorPick">
                <input type="text" v-model="tagForm.color" class="form-input" placeholder="#FF5733" maxlength="7">
              </div>
            </div>
            <div class="form-group">
              <label>图标 <span class="optional">(可选)</span></label>
              <div class="icon-picker-wrapper">
                <div class="icon-picker-display" tabindex="0" role="button" @click="showIconPicker = !showIconPicker" @keydown.enter.prevent="showIconPicker = !showIconPicker" @keydown.space.prevent="showIconPicker = !showIconPicker">
                  <span v-if="tagForm.icon" class="icon-preview">
                    <i :class="tagForm.icon"></i>
                    <span class="icon-class-text">{{ tagForm.icon }}</span>
                  </span>
                  <span v-else class="icon-placeholder">点击选择图标</span>
                  <span class="icon-picker-arrow">▼</span>
                </div>
                <div v-if="showIconPicker" class="icon-picker-dropdown">
                  <input type="text" v-model="iconSearchQuery" class="form-input icon-search-input" placeholder="搜索图标...">
                  <div class="icon-grid">
                    <div
                      v-for="icon in filteredIcons"
                      :key="icon.value"
                      class="icon-grid-item"
                      :class="{ 'icon-selected': tagForm.icon === icon.value }"
                      tabindex="0"
                      role="button"
                      @click="selectIcon(icon.value)"
                      @keydown.enter.prevent="selectIcon(icon.value)"
                      @keydown.space.prevent="selectIcon(icon.value)"
                      :title="icon.label"
                    >
                      <i :class="icon.value"></i>
                    </div>
                  </div>
                  <div class="icon-picker-footer">
                    <button class="btn btn-sm btn-secondary" @click="selectIcon('')">清除选择</button>
                    <button class="btn btn-sm btn-primary" @click="showIconPicker = false">确定</button>
                  </div>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>排序 <span class="optional">(可选)</span></label>
              <input type="number" v-model.number="tagForm.sortOrder" class="form-input" placeholder="数字越小越靠前" min="0">
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeFormModal">取消</button>
          <button 
            class="btn btn-primary" 
            @click="submitTagForm"
            :disabled="!tagForm.name || tagForm.name.trim() === ''"
          >{{ isEditMode ? '保存修改' : '创建标签' }}</button>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3>{{ getDeleteModalTitle() }}</h3>
          <button class="close-btn" @click="closeDeleteModal">&times;</button>
        </div>
        <div class="modal-body">
          <p class="warning-text">
            {{ isBatchDelete 
              ? `确定要${deleteAction === 'SOFT_DELETE' ? '软' : '硬'}删除选中的 ${selectedTagIds.length} 个标签吗？` 
              : `确定要${deleteAction === 'SOFT_DELETE' ? '软' : '硬'}删除此标签吗？` 
            }}
          </p>
          <p class="sub-text" v-if="deleteAction === 'SOFT_DELETE'">
            软删除仅移除标签与文章的关联关系，标签本身仍然保留。您可以选择要解除关联的文章，不选则解除所有关联。
          </p>
          <p class="sub-text" v-else>
            硬删除将永久删除标签及其与文章的所有关联，此操作不可恢复。
          </p>

          <!-- 软删除时显示关联文章选择 -->
          <div v-if="deleteAction === 'SOFT_DELETE' && !isBatchDelete && deletePostsList.length > 0" class="form-group">
            <label>选择要解除关联的文章 <span class="optional">(不选则全部解除)</span></label>
            <div class="post-selection-list">
              <div class="post-selection-header">
                <label class="checkbox-label">
                  <input type="checkbox" :checked="isAllDeletePostsSelected" @change="toggleSelectAllDeletePosts">
                  <span>全选 ({{ deletePostsList.length }} 篇文章)</span>
                </label>
                <span v-if="selectedDeletePostIds.length > 0" class="selected-info">
                  已选择 {{ selectedDeletePostIds.length }} 篇
                </span>
              </div>
              <div class="post-selection-items">
                <label v-for="post in deletePostsList" :key="post.postId" class="checkbox-label post-checkbox-item">
                  <input 
                    type="checkbox" 
                    :value="post.postId" 
                    v-model="selectedDeletePostIds"
                  >
                  <span class="post-id-badge-sm">#{{ post.postId }}</span>
                  <span class="post-checkbox-title">{{ post.postTitle }}</span>
                </label>
              </div>
            </div>
          </div>
          <div v-if="deleteAction === 'SOFT_DELETE' && !isBatchDelete && deletePostsList.length === 0 && deletePostsLoading" class="form-group">
            <p class="text-muted">加载关联文章中...</p>
          </div>
          <div v-if="deleteAction === 'SOFT_DELETE' && !isBatchDelete && deletePostsList.length === 0 && !deletePostsLoading" class="form-group">
            <p class="text-muted">此标签暂无关联文章</p>
          </div>
          
          <div class="form-group">
            <label>通知标题 <span class="optional">(可选)</span></label>
            <input type="text" v-model="deleteForm.formTitle" class="form-input" :placeholder="deleteAction === 'SOFT_DELETE' ? '标签关联移除通知' : '标签删除通知'">
          </div>
          <div class="form-group">
            <label>删除理由 <span class="required">*</span></label>
            <textarea v-model="deleteForm.reason" class="form-input form-textarea" placeholder="请填写删除理由" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>扩展信息 <span class="optional">(可选)</span></label>
            <div v-for="(field, index) in deleteForm.extraFieldsList" :key="index" class="extra-field-row">
              <input type="text" v-model="field.fieldName" class="form-input extra-field-input" placeholder="字段名">
              <input type="text" v-model="field.fieldValue" class="form-input extra-field-input" placeholder="字段值">
              <button class="btn btn-sm btn-danger" @click="removeExtraField(index)">删除</button>
            </div>
            <button class="btn btn-sm btn-secondary" @click="addExtraField">+ 添加扩展字段</button>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteModal">取消</button>
          <button 
            class="btn btn-danger" 
            @click="confirmDelete"
            :disabled="!deleteForm.reason || deleteForm.reason.trim() === ''"
          >确认{{ deleteAction === 'SOFT_DELETE' ? '软' : '硬' }}删除</button>
        </div>
      </div>
    </div>

    <!-- Quick Links -->
    <div class="quick-links">
      <router-link to="/" class="quick-link">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>
        返回前台首页
      </router-link>
      <button @click="handleLogout" class="quick-link">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
        退出登录
      </button>
    </div>
  </AdminLayout>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { getAdminTags, getAdminTagDetail, createAdminTag, updateAdminTag, executeTagAction } from '@/api/admin'
import AdminLayout from '@/layouts/AdminLayout.vue'

export default {
  name: 'AdminTagManagement',
  components: {
    AdminLayout
  },
  setup() {
    const store = useStore()
    const router = useRouter()

    // ======================= State =======================
    const tagList = ref([])
    const loading = ref(false)
    const pagination = ref({
      page: 0,
      size: 10,
      total: 0,
      totalPages: 0
    })
    const searchForm = ref({
      name: '',
      createdBy: '',
      startDate: '',
      endDate: ''
    })

    // Selection state
    const selectedTagIds = ref([])

    // Detail modal state
    const showDetailModal = ref(false)
    const selectedTag = ref(null)

    // Create/Edit modal state
    const showFormModal = ref(false)
    const isEditMode = ref(false)
    const editingTagId = ref(null)
    const colorPickerValue = ref('#1890ff')
    const tagForm = ref({
      name: '',
      description: '',
      color: '',
      icon: '',
      sortOrder: null
    })

    // Delete modal state
    const showDeleteModal = ref(false)
    const isBatchDelete = ref(false)
    const deleteAction = ref('SOFT_DELETE')
    const deleteTargetTag = ref(null)
    const deleteForm = ref({
      formTitle: '',
      reason: '',
      extraFieldsList: []
    })
    // Soft delete post selection state
    const deletePostsList = ref([])
    const deletePostsLoading = ref(false)
    const selectedDeletePostIds = ref([])

    // Expanded posts in table
    const expandedTagIds = ref([])
    const expandedPostsMap = ref({})

    // Icon picker state
    const showIconPicker = ref(false)
    const iconSearchQuery = ref('')

    // Common Font Awesome 6 icons for tags
    // Brand icons use "fa-brands" prefix, solid icons use "fa-solid" prefix
    const iconList = [
      // Solid icons (fa-solid)
      { value: 'fa-solid fa-tag', label: '标签' },
      { value: 'fa-solid fa-tags', label: '多标签' },
      { value: 'fa-solid fa-code', label: '代码' },
      { value: 'fa-solid fa-database', label: '数据库' },
      { value: 'fa-solid fa-server', label: '服务器' },
      { value: 'fa-solid fa-cloud', label: '云' },
      { value: 'fa-solid fa-globe', label: '全球' },
      { value: 'fa-solid fa-lock', label: '安全' },
      { value: 'fa-solid fa-shield-halved', label: '防护' },
      { value: 'fa-solid fa-bug', label: 'Bug' },
      { value: 'fa-solid fa-wrench', label: '工具' },
      { value: 'fa-solid fa-gear', label: '设置' },
      { value: 'fa-solid fa-gears', label: '多设置' },
      { value: 'fa-solid fa-rocket', label: '火箭' },
      { value: 'fa-solid fa-lightbulb', label: '灵感' },
      { value: 'fa-solid fa-book', label: '书籍' },
      { value: 'fa-solid fa-graduation-cap', label: '教育' },
      { value: 'fa-solid fa-paintbrush', label: '设计' },
      { value: 'fa-solid fa-palette', label: '调色板' },
      { value: 'fa-solid fa-camera', label: '相机' },
      { value: 'fa-solid fa-image', label: '图片' },
      { value: 'fa-solid fa-video', label: '视频' },
      { value: 'fa-solid fa-music', label: '音乐' },
      { value: 'fa-solid fa-gamepad', label: '游戏' },
      { value: 'fa-solid fa-heart', label: '喜爱' },
      { value: 'fa-solid fa-star', label: '星标' },
      { value: 'fa-solid fa-fire', label: '热门' },
      { value: 'fa-solid fa-bolt', label: '闪电' },
      { value: 'fa-solid fa-leaf', label: '叶子' },
      { value: 'fa-solid fa-mug-hot', label: '咖啡' },
      { value: 'fa-solid fa-pen', label: '写作' },
      { value: 'fa-solid fa-laptop-code', label: '编程' },
      { value: 'fa-solid fa-mobile-screen-button', label: '移动端' },
      { value: 'fa-solid fa-desktop', label: '桌面' },
      { value: 'fa-solid fa-microchip', label: '芯片' },
      { value: 'fa-solid fa-robot', label: '机器人' },
      { value: 'fa-solid fa-brain', label: 'AI/大脑' },
      { value: 'fa-solid fa-chart-line', label: '图表' },
      { value: 'fa-solid fa-network-wired', label: '网络' },
      { value: 'fa-solid fa-terminal', label: '终端' },
      { value: 'fa-solid fa-sitemap', label: '架构' },
      { value: 'fa-solid fa-cube', label: '模块' },
      { value: 'fa-solid fa-cubes', label: '多模块' },
      { value: 'fa-solid fa-puzzle-piece', label: '插件' },
      { value: 'fa-solid fa-flask', label: '实验' },
      { value: 'fa-solid fa-seedling', label: '成长' },
      { value: 'fa-solid fa-trophy', label: '奖杯' },
      { value: 'fa-solid fa-flag', label: '标记' },
      { value: 'fa-solid fa-bookmark', label: '收藏' },
      { value: 'fa-solid fa-compass', label: '指南' },
      { value: 'fa-solid fa-map', label: '地图' },
      // Brand icons (fa-brands)
      { value: 'fa-brands fa-java', label: 'Java' },
      { value: 'fa-brands fa-python', label: 'Python' },
      { value: 'fa-brands fa-js', label: 'JavaScript' },
      { value: 'fa-brands fa-html5', label: 'HTML5' },
      { value: 'fa-brands fa-css3-alt', label: 'CSS3' },
      { value: 'fa-brands fa-react', label: 'React' },
      { value: 'fa-brands fa-vuejs', label: 'Vue.js' },
      { value: 'fa-brands fa-angular', label: 'Angular' },
      { value: 'fa-brands fa-node-js', label: 'Node.js' },
      { value: 'fa-brands fa-docker', label: 'Docker' },
      { value: 'fa-brands fa-linux', label: 'Linux' },
      { value: 'fa-brands fa-windows', label: 'Windows' },
      { value: 'fa-brands fa-apple', label: 'Apple' },
      { value: 'fa-brands fa-android', label: 'Android' },
      { value: 'fa-brands fa-github', label: 'GitHub' },
      { value: 'fa-brands fa-git-alt', label: 'Git' },
      { value: 'fa-brands fa-php', label: 'PHP' },
      { value: 'fa-brands fa-rust', label: 'Rust' },
      { value: 'fa-brands fa-golang', label: 'Go' },
      { value: 'fa-brands fa-swift', label: 'Swift' },
      { value: 'fa-brands fa-aws', label: 'AWS' },
      { value: 'fa-brands fa-npm', label: 'npm' }
    ]

    const filteredIcons = computed(() => {
      if (!iconSearchQuery.value) return iconList
      const query = iconSearchQuery.value.toLowerCase()
      return iconList.filter(icon =>
        icon.label.toLowerCase().includes(query) ||
        icon.value.toLowerCase().includes(query)
      )
    })

    const selectIcon = (iconValue) => {
      tagForm.value.icon = iconValue
      if (iconValue) {
        showIconPicker.value = false
      }
      iconSearchQuery.value = ''
    }

    // Menu items (consistent with other admin views)
    const menuItems = [
      { id: 'dashboard', path: '/admin', label: '仪表盘', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
      { id: 'articles', path: '/admin/posts', label: '文章管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>' },
      { id: 'categories', path: '/admin/categories', label: '分类管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/></svg>' },
      { id: 'tags', path: '/admin/tags', label: '标签管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>' },
      { id: 'comments', path: '/admin/comments', label: '评论管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>' },
      { id: 'logs', path: '/admin/logs', label: '操作日志', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/></svg>' },
      { id: 'media', path: '/admin', label: '媒体库', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>' },
      { id: 'users', path: '/admin/users', label: '用户管理', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>' },
      { id: 'settings', path: '/admin', label: '系统设置', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>' }
    ]

    const isActiveRoute = (id) => {
      return id === 'tags'
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    // ======================= Helper Methods =======================

    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }

    const formatDateTime = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }

    const truncateText = (text, maxLength) => {
      if (!text) return '-'
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    const getPostCountClass = (count) => {
      if (count >= 10) return 'count-high'
      if (count >= 5) return 'count-medium'
      return 'count-low'
    }

    const getDeleteModalTitle = () => {
      const typeStr = deleteAction.value === 'SOFT_DELETE' ? '软删除' : '硬删除'
      return isBatchDelete.value ? `批量${typeStr}标签` : `${typeStr}标签`
    }

    // ======================= Data Loading =======================

    const loadTags = async () => {
      loading.value = true
      try {
        const params = {
          page: pagination.value.page,
          size: pagination.value.size,
          name: searchForm.value.name || undefined,
          createdBy: searchForm.value.createdBy || undefined,
          startDate: searchForm.value.startDate || undefined,
          endDate: searchForm.value.endDate || undefined
        }
        const response = await getAdminTags(params)
        tagList.value = response.content || []
        pagination.value.total = response.totalElements || 0
        pagination.value.totalPages = response.totalPages || 0
        // Clear selections when page changes
        selectedTagIds.value = []
      } catch (error) {
        console.error('Failed to load tags:', error)
        alert('加载标签列表失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    const searchTags = () => {
      pagination.value.page = 0
      loadTags()
    }

    const resetSearch = () => {
      searchForm.value = {
        name: '',
        createdBy: '',
        startDate: '',
        endDate: ''
      }
      pagination.value.page = 0
      loadTags()
    }

    const changePage = (newPage) => {
      pagination.value.page = newPage
      loadTags()
    }

    // ======================= Detail Modal =======================

    const viewTagDetail = async (tag) => {
      try {
        const detail = await getAdminTagDetail(tag.id)
        selectedTag.value = detail
        showDetailModal.value = true
      } catch (error) {
        console.error('Failed to load tag detail:', error)
        alert('加载标签详情失败')
      }
    }

    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedTag.value = null
    }

    // ======================= Create/Edit Modal =======================

    const openCreateModal = () => {
      isEditMode.value = false
      editingTagId.value = null
      tagForm.value = { name: '', description: '', color: '', icon: '', sortOrder: null }
      colorPickerValue.value = '#1890ff'
      showFormModal.value = true
    }

    const openEditModal = (tag) => {
      isEditMode.value = true
      editingTagId.value = tag.id
      tagForm.value = {
        name: tag.name,
        description: tag.description || '',
        color: tag.color || '',
        icon: tag.icon || '',
        sortOrder: tag.sortOrder
      }
      colorPickerValue.value = tag.color || '#1890ff'
      showFormModal.value = true
    }

    const openEditModalFromDetail = () => {
      if (!selectedTag.value) return
      const tagToEdit = selectedTag.value
      closeDetailModal()
      openEditModal(tagToEdit)
    }

    const closeFormModal = () => {
      showFormModal.value = false
      isEditMode.value = false
      editingTagId.value = null
    }

    const onColorPick = () => {
      tagForm.value.color = colorPickerValue.value
    }

    const submitTagForm = async () => {
      if (!tagForm.value.name || tagForm.value.name.trim() === '') {
        alert('请填写标签名称')
        return
      }

      const data = {
        name: tagForm.value.name.trim(),
        description: tagForm.value.description || undefined,
        color: tagForm.value.color || undefined,
        icon: tagForm.value.icon || undefined,
        sortOrder: tagForm.value.sortOrder != null ? tagForm.value.sortOrder : undefined
      }

      try {
        if (isEditMode.value) {
          await updateAdminTag(editingTagId.value, data)
          alert('标签更新成功')
        } else {
          await createAdminTag(data)
          alert('标签创建成功')
        }
        closeFormModal()
        await loadTags()
      } catch (error) {
        console.error('Failed to save tag:', error)
        alert((isEditMode.value ? '更新' : '创建') + '标签失败: ' + (error.response?.data || error.message))
      }
    }

    // ======================= Delete Modal =======================

    const openDeleteModal = async (tag, action) => {
      deleteTargetTag.value = tag
      deleteAction.value = action
      isBatchDelete.value = false
      deleteForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      deletePostsList.value = []
      selectedDeletePostIds.value = []
      showDeleteModal.value = true

      // 软删除时加载关联文章列表供选择
      if (action === 'SOFT_DELETE' && tag.postCount > 0) {
        deletePostsLoading.value = true
        try {
          const detail = await getAdminTagDetail(tag.id)
          deletePostsList.value = detail.posts || []
        } catch (error) {
          console.error('Failed to load tag posts for delete:', error)
        } finally {
          deletePostsLoading.value = false
        }
      }
    }

    const openDeleteModalFromDetail = (action) => {
      if (!selectedTag.value) return
      const tag = selectedTag.value
      closeDetailModal()
      // 如果详情中已有posts数据，直接使用
      deleteTargetTag.value = tag
      deleteAction.value = action
      isBatchDelete.value = false
      deleteForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      selectedDeletePostIds.value = []
      if (action === 'SOFT_DELETE' && tag.posts && tag.posts.length > 0) {
        deletePostsList.value = tag.posts
      } else {
        deletePostsList.value = []
      }
      showDeleteModal.value = true
    }

    const openBatchDeleteModal = (action) => {
      if (selectedTagIds.value.length === 0) return
      deleteAction.value = action
      isBatchDelete.value = true
      deleteForm.value = { formTitle: '', reason: '', extraFieldsList: [] }
      deletePostsList.value = []
      selectedDeletePostIds.value = []
      showDeleteModal.value = true
    }

    const closeDeleteModal = () => {
      showDeleteModal.value = false
      deleteTargetTag.value = null
      isBatchDelete.value = false
      deletePostsList.value = []
      selectedDeletePostIds.value = []
    }

    const isAllDeletePostsSelected = computed(() => {
      return deletePostsList.value.length > 0 && 
             deletePostsList.value.every(p => selectedDeletePostIds.value.includes(p.postId))
    })

    const toggleSelectAllDeletePosts = () => {
      if (isAllDeletePostsSelected.value) {
        selectedDeletePostIds.value = []
      } else {
        selectedDeletePostIds.value = deletePostsList.value.map(p => p.postId)
      }
    }

    const addExtraField = () => {
      deleteForm.value.extraFieldsList.push({ fieldName: '', fieldValue: '' })
    }

    const removeExtraField = (index) => {
      deleteForm.value.extraFieldsList.splice(index, 1)
    }

    const getExtraFieldsJson = () => {
      const validFields = deleteForm.value.extraFieldsList.filter(
        f => f.fieldName && f.fieldName.trim() !== ''
      )
      return validFields.length > 0 ? JSON.stringify(validFields) : null
    }

    const confirmDelete = async () => {
      if (!deleteForm.value.reason || deleteForm.value.reason.trim() === '') {
        alert('请填写删除理由')
        return
      }

      const ids = isBatchDelete.value ? selectedTagIds.value : [deleteTargetTag.value.id]
      
      // 构造请求
      const requestData = {
        action: deleteAction.value,
        tagIds: ids,
        formTitle: deleteForm.value.formTitle || undefined,
        reason: deleteForm.value.reason,
        extraFields: getExtraFieldsJson()
      }

      // 软删除时，如果选择了特定文章，则传递postIds
      if (deleteAction.value === 'SOFT_DELETE' && selectedDeletePostIds.value.length > 0) {
        requestData.postIds = selectedDeletePostIds.value
      }

      try {
        await executeTagAction(requestData)
        closeDeleteModal()
        selectedTagIds.value = []
        await loadTags()
        const typeStr = deleteAction.value === 'SOFT_DELETE' ? '软' : '硬'
        alert(`成功${typeStr}删除 ${ids.length} 个标签`)
      } catch (error) {
        console.error('Failed to delete tags:', error)
        alert('删除失败: ' + (error.response?.data || error.message))
      }
    }

    // ======================= Expand Posts in Table =======================

    // State for tracking loading status per tag
    const expandedPostsLoading = ref({})

    const isTagExpanded = (tagId) => {
      return expandedTagIds.value.includes(tagId)
    }

    const toggleExpandPosts = async (tag) => {
      if (tag.postCount === 0) return

      const idx = expandedTagIds.value.indexOf(tag.id)
      if (idx !== -1) {
        expandedTagIds.value.splice(idx, 1)
        return
      }

      // Load posts if not already loaded
      if (!expandedPostsMap.value[tag.id]) {
        expandedPostsLoading.value = { ...expandedPostsLoading.value, [tag.id]: true }
        try {
          const detail = await getAdminTagDetail(tag.id)
          expandedPostsMap.value = { ...expandedPostsMap.value, [tag.id]: detail.posts || [] }
        } catch (error) {
          console.error('Failed to load tag posts:', error)
          expandedPostsMap.value = { ...expandedPostsMap.value, [tag.id]: [] }
        } finally {
          expandedPostsLoading.value = { ...expandedPostsLoading.value, [tag.id]: false }
        }
      }
      expandedTagIds.value.push(tag.id)
    }

    // ======================= Batch Selection =======================

    const isTagSelected = (tagId) => {
      return selectedTagIds.value.includes(tagId)
    }

    const toggleTagSelection = (tagId) => {
      const index = selectedTagIds.value.indexOf(tagId)
      if (index === -1) {
        selectedTagIds.value.push(tagId)
      } else {
        selectedTagIds.value.splice(index, 1)
      }
    }

    const isAllSelected = computed(() => {
      return tagList.value.length > 0 && tagList.value.every(t => selectedTagIds.value.includes(t.id))
    })

    const isPartialSelected = computed(() => {
      const selectedCount = tagList.value.filter(t => selectedTagIds.value.includes(t.id)).length
      return selectedCount > 0 && selectedCount < tagList.value.length
    })

    const toggleSelectAll = () => {
      if (isAllSelected.value) {
        selectedTagIds.value = []
      } else {
        selectedTagIds.value = tagList.value.map(t => t.id)
      }
    }

    onMounted(() => {
      loadTags()
    })

    return {
      menuItems,
      isActiveRoute,
      handleLogout,
      // Tag list
      tagList,
      loading,
      pagination,
      searchForm,
      formatDate,
      formatDateTime,
      truncateText,
      getPostCountClass,
      searchTags,
      resetSearch,
      changePage,
      // Detail modal
      showDetailModal,
      selectedTag,
      viewTagDetail,
      closeDetailModal,
      // Create/Edit modal
      showFormModal,
      isEditMode,
      tagForm,
      colorPickerValue,
      openCreateModal,
      openEditModal,
      openEditModalFromDetail,
      closeFormModal,
      onColorPick,
      submitTagForm,
      // Icon picker
      showIconPicker,
      iconSearchQuery,
      filteredIcons,
      selectIcon,
      // Delete modal
      showDeleteModal,
      isBatchDelete,
      deleteAction,
      deleteForm,
      deletePostsList,
      deletePostsLoading,
      selectedDeletePostIds,
      isAllDeletePostsSelected,
      toggleSelectAllDeletePosts,
      getDeleteModalTitle,
      openDeleteModal,
      openDeleteModalFromDetail,
      openBatchDeleteModal,
      closeDeleteModal,
      addExtraField,
      removeExtraField,
      confirmDelete,
      // Expand posts in table
      expandedPostsMap,
      expandedPostsLoading,
      isTagExpanded,
      toggleExpandPosts,
      // Batch selection
      selectedTagIds,
      isTagSelected,
      toggleTagSelection,
      isAllSelected,
      isPartialSelected,
      toggleSelectAll
    }
  }
}
</script>

<style scoped>

/* Cards */
.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1d2e;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

/* Search Card */
.search-card .search-form {
  padding: 20px 24px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-row .form-group {
  flex: 1;
  min-width: 180px;
  margin-bottom: 0;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
}

.color-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.color-picker {
  width: 40px;
  height: 40px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  padding: 2px;
}

.extra-field-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.extra-field-input {
  flex: 1;
}

.search-btn-group {
  display: flex;
  align-items: flex-end;
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: #1890ff;
  color: #fff;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-primary:disabled {
  background: #91d5ff;
  cursor: not-allowed;
}

.btn-secondary {
  background: #fff;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-success {
  background: #52c41a;
  color: #fff;
}

.btn-success:hover {
  background: #73d13d;
}

.btn-danger {
  background: #f5222d;
  color: #fff;
}

.btn-danger:hover {
  background: #ff4d4f;
}

.btn-danger:disabled {
  background: #ffccc7;
  cursor: not-allowed;
}

.btn-warning {
  background: #faad14;
  color: #fff;
}

.btn-warning:hover {
  background: #ffc53d;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

/* Data Table */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e8e8e8;
  font-size: 14px;
}

.data-table th {
  background: #fafafa;
  font-weight: 600;
  color: #666;
}

.data-table td {
  color: #333;
}

.data-table .checkbox-col {
  width: 40px;
  text-align: center;
}

.data-table .checkbox-col input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.selected-row {
  background-color: #e6f7ff;
}

.desc-col {
  max-width: 200px;
  color: #666;
}

.tag-name-cell {
  cursor: pointer;
  color: #1890ff;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tag-name-cell:hover {
  text-decoration: underline;
}

.tag-color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.color-preview {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-family: monospace;
}

.text-muted {
  color: #999;
}

/* Post count badge */
.post-count-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}

.count-high {
  background: #fff2f0;
  color: #f5222d;
}

.count-medium {
  background: #fffbe6;
  color: #faad14;
}

.count-low {
  background: #f6ffed;
  color: #52c41a;
}

/* Badges */
.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background: #f6ffed;
  color: #52c41a;
}

.badge-danger {
  background: #fff2f0;
  color: #f5222d;
}

.badge-warning {
  background: #fffbe6;
  color: #faad14;
}

.badge-info {
  background: #e6f7ff;
  color: #1890ff;
}

.badge-default {
  background: #fafafa;
  color: #666;
}

/* Action Buttons */
.actions {
  white-space: nowrap;
}

.action-btn {
  padding: 4px 8px;
  margin-right: 4px;
  border: none;
  background: transparent;
  color: #1890ff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #e6f7ff;
  border-radius: 4px;
}

.action-btn.danger {
  color: #f5222d;
}

.action-btn.danger:hover {
  background: #fff2f0;
}

.action-btn.success {
  color: #52c41a;
}

.action-btn.success:hover {
  background: #f6ffed;
}

.action-btn.warning {
  color: #faad14;
}

.action-btn.warning:hover {
  background: #fffbe6;
}

/* Loading & Empty */
.loading-container {
  padding: 40px;
  text-align: center;
}

.loading-spinner {
  color: #666;
  font-size: 14px;
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 40px !important;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #e8e8e8;
}

.page-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.page-btn:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-lg {
  width: 700px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  width: 100px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
}

.detail-row .value {
  flex: 1;
  color: #333;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.warning-text {
  color: #f5222d;
  font-weight: 500;
  margin-bottom: 8px;
}

.sub-text {
  color: #666;
  font-size: 13px;
  margin-bottom: 16px;
}

.required {
  color: #f5222d;
}

.optional {
  color: #999;
  font-weight: normal;
}

/* Quick Links */
.quick-links {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.quick-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-link:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.quick-link svg {
  width: 18px;
  height: 18px;
}

/* Icon Picker */
.icon-picker-wrapper {
  position: relative;
}

.icon-picker-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  background: #fff;
  transition: border-color 0.2s;
}

.icon-picker-display:hover {
  border-color: #1890ff;
}

.icon-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-preview i {
  font-size: 18px;
  color: #333;
}

.icon-class-text {
  font-size: 13px;
  color: #666;
  font-family: monospace;
}

.icon-placeholder {
  color: #999;
  font-size: 14px;
}

.icon-picker-arrow {
  color: #999;
  font-size: 10px;
}

.icon-picker-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 360px;
  display: flex;
  flex-direction: column;
}

.icon-search-input {
  margin: 8px;
  width: calc(100% - 16px) !important;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 8px;
  overflow-y: auto;
  max-height: 240px;
}

.icon-grid-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 18px;
  color: #555;
}

.icon-grid-item:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.icon-grid-item.icon-selected {
  background: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.icon-picker-footer {
  display: flex;
  justify-content: space-between;
  padding: 8px;
  border-top: 1px solid #e8e8e8;
}

/* Associated posts in detail modal */
.detail-row-block {
  flex-direction: column;
  gap: 8px;
}

.detail-row-block .label {
  width: auto;
}

.associated-posts-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 4px;
}

.associated-post-item {
  padding: 4px 8px;
  border-radius: 4px;
}

.associated-post-item:hover {
  background: #f5f5f5;
}

.post-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
}

.post-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

.post-id-badge {
  display: inline-block;
  background: #e6f7ff;
  color: #1890ff;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

/* Expanded posts in table */
.post-count-badge.clickable {
  cursor: pointer;
}

.post-count-badge.clickable:hover {
  opacity: 0.8;
}

.expand-arrow {
  font-size: 10px;
  margin-left: 4px;
}

.expanded-posts {
  margin-top: 6px;
  padding: 6px 0;
  border-top: 1px solid #f0f0f0;
}

.expanded-post-item {
  padding: 2px 0;
}

.post-link-sm {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #1890ff;
  text-decoration: none;
  font-size: 12px;
}

.post-link-sm:hover {
  color: #40a9ff;
  text-decoration: underline;
}

.post-id-badge-sm {
  display: inline-block;
  background: #e6f7ff;
  color: #1890ff;
  padding: 0 4px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
}

/* Post selection in delete modal */
.post-selection-list {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.post-selection-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.selected-info {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.post-selection-items {
  max-height: 200px;
  overflow-y: auto;
  padding: 4px 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}

.post-checkbox-item {
  padding: 6px 12px;
  transition: background 0.15s;
}

.post-checkbox-item:hover {
  background: #f5f5f5;
}

.post-checkbox-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
