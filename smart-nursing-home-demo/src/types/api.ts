export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface ApiError {
  code: number;
  message: string;
  data?: any;
}

export function isApiSuccess<T>(response: ApiResponse<T>): boolean {
  return response.code === 200;
}

export function extractData<T>(response: ApiResponse<T>): T {
  if (!isApiSuccess(response)) {
    throw new Error(response.message || '请求失败');
  }
  return response.data;
}
