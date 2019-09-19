<template>
  <div class="ping">
    <input v-model="pingMessage" placeholder="Message to ping" @keyup.enter="send" />
    <button @click.stop="send">Send</button>
    <br />
    <b>
      <span>Ping responses:</span>
    </b>
    <br />
    <div v-for="(response, i) in responses" :key="i">
      <span>Message: {{response.message}} Time Taken: {{response.time}}ms</span>
      <br />
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "ping",
  data: () => ({
    pingMessage: "",
    responses: []
  }),
  methods: {
    async send(event) {
      const start = performance.now();
      // Send the request to the server and wait for the response.
      const result = await axios.get(`${process.env.VUE_APP_API_HOST}/ping`, {
        params: {
          ping: this.pingMessage
        }
      });
      const end = performance.now();
      this.responses.push({
        message: result.data,
        time: (end - start).toFixed(2)
      });
      this.pingMessage = "";
    }
  }
};
</script>
