<script setup>
/**
 * App.vue - 应用根组件
 * router-view: 根据当前路由显示对应的页面组件
 */
</script>

<template>
  <div id="app">
    <!-- router-view 会根据当前路由渲染对应的组件 -->
    <router-view />
  </div>
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>
