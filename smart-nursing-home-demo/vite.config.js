import { fileURLToPath, URL } from 'node:url';
import path from 'node:path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
export default defineConfig(function (_a) {
    var mode = _a.mode;
    var env = loadEnv(mode, process.cwd(), '');
    var proxyTarget = env.VITE_API_BASE_URL || 'http://localhost:8080';
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
        server: {
            port: 5173,
            proxy: {
                '/auth': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/api/auth': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/api/users': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/api/roles': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/api/permissions': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/care-orchestration': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/admission': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/contract': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/health': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/care-delivery': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/quality': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/billing': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/resource-scheduling': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/safety-emergency': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/business': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
                '/user-service': {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure: function (proxy) {
                        proxy.on('proxyReq', function (proxyReq) {
                            proxyReq.removeHeader('cookie');
                        });
                    },
                },
            },
        },
    };
});
