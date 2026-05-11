<template>
  <router-view />
</template>

<script setup>
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;600;700;900&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --bg-body: #f5f5f5;
  --bg-white: #ffffff;
  --bg-gray: #f7f8fa;
  --bg-hover: #f0f1f3;
  --border-light: #e8e8e8;
  --border-medium: #d9d9d9;
  --text-primary: #222222;
  --text-regular: #333333;
  --text-secondary: #666666;
  --text-muted: #999999;
  --text-placeholder: #cccccc;
  --color-primary: #ff4d4f;
  --color-primary-light: #fff1f0;
  --color-primary-dark: #cf1322;
  --color-orange: #ff7a45;
  --color-orange-light: #fff7e6;
  --color-green: #52c41a;
  --color-green-light: #f6ffed;
  --color-blue: #1890ff;
  --color-blue-light: #e6f7ff;
  --color-gold: #faad14;
  --color-gold-light: #fffbe6;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.06);
  --shadow-md: 0 2px 8px rgba(0,0,0,0.08);
  --shadow-lg: 0 4px 16px rgba(0,0,0,0.1);
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
}

body {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: var(--bg-body);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  font-size: 14px;
  line-height: 1.5;
}

a {
  color: var(--color-blue);
  text-decoration: none;
}

a:hover {
  color: var(--color-primary);
}

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: var(--bg-body);
}

::-webkit-scrollbar-thumb {
  background: var(--border-medium);
  border-radius: 3px;
}

::selection {
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.el-message {
  border-radius: var(--radius-md) !important;
}
</style>
