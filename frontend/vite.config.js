import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"

export default defineConfig({
    plugins: [vue()],
    server: {
        port: 5173,
        proxy: {
            // 프론트에서 /api 로 부르면 백엔드로 프록시
            "/api": {
                target: process.env.VITE_API_BASE || "http://localhost:8081",
                changeOrigin: true
                // 필요 시:
                // rewrite: p => p.replace(/^\/api/, ''),
            }
        }
    }
})
