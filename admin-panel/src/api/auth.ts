import { api } from './api';

export interface LoginRequest {
  username: string;
  password: string;
}

// Role values are prefixed with 'ROLE_' from the backend
export type UserRole = 'ROLE_ADMIN' | 'ROLE_USER';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  try {
    const response = await api.post('/auth/sign-in', credentials);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const logout = async (): Promise<void> => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
}; 
