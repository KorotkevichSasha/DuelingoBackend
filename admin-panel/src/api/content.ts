import { api } from './api';

export interface Test {
  id: string;
  topic: string;
  difficulty: string;
  questionCount: number;
  completionRate: number;
  averageScore: number;
}

export interface Question {
  id: string;
  topic: string;
  type: string;
  difficulty: string;
  correctRate: number;
}

export interface TestListResponse {
  tests: Test[];
  totalTests: number;
  currentPage: number;
  totalPages: number;
}

export interface QuestionListResponse {
  questions: Question[];
  totalQuestions: number;
  currentPage: number;
  totalPages: number;
}

export interface CreateTestRequest {
  topic: string;
  difficulty: string;
  description?: string;
  questions?: string[]; // Question IDs
}

export interface CreateQuestionRequest {
  topic: string;
  type: string;
  difficulty: string;
  content: string;
  options?: string[];
  correctAnswer: string;
}

export const fetchTests = async (
  page: number = 0,
  size: number = 10,
  topic?: string,
  difficulty?: string
): Promise<TestListResponse> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  if (topic) {
    params.append('topic', topic);
  }

  if (difficulty) {
    params.append('difficulty', difficulty);
  }

  const response = await api.get(`/admin/tests?${params}`);
  return response.data;
};

export const fetchQuestions = async (
  page: number = 0,
  size: number = 10,
  topic?: string,
  type?: string,
  difficulty?: string
): Promise<QuestionListResponse> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  if (topic) {
    params.append('topic', topic);
  }

  if (type) {
    params.append('type', type);
  }

  if (difficulty) {
    params.append('difficulty', difficulty);
  }

  const response = await api.get(`/admin/questions?${params}`);
  return response.data;
};

export const createTest = async (test: CreateTestRequest): Promise<Test> => {
  const response = await api.post('/admin/tests', test);
  return response.data;
};

export const createQuestion = async (question: CreateQuestionRequest): Promise<Question> => {
  const response = await api.post('/admin/questions', question);
  return response.data;
}; 