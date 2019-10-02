<template>
  <div class="code">
    <CodeEditor />
  </div>
</template>

<script>
import CodeEditor from '@/components/CodeEditor.vue';

export default {
  components: {
    CodeEditor,
  },
};
</script>

<style scoped>
.code {
  flex: 1 1 auto;
  min-height: 0;
  min-width: 0;
  display: flex;
}
</style>
