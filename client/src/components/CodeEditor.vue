<template>
  <div class="code-editor">
    <div>
      <span>{{ status }} to Java server</span>
      <button id="run-code" @click.stop="run">Run code</button>
    </div>
    <div class="editor-wrapper">
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
    <div class="output">
      <div class="status">
        <span>Status: {{ remoteStatus }}</span>
        <div class="input">
          <input v-model="input" v-on:keyup.enter="sendInput" placeholder="code input" />
          <button @click.stop="sendInput">Send Input</button>
        </div>
      </div>
      <textarea :readonly="true" :value="remoteOutput"></textarea>
    </div>
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
    this.stompClient = Stomp.over(this.socket, { debug: false });
    this.stompClient.connect(
      {},
      () => {
        this.connected = true;
        this.stompClient.subscribe('/user/topic/compile', (tick) => {
          this.convertErrors(JSON.parse(tick.body));
        });
        this.stompClient.subscribe('/user/topic/runner/status', (tick) => {
          const body = JSON.parse(tick.body);
          if (body.output) {
            this.remoteOutput += body.output;
          }
          if (body.errorOutput) {
            this.remoteOutput += body.errorOutput;
          }
          if (body.status) {
            this.remoteStatus = body.status;
          }
        });
      },
      (error) => {
        console.log(error);
        this.connected = false;
      },
    );
  },
  mounted() {
    window.addEventListener('resize', this.onResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize);

    if (this.connected) {
      if (this.stompClient.connected) {
        this.stompClient.disconnect();
      } else if (this.socket.readyState === this.socket.OPEN) {
        this.socket.close();
      }
    }
  },
  props: {
    initialCode: {
      default: [
        'public class Test {',
        ' public static void main(String[] args) {',
        '    System.out.println("Hello World!");',
        ' }',
        '}',
      ].join('\n'),
      type: String,
    },
  },
  data() {
    return {
      loading: true,
      code: this.initialCode,
      remoteOutput: '',
      remoteStatus: 'STOPPED',
      input: '',
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
    code: debounce(function () {
      this.compile();
    }, 500),
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
        this.stompClient.send(
          '/app/compile',
          JSON.stringify({ sources: [this.code] }),
        );
      }
    },
    run() {
      if (this.connected) {
        this.stompClient.send(
          '/app/run',
          JSON.stringify({ sources: [this.code] }),
        );
        this.remoteOutput = '';
        this.sendInput();
      }
    },
    sendInput() {
      if (this.connected && this.input !== '') {
        this.input += '\n';
        this.remoteOutput += this.input;
        this.stompClient.send(
          '/app/runner/input',
          JSON.stringify({ input: this.input }),
        );
        this.input = '';
      }
    },
    convertErrors(errors) {
      const markers = errors.results.map((error) => {
        const severity = error.severity === 'WARNING'
          ? this.monaco.MarkerSeverity.Warning
          : this.monaco.MarkerSeverity.Error;
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
.code-editor {
  text-align: left;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
}

.editor-wrapper {
  flex: 1 1 auto;
  display: flex;
  min-height: 350px;
  min-width: 100px;
}

.editor {
  flex: 1 1 auto;
  min-height: 0;
  min-width: 0;
}

.output .status {
  display: flex;
  flex-direction: row;
}

.output .status .input {
  flex: 1 1 auto;
  display: flex;
  flex-direction: row;
}

.output .status .input input {
  flex: 1 1 auto;
}

.output textarea {
  resize: none;
  height: 10vh;
  width: 100%;
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
