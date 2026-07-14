package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.admin.content.CreateQuestionRequest;
import by.gsu.duelingobackend.dto.request.admin.content.CreateTestRequest;
import by.gsu.duelingobackend.dto.response.admin.content.AdminQuestionListResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminQuestionResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminTestListResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminTestResponse;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;

public interface AdminContentService {
    AdminTestListResponse getAllTests(int page, int size, String topic, QuestionDifficulty difficulty);
    AdminQuestionListResponse getAllQuestions(int page, int size, String topic, QuestionType type, QuestionDifficulty difficulty);
    AdminTestResponse createTest(CreateTestRequest request);
    AdminQuestionResponse createQuestion(CreateQuestionRequest request);
} 