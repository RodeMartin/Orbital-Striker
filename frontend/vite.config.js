import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  
  // EZ A VÉGSŐ FIX, AMI HELYREHOZZA A STOMPJS-T VITE ALATT!
  define: {
    global: 'window',
  }
})