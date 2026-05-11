import axios from 'axios'

const TRACK_URL = '/api/track/behavior'

let sessionId = generateSessionId()
let userId = null

function generateSessionId() {
  return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

export function setTrackUserId(id) {
  userId = id
}

function sendTrack(data) {
  const payload = {
    ...data,
    userId,
    sessionId,
    timestamp: Date.now(),
    userAgent: navigator.userAgent,
    ip: '',
  }

  axios.post(TRACK_URL, payload, { timeout: 3000 }).catch(() => {})
}

export function trackClick(productId, category) {
  sendTrack({ eventType: 'CLICK', productId, category })
}

export function trackBrowse(productId, category, duration) {
  sendTrack({ eventType: 'BROWSE', productId, category, duration })
}

export function trackSearch(keyword) {
  sendTrack({ eventType: 'SEARCH', keyword })
}

export function trackPurchase(productId, category) {
  sendTrack({ eventType: 'PURCHASE', productId, category })
}

export function trackCart(productId, category) {
  sendTrack({ eventType: 'CART', productId, category })
}

export function initTracker() {
  document.addEventListener('click', (e) => {
    const target = e.target.closest('[data-track]')
    if (target) {
      const trackType = target.dataset.track
      const productId = target.dataset.productId
      const category = target.dataset.category
      if (trackType && productId) {
        sendTrack({ eventType: trackType.toUpperCase(), productId, category })
      }
    }
  })

  window.addEventListener('beforeunload', () => {
    if (window.__currentBrowseProduct) {
      const { productId, category, startTime } = window.__currentBrowseProduct
      const duration = Math.floor((Date.now() - startTime) / 1000)
      const payload = {
        eventType: 'BROWSE',
        productId,
        category,
        duration,
        userId,
        sessionId,
        timestamp: Date.now(),
        userAgent: navigator.userAgent,
        ip: '',
      }
      const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' })
      navigator.sendBeacon(TRACK_URL, blob)
    }
  })
}

export function startBrowseTimer(productId, category) {
  window.__currentBrowseProduct = {
    productId,
    category,
    startTime: Date.now(),
  }
}

export function endBrowseTimer() {
  if (window.__currentBrowseProduct) {
    const { productId, category, startTime } = window.__currentBrowseProduct
    const duration = Math.floor((Date.now() - startTime) / 1000)
    trackBrowse(productId, category, duration)
    window.__currentBrowseProduct = null
  }
}
