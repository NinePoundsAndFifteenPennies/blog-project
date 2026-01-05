<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]" @click.self="handleClose">
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col mx-4">
      <!-- Header -->
      <div class="flex items-center justify-between p-4 border-b border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800">创建表格</h2>
        <button
          @click="handleClose"
          class="p-1 hover:bg-gray-100 rounded transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="flex flex-1 overflow-hidden">
        <!-- Left Panel: Controls -->
        <div class="w-64 p-4 border-r border-gray-200 bg-gray-50 flex-shrink-0">
          <div class="space-y-4">
            <!-- Row Control -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                行数
              </label>
              <input
                type="number"
                min="2"
                max="20"
                :value="rows"
                @input="handleRowsChange"
                class="w-full px-3 py-2 mb-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
              <input
                type="range"
                min="2"
                max="20"
                :value="rows"
                @input="handleRowsChange"
                class="w-full"
              />
            </div>

            <!-- Column Control -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                列数
              </label>
              <input
                type="number"
                min="2"
                max="20"
                :value="cols"
                @input="handleColsChange"
                class="w-full px-3 py-2 mb-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
              <input
                type="range"
                min="2"
                max="20"
                :value="cols"
                @input="handleColsChange"
                class="w-full"
              />
            </div>

            <!-- Format Info (read-only, based on contentType) -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                格式
              </label>
              <div class="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-100 text-gray-700">
                {{ format === 'markdown' ? 'Markdown' : 'HTML' }}
              </div>
              <p class="mt-1 text-xs text-gray-500">
                根据当前编辑器内容类型自动选择
              </p>
            </div>

            <!-- Confirm Button -->
            <button
              @click="handleConfirm"
              class="w-full px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
            >
              确认插入
            </button>
          </div>
        </div>

        <!-- Right Panel: Preview Table -->
        <div class="flex-1 p-4 overflow-auto">
          <h3 class="text-sm font-medium text-gray-700 mb-3">表格预览（可编辑）</h3>
          <div class="overflow-x-auto">
            <table class="w-full border-collapse border border-gray-300">
              <thead>
                <tr class="bg-gray-100">
                  <th 
                    v-for="(cell, colIndex) in tableData[0]" 
                    :key="colIndex" 
                    class="border border-gray-300 p-2"
                  >
                    <input
                      type="text"
                      :value="cell"
                      @input="updateCell(0, colIndex, $event.target.value)"
                      class="w-full px-2 py-1 text-center font-semibold bg-transparent focus:outline-none focus:ring-2 focus:ring-primary-500 rounded"
                    />
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(row, rowIndex) in tableData.slice(1)" 
                  :key="rowIndex" 
                  class="hover:bg-gray-50"
                >
                  <td 
                    v-for="(cell, colIndex) in row" 
                    :key="colIndex" 
                    class="border border-gray-300 p-2"
                  >
                    <input
                      type="text"
                      :value="cell"
                      @input="updateCell(rowIndex + 1, colIndex, $event.target.value)"
                      class="w-full px-2 py-1 text-center bg-transparent focus:outline-none focus:ring-2 focus:ring-primary-500 rounded"
                    />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, toRefs } from 'vue'

