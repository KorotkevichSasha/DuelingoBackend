import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8082';

export const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  console.error('Request error:', error);
  return Promise.reject(error);
});

interface RetryableRequest extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

let refreshRequest: Promise<string> | null = null;

api.interceptors.response.use(undefined, async (error: AxiosError) => {
  const request = error.config as RetryableRequest | undefined;
  const refreshToken = sessionStorage.getItem('refreshToken');
  if (error.response?.status !== 401 || !request || request._retry || !refreshToken) {
    return Promise.reject(error);
  }

  request._retry = true;
  refreshRequest ??= axios.post(`${baseURL}/auth/refresh-token`, { refreshToken })
    .then(({ data }) => {
      sessionStorage.setItem('token', data.accessToken);
      sessionStorage.setItem('refreshToken', data.refreshToken);
      return data.accessToken as string;
    })
    .finally(() => { refreshRequest = null; });

  try {
    const accessToken = await refreshRequest;
    request.headers.Authorization = `Bearer ${accessToken}`;
    return api(request);
  } catch (refreshError) {
    sessionStorage.clear();
    window.location.assign('/login');
    return Promise.reject(refreshError);
  }
});

