<template>
  <div class="ping">
    <input v-model="pingMessage" placeholder="Message to ping" @keyup.enter="sendHTTP" />
    <button @click.stop="sendHTTP">Send with HTTP</button>
    <button @click.stop="sendSocket" :disabled="!connected">Send with WebSockets</button>
    <br />
    <span>Ping through HTTP</span>
    <br />
    <b>
      <span>Ping responses:</span>
    </b>
    <br />
    <div v-for="(response, i) in httpResponses" :key="i + 'http'">
      <span>Message: {{response.message}} Time Taken: {{response.time}}ms</span>
      <br />
    </div>
    <span>Ping through WebSockets, status: {{status}}</span>
    <br />
    <b>
      <span>Ping responses:</span>
    </b>
    <br />
    <div v-for="(response, i) in socketResponses" :key="i + 'socket'">
      <span>Message: {{response.message}} Time Taken: {{response.time}}ms</span>
      <br />
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';

export default {
  name: 'ping',
  data: () => ({
    pingMessage: '',
    httpResponses: [],
    socketResponses: [],
    connected: false,
  }),
  computed: {
    status() {
      if (this.connected) {
        return 'Connected';
      }
      return 'Not Connected';
    },
  },
  created() {
    this.socket = new SockJS(`${process.env.VUE_APP_API_HOST}/socket`);
    this.socket.onopen = () => {
      this.stompClient = Stomp.over(this.socket, { debug: false });
      this.stompClient.connect(
        {},
        () => {
          this.connected = true;
          this.stompClient.subscribe('/user/topic/ping', (tick) => {
            const end = performance.now();
            this.socketResponses.push({
              message: tick.body,
              time: (end - this.socketStart).toFixed(2),
            });
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
  beforeDestroy() {
    if (this.connected) {
      if (this.stompClient.connected) {
        this.stompClient.disconnect();
      } else if (this.socket.readyState === this.socket.OPEN) {
        this.socket.close();
      }
    }
  },
  methods: {
    async sendHTTP() {
      const start = performance.now();
      // Send the request to the server and wait for the response.
      const result = await axios.get(`${process.env.VUE_APP_API_HOST}/ping`, {
        params: {
          ping: this.pingMessage,
        },
      });
      const end = performance.now();
      this.httpResponses.push({
        message: result.data,
        time: (end - start).toFixed(2),
      });
      this.pingMessage = '';
    },
    sendSocket() {
      if (this.connected) {
        this.socketStart = performance.now();
        this.stompClient.send('/app/ping', this.pingMessage, {});
      }
      this.pingMessage = '';
    },
  },
};
</script>
