package by.gsu.duelingobackend.service.impl;

import by.gsu.duelingobackend.dto.request.admin.content.CreateQuestionRequest;
import by.gsu.duelingobackend.dto.request.admin.content.CreateTestRequest;
import by.gsu.duelingobackend.dto.response.admin.content.AdminQuestionListResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminQuestionResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminTestListResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminTestResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.document.Test;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.repository.test.TestRepository;
import by.gsu.duelingobackend.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public AdminTestListResponse getAllTests(int page, int size, String topic, QuestionDifficulty difficulty) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        if (topic != null && !topic.isEmpty()) {
            criteria.add(Criteria.where("topic").is(topic));
        }
        if (difficulty != null) {
            criteria.add(Criteria.where("difficulty").is(difficulty));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Test> tests = mongoTemplate.find(query, Test.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Test.class);

        Page<Test> testPage = PageableExecutionUtils.getPage(tests, pageable, () -> total);

        List<AdminTestResponse> testResponses = tests.stream()
                .map(test -> AdminTestResponse.builder()
                        .id(test.getId().toString())
                        .topic(test.getTopic())
                        .difficulty(test.getDifficulty())
                        .questionCount(test.getQuestions().size())
                        .completionRate(calculateTestCompletionRate(test))
                        .averageScore(calculateTestAverageScore(test))
                        .build())
                .toList();

        return AdminTestListResponse.builder()
                .tests(testResponses)
                .totalTests(testPage.getTotalElements())
                .currentPage(page)
                .totalPages(testPage.getTotalPages())
                .build();
    }

    @Override
    public AdminQuestionListResponse getAllQuestions(int page, int size, String topic, QuestionType type, QuestionDifficulty difficulty) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        if (topic != null && !topic.isEmpty()) {
            criteria.add(Criteria.where("topic").is(topic));
        }
        if (type != null) {
            criteria.add(Criteria.where("type").is(type));
        }
        if (difficulty != null) {
            criteria.add(Criteria.where("difficulty").is(difficulty));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Question> questions = mongoTemplate.find(query, Question.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Question.class);

        Page<Question> questionPage = PageableExecutionUtils.getPage(questions, pageable, () -> total);

        List<AdminQuestionResponse> questionResponses = questions.stream()
                .map(question -> AdminQuestionResponse.builder()
                        .id(question.getId().toString())
                        .topic(question.getTopic())
                        .type(question.getType())
                        .difficulty(question.getDifficulty())
                        .correctRate(calculateQuestionCorrectRate(question))
                        .build())
                .toList();

        return AdminQuestionListResponse.builder()
                .questions(questionResponses)
                .totalQuestions(questionPage.getTotalElements())
                .currentPage(page)
                .totalPages(questionPage.getTotalPages())
                .build();
    }

    private double calculateTestCompletionRate(Test test) {
        // TODO: Implement test completion rate calculation
        return 0.0;
    }

    private double calculateTestAverageScore(Test test) {
        // TODO: Implement test average score calculation
        return 0.0;
    }

    private double calculateQuestionCorrectRate(Question question) {
        // TODO: Implement question correct rate calculation
        return 0.0;
    }

    @Override
    public AdminTestResponse createTest(CreateTestRequest request) {
        Test newTest = Test.builder()
                .topic(request.getTopic())
                .difficulty(request.getDifficulty())
                .build();
        
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<Question> questions = request.getQuestions().stream()
                    .map(id -> questionRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Question not found")))
                    .toList();
            newTest.setQuestions(questions);
        } else {
            newTest.setQuestions(new ArrayList<>());
        }
        
        Test savedTest = testRepository.save(newTest);
        
        return AdminTestResponse.builder()
                .id(savedTest.getId().toString())
                .topic(savedTest.getTopic())
                .difficulty(savedTest.getDifficulty())
                .questionCount(savedTest.getQuestions().size())
                .completionRate(0.0)
                .averageScore(0.0)
                .build();
    }
    
    @Override
    public AdminQuestionResponse createQuestion(CreateQuestionRequest request) {
        Question newQuestion = Question.builder()
                .topic(request.getTopic())
                .type(request.getType())
                .difficulty(request.getDifficulty())
                .questionText(request.getContent())
                .options(request.getOptions())
                .correctAnswers(List.of(request.getCorrectAnswer()))
                .build();
        
        Question savedQuestion = questionRepository.save(newQuestion);
        
        return AdminQuestionResponse.builder()
                .id(savedQuestion.getId().toString())
                .topic(savedQuestion.getTopic())
                .type(savedQuestion.getType())
                .difficulty(savedQuestion.getDifficulty())
                .correctRate(0.0)
                .build();
    }
} 