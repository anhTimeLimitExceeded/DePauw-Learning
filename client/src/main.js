import Vue from 'vue';
import VueSidebarMenu from 'vue-sidebar-menu';
import App from './App.vue';
import router from './router';
import store from './store';
import 'vue-sidebar-menu/dist/vue-sidebar-menu.css';
import '@fortawesome/fontawesome-free/css/all.css';

Vue.use(VueSidebarMenu);
Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app');