export default {
  name: 'TableEditorModal',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    },
    format: {
      type: String,
      default: 'markdown',
      validator: (value) => ['markdown', 'html'].includes(value)
    }
  },
  emits: ['close', 'insert'],
  setup(props, { emit }) {
    const { isOpen, format } = toRefs(props)
    
    const rows = ref(3)
    const cols = ref(3)
    const tableData = ref([])

    // Initialize table data
    const initTableData = (r, c) => {
      const data = []
      for (let i = 0; i < r; i++) {
        const row = []
        for (let j = 0; j < c; j++) {
          row.push(i === 0 ? `标题${j + 1}` : `内容${i}-${j + 1}`)
        }
        data.push(row)
      }
      return data
    }

    // Handle size changes while preserving existing data
    const handleSizeChange = (newRows, newCols) => {
      rows.value = newRows
      cols.value = newCols
      
      const newData = []
      for (let i = 0; i < newRows; i++) {
        const row = []
        for (let j = 0; j < newCols; j++) {
          // Preserve existing data, fill new cells with defaults
          if (tableData.value[i] && tableData.value[i][j] !== undefined) {
            row.push(tableData.value[i][j])
          } else {
            row.push(i === 0 ? `标题${j + 1}` : `内容${i}-${j + 1}`)
          }
        }
        newData.push(row)
      }
      tableData.value = newData
    }

    const handleRowsChange = (event) => {
      const val = parseInt(event.target.value) || 2
      handleSizeChange(Math.min(Math.max(val, 2), 20), cols.value)
    }

    const handleColsChange = (event) => {
      const val = parseInt(event.target.value) || 2
      handleSizeChange(rows.value, Math.min(Math.max(val, 2), 20))
    }

    // Update individual cell
    const updateCell = (rowIndex, colIndex, value) => {
      const newData = tableData.value.map((row, rIdx) => 
        rIdx === rowIndex 
          ? row.map((cell, cIdx) => cIdx === colIndex ? value : cell)
          : [...row]
      )
      tableData.value = newData
    }

    // Escape pipe and backslash characters for Markdown table cells
    const escapeMarkdownCell = (text) => {
      // First escape backslashes, then escape pipes
      return text.replace(/\\/g, '\\\\').replace(/\|/g, '\\|')
    }

    // Escape HTML special characters to prevent XSS
    const escapeHTML = (text) => {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    }

    // Generate Markdown table
    const generateMarkdown = (data) => {
      if (data.length === 0) return ''
      
      let markdown = ''
      
      // First row (header)
      markdown += '| ' + data[0].map(escapeMarkdownCell).join(' | ') + ' |\n'
      
      // Separator
      markdown += '| ' + data[0].map(() => '---').join(' | ') + ' |\n'
      
      // Data rows
      for (let i = 1; i < data.length; i++) {
        markdown += '| ' + data[i].map(escapeMarkdownCell).join(' | ') + ' |\n'
      }
      
      return markdown
    }

    // Generate HTML table
    const generateHTML = (data) => {
      if (data.length === 0) return ''
      
      let html = '<table>\n'
      
      // Header
      html += '  <thead>\n    <tr>\n'
      data[0].forEach(cell => {
        html += `      <th>${escapeHTML(cell)}</th>\n`
      })
      html += '    </tr>\n  </thead>\n'
      
      // Body
      if (data.length > 1) {
        html += '  <tbody>\n'
        for (let i = 1; i < data.length; i++) {
          html += '    <tr>\n'
          data[i].forEach(cell => {
            html += `      <td>${escapeHTML(cell)}</td>\n`
          })
          html += '    </tr>\n'
        }
        html += '  </tbody>\n'
      }
      
      html += '</table>'
      
      return html
    }

    // Handle confirm
    const handleConfirm = () => {
      const result = format.value === 'markdown' 
        ? generateMarkdown(tableData.value)
        : generateHTML(tableData.value)
      emit('insert', result)
      emit('close')
    }

    // Handle close
    const handleClose = () => {
      emit('close')
    }

    // Initialize/reset table data when modal opens
    watch(isOpen, (newValue) => {
      if (newValue) {
        tableData.value = initTableData(rows.value, cols.value)
      }
    }, { immediate: true })

    return {
      rows,
      cols,
      tableData,
      handleRowsChange,
      handleColsChange,
      updateCell,
      handleConfirm,
      handleClose
    }
  }
}
</script>

<style scoped>
/* Custom styles for range input */
input[type="range"] {
  -webkit-appearance: none;
  appearance: none;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  outline: none;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  background: #4f46e5;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

input[type="range"]::-webkit-slider-thumb:hover {
  background: #4338ca;
}

input[type="range"]::-moz-range-thumb {
  width: 16px;
  height: 16px;
  background: #4f46e5;
  border-radius: 50%;
  cursor: pointer;
  border: none;
}

input[type="range"]::-moz-range-thumb:hover {
  background: #4338ca;
}

/* Table cell input styling */
table input {
  min-width: 60px;
}
</style>
