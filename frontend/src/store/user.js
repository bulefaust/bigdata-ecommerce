import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userId = ref(localStorage.getItem('userId') || null)
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('userRole') || 'user')

  const isAdmin = computed(() => role.value === 'admin')

  function setUser(id, name, userRole) {
    userId.value = id
    username.value = name
    role.value = userRole || 'user'
    localStorage.setItem('userId', id)
    localStorage.setItem('username', name)
    localStorage.setItem('userRole', userRole || 'user')
  }

  function logout() {
    userId.value = null
    username.value = ''
    role.value = 'user'
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('userRole')
  }

  return { userId, username, role, isAdmin, setUser, logout }
})
