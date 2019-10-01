/* eslint-disable import/first */
import {
  shallowMount,
} from '@vue/test-utils';

const mockSocksJSClient = {
  connected: true,
  close: jest.fn(),
};

jest.mock('sockjs-client', () => jest.fn().mockImplementation(() => mockSocksJSClient));

const mockStompConnect = jest.fn();
const mockStompSubscribe = jest.fn();
const mockStompSend = jest.fn();

jest.mock('webstomp-client', () => ({
  over: jest.fn().mockImplementation(() => ({
    connect: mockStompConnect,
    subscribe: mockStompSubscribe,
    send: mockStompSend,
  })),
  connected: true,
}));

jest.mock('lodash-es', () => ({
  debounce: jest.fn(fn => fn),
}));

import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';

import MonacoEditor from 'vue-monaco';
import * as monaco from 'monaco-editor';

import CodeEditor from '@/components/CodeEditor.vue';

const mockSetModelMarkers = jest.fn();
const mockEditorLayout = jest.fn();
const moctMonaco = {
  editor: {
    layout: mockEditorLayout,
    setModelMarkers: mockSetModelMarkers,
    getModel: jest.fn(() => ({
      isModel: true,
    })),
  },
  MarkerSeverity: monaco.MarkerSeverity,
};
const mockEditor = moctMonaco.editor;

describe('CodeEditor.vue', () => {
  // Workaround to let IDEs have the correct type for the variable
  let wrapper = shallowMount(CodeEditor);
  wrapper.destroy();

  beforeEach(() => {
    jest.clearAllMocks();

    wrapper = shallowMount(CodeEditor, {
      propsData: {
        initialCode: 'test code',
      },
    });

    wrapper.vm.$refs.editor = {
      monaco: moctMonaco,
      getEditor() {
        return mockEditor;
      },
    };
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it('renders', () => {
    expect(wrapper.exists).toBeTruthy();
  });

  it('connects to a websocket', () => {
    expect(SockJS).toHaveBeenCalledWith(`${process.env.VUE_APP_API_HOST}/socket`);
  });

  it('creates STOMP client', () => {
    expect(Stomp.over).toHaveBeenCalledWith(expect.anything(), {
      debug: expect.any(Boolean),
    });
    expect(mockStompConnect).toHaveBeenCalledTimes(1);
    expect(typeof mockStompConnect.mock.calls[0][1]).toBe('function');
    expect(wrapper.vm.$data.connected).toBeFalsy();
    mockStompConnect.mock.calls[0][1]();
    expect(wrapper.vm.$data.connected).toBeTruthy();
  });

  it('passes prop to code editor', () => {
    expect(wrapper.find(MonacoEditor).props().value).toStrictEqual('test code');
  });

  it('loads the code editor', () => {
    expect(wrapper.vm.loading).toBeTruthy();
    wrapper.vm.onMountedEditor();
    expect(wrapper.vm.loading).toBeFalsy();
    expect(wrapper.vm.monaco).toBeDefined();
    expect(wrapper.vm.editor).toBeDefined();
  });

  it('resizes the code editor on resize event', () => {
    wrapper.vm.onMountedEditor();

    global.dispatchEvent(new Event('resize'));

    expect(mockEditorLayout).toBeCalled();
  });

  describe('after connecting', () => {
    beforeEach(() => {
      mockStompConnect.mock.calls[0][1]();
    });

    it('sends updates to the server', () => {
      expect(mockStompSend).not.toHaveBeenCalled();
      wrapper.setData({
        code: 'different code',
      });
      expect(mockStompSend).toHaveBeenCalledWith(
        '/app/compile', JSON.stringify({
          sources: ['different code'],
        }),
      );
    });

    it('sends code to the server to run', () => {
      expect(mockStompSend).not.toHaveBeenCalled();
      wrapper.vm.run();
      expect(mockStompSend).toHaveBeenCalledWith(
        '/app/run', JSON.stringify({
          sources: ['test code'],
        }),
      );
    });

    it('sends code to be run after clicking the button', () => {
      expect(mockStompSend).not.toHaveBeenCalled();
      wrapper.find('#run-code').trigger('click');
      expect(mockStompSend).toHaveBeenCalledWith(
        '/app/run', JSON.stringify({
          sources: ['test code'],
        }),
      );
    });

    it('clears console output when sending code to run', () => {
      wrapper.setData({
        remoteOutput: 'test output',
      });
      wrapper.vm.run();
      expect(wrapper.vm.remoteOutput).toHaveLength(0);
    });

    it('appends input to output console when sending the input', () => {
      wrapper.setData({
        remoteOutput: 'test output',
        input: 'test input',
      });
      wrapper.vm.sendInput();
      expect(wrapper.vm.remoteOutput).toMatch(/.*test input\n$/);
    });

    it('sends the current input to the server', () => {
      wrapper.setData({
        input: 'test input',
      });
      wrapper.vm.sendInput();
      expect(mockStompSend).toHaveBeenCalledWith(
        '/app/runner/input', JSON.stringify({
          input: 'test input\n',
        }),
      );
    });

    describe('and receiving a message', () => {
      let channelToFunction;

      beforeEach(() => {
        channelToFunction = new Map(mockStompSubscribe.mock.calls.map(args => [args[
          0], args[1]]));
      });

      it('clears the errors from the empty compiler output', () => {
        wrapper.vm.onMountedEditor();

        channelToFunction.get('/user/topic/compile')({
          body: JSON.stringify({
            results: [],
          }),
        });

        expect(mockSetModelMarkers).toHaveBeenCalledWith(expect.objectContaining({
          isModel: true,
        }), expect.any(String), []);
      });

      it('sets the errors from the compiler output', () => {
        wrapper.vm.onMountedEditor();

        channelToFunction.get('/user/topic/compile')({
          body: JSON.stringify({
            results: [{
              startLineNumber: 1,
              startColumnNumber: 3,
              endLineNumber: 2,
              endColumnNumber: 5,
              severity: 'ERROR',
              message: 'test error',
            }],
          }),
        });

        const expectedAdaptedError = [{
          startLineNumber: 1,
          startColumn: 3,
          endLineNumber: 2,
          endColumn: 5,
          message: 'test error',
          severity: monaco.MarkerSeverity.Error,
        }];

        expect(mockSetModelMarkers).toHaveBeenCalledWith(expect.objectContaining({
          isModel: true,
        }), expect.any(String), expectedAdaptedError);
      });

      it('appends the output from the status packet', () => {
        wrapper.setData({
          remoteOutput: 'test output ',
        });

        channelToFunction.get('/user/topic/runner/status')({
          body: JSON.stringify({
            output: 'more output',
          }),
        });

        expect(wrapper.vm.remoteOutput).toStrictEqual('test output more output');
      });

      it('appends the error output from the status packet', () => {
        wrapper.setData({
          remoteOutput: 'test output ',
        });

        channelToFunction.get('/user/topic/runner/status')({
          body: JSON.stringify({
            errorOutput: 'error output',
          }),
        });

        expect(wrapper.vm.remoteOutput).toStrictEqual('test output error output');
      });

      it('changes the status from the status packet', () => {
        wrapper.setData({
          remoteStatus: 'test status',
        });

        channelToFunction.get('/user/topic/runner/status')({
          body: JSON.stringify({
            status: 'new test status',
          }),
        });

        expect(wrapper.vm.remoteStatus).toStrictEqual('new test status');
      });
    });
  });
});
