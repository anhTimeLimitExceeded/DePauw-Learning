const MonacoEditorPlugin = require('monaco-editor-webpack-plugin');

module.exports = {
  configureWebpack() {
    return {
      plugins: [
        new MonacoEditorPlugin({
          languages: ['java'],
        }),
      ],
    };
  },
  devServer: {
    host: 'localhost',
    port: 3000,
  },
};
