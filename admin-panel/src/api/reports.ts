import { api } from './api';

export interface UserReport {
  id: string;
  reporterId: string;
  reporterUsername: string;
  reportedUserId: string;
  reportedUsername: string;
  reason: string;
  createdAt: string;
}

export const fetchReports = async () => (await api.get<UserReport[]>('/admin/reports')).data;
