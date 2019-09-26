<template>
  <div class="code">
    <div>
      <span>{{ status }} to Java server</span>
    </div>
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
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { debounce } from 'lodash-es';

export default {
  components: {
    MonacoEditor,
  },
  created() {
    this.socket = new SockJS(`${process.env.VUE_APP_API_HOST}/socket`);
    this.socket.onopen = () => {
      this.stompClient = Stomp.over(this.socket, { debug: false });
      this.stompClient.connect(
        {},
        () => {
          this.connected = true;
          this.stompClient.subscribe('/user/topic/compile', (tick) => {
            this.convertErrors(JSON.parse(tick.body));
          });
        },
        (error) => {
          console.log(error);
          this.connected = false;
        },
      );
      this.socket.onopen();
    };
  },
  mounted() {
    window.addEventListener('resize', this.onResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize);

    if (this.stompClient.connected) {
      this.stompClient.disconnect();
    } else if (this.socket.readyState === this.socket.OPEN) {
      this.socket.close();
    }
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
        glyphMargin: false,
      },
      decorations: [],

      connected: false,
    };
  },
  computed: {
    status() {
      if (this.connected) {
        return 'Connected';
      }
      return 'Not Connected';
    },
  },
  watch: {
    // eslint-disable-next-line func-names
    code: debounce(function () { this.compile(); }, 500),
  },
  methods: {
    onResize() {
      this.editor.layout();
    },
    onMountedEditor() {
      this.monaco = this.$refs.editor.monaco;
      this.loading = false;
      this.editor = this.$refs.editor.getEditor();
      /*
      // Example code to add errors and warnigns to the side of the editor.

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
      */
    },
    compile() {
      if (this.connected) {
        this.stompClient.send('/app/compile', JSON.stringify({ sources: [this.code] }), {});
      }
    },
    convertErrors(errors) {
      const markers = errors.results.map((error) => {
        const severity = error.severity === 'WARNING' ? this.monaco.MarkerSeverity.Warning : this.monaco.MarkerSeverity.Error;
        return {
          startLineNumber: error.startLineNumber,
          startColumn: error.startColumnNumber,
          endLineNumber: error.endLineNumber,
          endColumn: error.endColumnNumber,
          message: error.message,
          severity,
        };
      });
      this.monaco.editor.setModelMarkers(
        this.editor.getModel(),
        'compiler',
        markers,
      );
    },
  },
};
</script>

<style scoped>
.code {
  text-align: left;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
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
