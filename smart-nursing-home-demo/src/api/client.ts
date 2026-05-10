import { createApiClient, createTokenManager, unwrapApiData, extractApiErrorMessage } from 'common-react';
import { ROUTE_PATHS } from '../constants/routes';

const TOKEN_STORAGE_KEY = 'smart_nursing_home_demo_token';

const tokenManager = createTokenManager(TOKEN_STORAGE_KEY);

export const getToken = tokenManager.getToken;
export const setToken = tokenManager.setToken;
export const clearToken = tokenManager.clearToken;
export const hasToken = tokenManager.hasToken;

export { unwrapApiData, extractApiErrorMessage };

const apiClient = createApiClient({
  tokenStorageKey: TOKEN_STORAGE_KEY,
  loginPath: ROUTE_PATHS.LOGIN,
});

export default apiClient;
