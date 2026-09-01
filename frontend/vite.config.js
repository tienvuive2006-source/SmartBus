import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { join } from 'node:path'
import { tmpdir } from 'node:os'

// https://vite.dev/config/
export default defineConfig({
  // Keep Vite's disposable dependency cache outside node_modules on Windows.
  // Editors and antivirus tools can briefly lock generated files there and
  // otherwise make a later dev-server start fail with EPERM while unlinking.
  cacheDir: join(tmpdir(), 'smart-bus-booking-vite-cache'),
  plugins: [vue()],
  resolve: {
    alias: {
      // Cho phép dùng @/ thay vì ../../../ khi import
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  define: {
    // Fix lỗi 'global is not defined' của thư viện sockjs-client trong Vite
    global: 'window'
  },
  build: {
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            return id.toString().split('node_modules/')[1].split('/')[0].toString();
          }
        }
      }
    }
  }
})
