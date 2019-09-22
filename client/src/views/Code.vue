<template>
  <div class="code">
    <MonacoEditor
      ref="editor"
      class="editor"
      v-model="code"
      language="java"
      theme="vs-dark"
      :options="editorOptions"
      @editorDidMount="onMountedEditor"
    />
  </div>
</template>

<script>
import MonacoEditor from 'vue-monaco';

export default {
  components: {
    MonacoEditor,
  },
  mounted() {
    window.addEventListener('resize', this.onResize);
  },

  beforeDestroy() {
    window.removeEventListener('resize', this.onResize);
  },
  data() {
    return {
      loading: true,
      code: [
        'public class Test {',
        ' public static void main(String[] args) {',
        '    System.out.println("Hello World!");',
        ' }',
        '}',
      ].join('\n'),
      editorOptions: {
        scrollBeyondLastLine: false,
        roundedSelection: false,
        glyphMargin: true,
      },
      decorations: [],
    };
  },
  methods: {
    onResize() {
      this.editor.layout();
    },
    onMountedEditor() {
      this.monaco = this.$refs.editor.monaco;
      this.loading = false;
      this.editor = this.$refs.editor.getEditor();
      const markers = [
        {
          startLineNumber: 1,
          startColumn: 4,
          endLineNumber: 1,
          endColumn: 8,
          message: 'Error',
          severity: this.monaco.MarkerSeverity.Error,
        },
      ];
      this.monaco.editor.setModelMarkers(
        this.editor.getModel(),
        'compiler',
        markers,
      );
      const errors = [
        {
          range: new this.monaco.Range(2, 1, 2, 1),
          options: {
            glyphMarginClassName: 'warningIcon',
            glyphMarginHoverMessage: { value: 'Side Warning' },
          },
        },
        {
          range: new this.monaco.Range(3, 1, 3, 1),
          options: {
            glyphMarginClassName: 'errorIcon',
            glyphMarginHoverMessage: { value: 'Side Error' },
          },
        },
      ];
      this.decorations = this.editor.deltaDecorations(this.decorations, errors);
      this.decorations = this.editor.deltaDecorations(this.decorations, errors);
    },
  },
};
</script>

<style scoped>
.code {
  text-align: left;
  flex: 1 1 auto;
  display: flex;
  min-height: 0;
  min-width: 0;
}

.editor {
  flex: 1 1 auto;
  min-height: 0;
  min-width: 0;
}

::v-deep .errorIcon {
  display: block;
  background-image: url("../assets/icons/error_32px.png");
  background-size: contain;
  background-repeat: no-repeat;
}

::v-deep .warningIcon {
  display: block;
  background-image: url("../assets/icons/warning_32px.png");
  background-size: contain;
  background-repeat: no-repeat;
}
</style>
