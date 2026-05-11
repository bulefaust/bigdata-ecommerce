import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userId = ref(localStorage.getItem('userId') || null)
  const username = ref(localStorage.getItem('username') || '')

  function setUser(id, name) {
    userId.value = id
    username.value = name
    localStorage.setItem('userId', id)
    localStorage.setItem('username', name)
  }

  function logout() {
    userId.value = null
    username.value = ''
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  return { userId, username, setUser, logout }
})
