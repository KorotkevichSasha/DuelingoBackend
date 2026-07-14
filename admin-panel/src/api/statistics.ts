import { api } from './api';

export interface UserRegistrationStats {
  date: string;
  count: number;
}

export interface TopicPopularityStats {
  topic: string;
  attempts: number;
  averageScore: number;
}

export interface UserProgressStats {
  username: string;
  wordsLearned: number;
  testsCompleted: number;
  duelsWon: number;
}

export interface TestCompletionStats {
  date: string;
  completions: number;
  averageScore: number;
}

export interface UserStatisticsResponse {
  totalUsers: number;
  activeUsersLast24h: number;
  activeUsersLast7d: number;
  activeUsersLast30d: number;
  registrationStats: UserRegistrationStats[];
  usersByRole: Record<string, number>;
}

export interface ContentStatisticsResponse {
  totalTests: number;
  totalQuestions: number;
  questionsByType: Record<string, number>;
  questionsByDifficulty: Record<string, number>;
  testsByTopic: Record<string, number>;
  topicPopularityStats: TopicPopularityStats[];
}

export interface LearningStatisticsResponse {
  totalWordsLearned: number;
  totalTestsCompleted: number;
  totalDuelsPlayed: number;
  completionsByDifficulty: Record<string, number>;
  topUsersByProgress: UserProgressStats[];
  testCompletionStats: TestCompletionStats[];
}

export const fetchUserStatistics = async (): Promise<UserStatisticsResponse> => {
  const response = await api.get('/admin/statistics/users');
  return response.data;
};

export const fetchContentStatistics = async (): Promise<ContentStatisticsResponse> => {
  const response = await api.get('/admin/statistics/content');
  return response.data;
};

export const fetchLearningStatistics = async (): Promise<LearningStatisticsResponse> => {
  const response = await api.get('/admin/statistics/learning');
  return response.data;
}; 