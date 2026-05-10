import { fileURLToPath, URL } from 'node:url';
import path from 'node:path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxyTarget = env.VITE_API_BASE_URL || 'http://localhost:8080';

  return {
    plugins: [react()],
    resolve: {
      alias: {
        'common-react': path.resolve(path.dirname(fileURLToPath(new URL(import.meta.url))), '../packages/common-react/src/index.ts'),
        antd: path.resolve(path.dirname(fileURLToPath(new URL(import.meta.url))), 'node_modules/antd'),
        react: path.resolve(path.dirname(fileURLToPath(new URL(import.meta.url))), 'node_modules/react'),
        'react-dom': path.resolve(path.dirname(fileURLToPath(new URL(import.meta.url))), 'node_modules/react-dom'),
        'react/jsx-runtime': path.resolve(path.dirname(fileURLToPath(new URL(import.meta.url))), 'node_modules/react/jsx-runtime.js'),
        'react/jsx-dev-runtime': path.resolve(path.dirname(fileURLToPath(new URL(import.meta.url))), 'node_modules/react/jsx-dev-runtime.js'),
      },
    },
    build: {
      rollupOptions: {
        onwarn(warning, warn) {
          if (warning.code === 'MODULE_LEVEL_DIRECTIVE' && warning.message.includes("'use client'")) {
            return;
          }
          warn(warning);
        },
        output: {
          manualChunks(id) {
            if (id.includes('node_modules/react') || id.includes('node_modules/react-dom') || id.includes('node_modules/react-router-dom')) {
              return 'react-vendor';
            }
            if (id.includes('node_modules/@ant-design/icons')) {
              return 'antd-icons';
            }
            if (id.includes('node_modules/antd/es/')) {
              const componentName = id.split('node_modules/antd/es/')[1]?.split('/')[0];
              if (['form', 'input', 'input-number', 'select', 'date-picker', 'radio', 'picker'].includes(componentName)) {
                return 'antd-entry';
              }
              if (['table', 'descriptions', 'list', 'tabs', 'collapse', 'drawer', 'modal', 'statistic', 'steps', 'tag', 'empty'].includes(componentName)) {
                return 'antd-display';
              }
              if (['alert', 'button', 'card', 'layout', 'menu', 'message', 'space', 'spin', 'typography'].includes(componentName)) {
                return 'antd-basic';
              }
              return 'antd-core';
            }
            if (id.includes('node_modules/@ant-design')) {
              return 'antd-core';
            }
            if (id.includes('node_modules/rc-')) {
              return 'antd-rc';
            }
            if (id.includes('node_modules/dayjs')) {
              return 'dayjs';
            }
            if (id.includes('node_modules/axios')) {
              return 'axios';
            }
            if (id.includes('node_modules')) {
              return 'vendor';
            }
            return undefined;
          },
        },
      },
    },
    server: {
      port: 5174,
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
