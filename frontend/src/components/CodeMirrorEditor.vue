<template>
  <div ref="editorContainer" class="codemirror-wrapper"></div>
</template>

<script>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { EditorView, keymap } from '@codemirror/view'
import { EditorState } from '@codemirror/state'
import { defaultKeymap, indentWithTab } from '@codemirror/commands'
import { markdown } from '@codemirror/lang-markdown'
import { html } from '@codemirror/lang-html'

export default {
  name: 'CodeMirrorEditor',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    contentType: {
      type: String,
      default: 'MARKDOWN'
    },
    placeholder: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editorContainer = ref(null)
    let editorView = null

    const createEditor = () => {
      if (!editorContainer.value) return

      const language = props.contentType === 'MARKDOWN' ? markdown() : html()

      const state = EditorState.create({
        doc: props.modelValue,
        extensions: [
          language,
          keymap.of([...defaultKeymap, indentWithTab]),
          EditorView.lineWrapping,
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              const newValue = update.state.doc.toString()
              emit('update:modelValue', newValue)
            }
          }),
          EditorView.theme({
            '&': {
              fontSize: '14px',
              height: '384px', // h-96 equivalent
              fontFamily: "'Monaco', 'Menlo', 'Ubuntu Mono', monospace"
            },
            '.cm-content': {
              padding: '0',
              caretColor: '#0ea5e9'
            },
            '.cm-line': {
              padding: '0 4px',
              lineHeight: '1.6'
            },
            '&.cm-focused': {
              outline: 'none'
            },
            '.cm-scroller': {
              overflow: 'auto',
              fontFamily: "'Monaco', 'Menlo', 'Ubuntu Mono', monospace"
            },
            '.cm-cursor': {
              borderLeftColor: '#0ea5e9'
            }
          })
        ]
      })

      editorView = new EditorView({
        state,
        parent: editorContainer.value
      })
    }

    const destroyEditor = () => {
      if (editorView) {
        editorView.destroy()
        editorView = null
      }
    }

    const insertText = (before, after, placeholder) => {
      if (!editorView) return

      const state = editorView.state
      const selection = state.selection.main
      const selectedText = state.doc.sliceString(selection.from, selection.to)
      const textToInsert = selectedText || placeholder

      const newText = before + textToInsert + after
      const transaction = state.update({
        changes: { from: selection.from, to: selection.to, insert: newText },
        selection: {
          anchor: selection.from + before.length,
          head: selection.from + before.length + textToInsert.length
        }
      })

      editorView.dispatch(transaction)
      editorView.focus()
    }

    // Watch for content type changes
    watch(() => props.contentType, () => {
      const currentValue = editorView?.state.doc.toString() || props.modelValue
      destroyEditor()
      createEditor()
      // Restore the value after recreating the editor
      if (editorView && currentValue) {
        const transaction = editorView.state.update({
          changes: { from: 0, to: editorView.state.doc.length, insert: currentValue }
        })
        editorView.dispatch(transaction)
      }
    })

    // Watch for external value changes (but not from the editor itself)
    watch(() => props.modelValue, (newValue) => {
      if (editorView) {
        const currentValue = editorView.state.doc.toString()
        if (currentValue !== newValue) {
          const transaction = editorView.state.update({
            changes: { from: 0, to: editorView.state.doc.length, insert: newValue }
          })
          editorView.dispatch(transaction)
        }
      }
    })

    onMounted(() => {
      createEditor()
    })

    onBeforeUnmount(() => {
      destroyEditor()
    })

    return {
      editorContainer,
      insertText
    }
  }
}
</script>

<style scoped>
.codemirror-wrapper {
  @apply w-full border-none outline-none;
}

:deep(.cm-editor) {
  @apply w-full;
}

:deep(.cm-scroller) {
  @apply overflow-auto;
}

:deep(.cm-content) {
  @apply outline-none;
}
</style>
