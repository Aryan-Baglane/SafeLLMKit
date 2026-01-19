import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            'safellmkit-js': path.resolve(__dirname, '../safellmkit-js/src/index.ts')
        }
    }
})
