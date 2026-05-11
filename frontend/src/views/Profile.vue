<template>
  <div class="profile">
    <h2>个人中心</h2>
    <el-empty v-if="!userStore.userId" description="请先登录" />
    <el-card v-else>
      <p>用户名：{{ userStore.username }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '../store/user'
const userStore = useUserStore()
</script>

<style scoped>
.profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
