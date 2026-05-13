<template>
  <router-view />
</template>

<script setup>
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&family=Noto+Sans+SC:wght@300;400;500;600;700;900&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --bg-body: #0B1120;
  --bg-surface: #0F172A;
  --bg-elevated: #1E293B;
  --bg-card: rgba(15, 23, 42, 0.8);
  --bg-card-hover: rgba(30, 41, 59, 0.9);
  --bg-glass: rgba(15, 23, 42, 0.6);
  --border-subtle: rgba(0, 212, 255, 0.06);
  --border-default: rgba(0, 212, 255, 0.12);
  --border-strong: rgba(0, 212, 255, 0.2);
  --text-primary: #F1F5F9;
  --text-secondary: rgba(241, 245, 249, 0.65);
  --text-tertiary: rgba(241, 245, 249, 0.4);
  --text-muted: rgba(241, 245, 249, 0.25);
  --color-primary: #00D4FF;
  --color-primary-light: #38BDF8;
  --color-primary-dark: #0284C7;
  --color-accent: #7C3AED;
  --color-accent-light: #A78BFA;
  --color-orange: #F59E0B;
  --color-orange-light: #FBBF24;
  --color-green: #10B981;
  --color-green-light: #34D399;
  --color-red: #EF4444;
  --color-red-light: #F87171;
  --color-pink: #EC4899;
  --gradient-primary: linear-gradient(135deg, #00D4FF, #7C3AED);
  --gradient-hero: linear-gradient(135deg, #00D4FF 0%, #7C3AED 50%, #00D4FF 100%);
  --gradient-orange: linear-gradient(135deg, #F59E0B, #EF4444);
  --gradient-glow: linear-gradient(135deg, rgba(0,212,255,0.15), rgba(124,58,237,0.15));
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.4);
  --shadow-md: 0 4px 20px rgba(0, 0, 0, 0.5);
  --shadow-lg: 0 8px 40px rgba(0, 0, 0, 0.6);
  --shadow-glow: 0 0 30px rgba(0, 212, 255, 0.15);
  --shadow-glow-accent: 0 0 30px rgba(124, 58, 237, 0.15);
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --radius-2xl: 24px;
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-base: 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  --font-display: 'Inter', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  --font-body: 'Noto Sans SC', 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: var(--font-body);
  background: var(--bg-body);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-size: 14px;
  line-height: 1.6;
  overflow-x: hidden;
}

a {
  color: var(--color-primary-light);
  text-decoration: none;
  transition: color var(--transition-fast);
}

a:hover {
  color: var(--color-accent-light);
}

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.2);
}

::selection {
  background: rgba(0, 212, 255, 0.3);
  color: #fff;
}

.el-message {
  border-radius: var(--radius-md) !important;
  background: var(--bg-elevated) !important;
  border: 1px solid var(--border-default) !important;
}
</style>
