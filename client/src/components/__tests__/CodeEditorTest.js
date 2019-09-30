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

jest.mock('webstomp-client', () => ({
  over: jest.fn().mockImplementation(() => ({
    connect: mockStompConnect,
    subscribe: mockStompSubscribe,
  })),
  connected: true,
}));

import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';

import CodeEditor from '@/components/CodeEditor.vue';

describe('CodeEditor.vue', () => {
  let wrapper = shallowMount(CodeEditor);

  beforeEach(() => {
    SockJS.mockClear();
    mockStompConnect.mockClear();

    wrapper = shallowMount(CodeEditor);
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
});
