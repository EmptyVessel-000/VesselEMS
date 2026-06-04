<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* 全局基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 覆写 Element Plus 主题色 */
:root {
  --el-color-primary: #2563eb;
  --el-color-primary-light-3: #6699ff;
  --el-color-primary-light-5: #a3c1ff;
  --el-color-primary-light-7: #c9d9ff;
  --el-color-primary-light-8: #dce6ff;
  --el-color-primary-light-9: #ecf2ff;
}

/* 路由切换过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>