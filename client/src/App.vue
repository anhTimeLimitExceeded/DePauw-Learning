<template>
<div id="demo" :class="[{'collapsed' : collapsed}]">
  <div class="demo">
    <div class="container">
      <div id="nav">
        <router-link to="/">Home</router-link>|
        <router-link to="/about">About</router-link>|
        <router-link to="/ping">Ping</router-link>|
        <router-link to="/code">Code</router-link>
      </div>
      <router-view />
    </div>
    <sidebar-menu
      :menu="menu"
      :width= "'250px'"
      :collapsed="collapsed"
      :hideToggle=false
      @toggle-collapse="onToggleCollapse"
      @item-click="onItemClick"
    />
  </div>
</div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      menu: [
        {
          header: true,
          title: 'Computer Science Courses',
          hiddenOnCollapse: true,
        },
        {
          title: 'Computer Science 1',
          icon: 'fa fa-code',
          child: [
            {
              title: '1. Variables',
            },
            {
              title: '2. Arrays',
              child: [
                {
                  title: '2.1 Index',
                },
                {
                  title: '2.2 ArrayList',
                },
              ],
            },
            {
              title: '3. Projects',
            },
          ],
        },
        {
          title: 'Networking',
          icon: 'fa fa-cogs',
          badge: {
            text: 'new',
            class: 'vsm--badge_default',
          },
          child: [
            {
              title: '1. Cryptography',
            },
          ],
        },
        {
          header: true,
          title: 'Other Materials',
          hiddenOnCollapse: true,
        },
      ],
      collapsed: true, // set to false if want to be initally opened
    };
  },
  // methods: {
  //  onToggleCollapse (collapsed) {
  //    this.collapsed = collapsed
  //  }
  // }
};
</script>
<style lang="scss">
@import url('https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600');
body,
html {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}
body {
  font-family: 'Source Sans Pro', sans-serif;
  font-size: 16px;
  background-color: #f2f4f7;
  color: #262626;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
#demo {
  padding-left: 250px;
}
#demo.collapsed {
  padding-left: 50px;
}
.demo {
  padding: 50px;
}
.container {
   font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
pre {
  font-family: Consolas, monospace;
  color: #000;
  background: #fff;
  border-radius: 2px;
  font-size: 13px;
  padding: 5px;
  overflow: auto;
}
</style>
