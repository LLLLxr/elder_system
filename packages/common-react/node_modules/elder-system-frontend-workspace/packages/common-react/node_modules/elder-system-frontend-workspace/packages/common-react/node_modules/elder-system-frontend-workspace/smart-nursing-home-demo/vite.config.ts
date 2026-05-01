import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxyTarget = env.VITE_API_BASE_URL || 'http://localhost:8080';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/auth': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/api/auth': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/api/users': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/api/roles': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/api/permissions': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/care-orchestration': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/admission': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/contract': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/health': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/care-delivery': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/quality': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/billing': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/resource-scheduling': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/safety-emergency': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/business': {
          target: proxyTarget,
          changeOrigin: true,
        },
        '/user-service': {
          target: proxyTarget,
          changeOrigin: true,
        },
      },
    },
  };
});
