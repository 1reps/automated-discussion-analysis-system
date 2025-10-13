<template>
  <div class="layout">
    <nav class="nav">
      <a class="brand" href="#">ADAS</a>
      <div class="links">
        <RouterLink to="/">Recorder</RouterLink>
        <RouterLink to="/sample">API Sample</RouterLink>
      </div>
    </nav>
    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
// RouterLink/RouterView are provided by vue-router
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #e5e7eb;
}
.brand {
  font-weight: 700;
  color: #111827;
  text-decoration: none;
}
.links {
  display: flex;
  gap: 12px;
}
.links a {
  color: #2563eb;
  text-decoration: none;
}
.links a.router-link-active {
  font-weight: 600;
}
.content {
  padding: 16px;
}
</style>
