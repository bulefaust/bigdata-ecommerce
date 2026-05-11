import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cartItems') || '[]'))

  const totalAmount = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  const totalCount = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  function save() {
    localStorage.setItem('cartItems', JSON.stringify(items.value))
  }

  function addItem(product) {
    const existing = items.value.find((i) => i.id === product.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({
        id: product.id,
        name: product.name,
        category: product.category,
        price: product.price,
        imageUrl: product.imageUrl,
        quantity: 1,
      })
    }
    save()
  }

  function removeItem(productId) {
    items.value = items.value.filter((i) => i.id !== productId)
    save()
  }

  function updateQuantity(productId, quantity) {
    const item = items.value.find((i) => i.id === productId)
    if (item) {
      item.quantity = Math.max(1, quantity)
      save()
    }
  }

  function clear() {
    items.value = []
    save()
  }

  return { items, totalAmount, totalCount, addItem, removeItem, updateQuantity, clear }
})
