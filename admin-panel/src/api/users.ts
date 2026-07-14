import { api } from './api';

export interface User {
  id: string;
  username: string;
  email: string;
  role: string;
  points: number;
  lastLogin: string;
  totalWords: number;
  completedTests: number;
  totalDuels: number;
}

export interface UserListResponse {
  users: User[];
  totalUsers: number;
  currentPage: number;
  totalPages: number;
}

export const fetchUsers = async (
  page: number = 0,
  size: number = 25,
  search?: string,
  role?: string
): Promise<UserListResponse> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  if (search) {
    params.append('search', search);
  }

  if (role) {
    params.append('role', role);
  }

  const response = await api.get(`/admin/users?${params}`);
  return response.data;
};

export const updateUserRole = async (userId: string, newRole: string): Promise<void> => {
  await api.put(`/admin/users/${userId}/role?newRole=${newRole}`);
};

export const resetUserPassword = async (userId: string): Promise<void> => {
  await api.post(`/admin/users/${userId}/reset-password`);
}; 