package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.request.admin.content.CreateQuestionRequest;
import by.gsu.duelingobackend.dto.request.admin.content.CreateTestRequest;
import by.gsu.duelingobackend.dto.response.admin.content.AdminQuestionListResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminQuestionResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminTestListResponse;
import by.gsu.duelingobackend.dto.response.admin.content.AdminTestResponse;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping("/tests")
    public ResponseEntity<AdminTestListResponse> getAllTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) QuestionDifficulty difficulty
    ) {
        return ResponseEntity.ok(adminContentService.getAllTests(page, size, topic, difficulty));
    }

    @GetMapping("/questions")
    public ResponseEntity<AdminQuestionListResponse> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) QuestionDifficulty difficulty
    ) {
        return ResponseEntity.ok(adminContentService.getAllQuestions(page, size, topic, type, difficulty));
    }
    
    @PostMapping("/tests")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AdminTestResponse> createTest(@RequestBody CreateTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createTest(request));
    }
    
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AdminQuestionResponse> createQuestion(@RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createQuestion(request));
    }
} 