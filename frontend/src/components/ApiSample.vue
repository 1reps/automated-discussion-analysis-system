<template>
  <div style="padding: 12px; border: 1px solid #ddd; border-radius: 8px;">
    <h3>API Sample</h3>
    <div style="display:flex; gap:8px; align-items:center; margin-bottom:8px;">
      <button @click="callApi" :disabled="loading">
        {{ loading ? 'Loading…' : 'Call /sample' }}
      </button>
      <small style="opacity:.7">Base: {{ baseUrl }}</small>
    </div>
    <div v-if="error" style="color:#b00020">Error: {{ error }}</div>
    <div v-else-if="result">Response: {{ result }}</div>
  </div>
  
</template>

<script setup>
import { ref, computed } from 'vue'

// Prefer env var; fallback to local Spring Boot default
const baseUrl = computed(() => import.meta.env.VITE_API_BASE || 'http://localhost:8080')

const loading = ref(false)
const result = ref('')
const error = ref('')

async function callApi() {
  loading.value = true
  result.value = ''
  error.value = ''
  try {
    const res = await fetch(`${baseUrl.value}/sample`, { method: 'GET' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    result.value = await res.text()
  } catch (e) {
    error.value = e?.message || String(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
button {
  padding: 6px 10px;
  border: 1px solid #888;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
}
button:disabled {
  opacity: .6;
  cursor: default;
}
</style>

