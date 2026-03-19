<template>
  <router-view />
</template>

<style>
/* 清楚默认边距，保证全屏填满 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  background-color: #f0f2f5;
}
#app {
  height: 100%;
}
</style>