import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const selectedCategory = ref('全部')

  function setCategory(cat) {
    selectedCategory.value = cat
  }

  return { selectedCategory, setCategory }
})
