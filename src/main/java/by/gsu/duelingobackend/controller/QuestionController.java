package by.gsu.duelingobackend.controller;

import by.gsu.duelingobackend.dto.response.AudioAnswerResponse;
import by.gsu.duelingobackend.dto.response.AudioQuestionResponse;
import by.gsu.duelingobackend.dto.response.QuestionDetailedResponse;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.service.QuestionService;
import by.gsu.duelingobackend.service.audio.impl.AudioProcessingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final AudioProcessingService audioProcessingService;

    @GetMapping("/random")
    public List<QuestionDetailedResponse> getRandomQuestions(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) QuestionDifficulty questionDifficulty,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return questionService.getRandomQuestions(topic, questionDifficulty, type, size);
    }

    @GetMapping("/audio/random")
    public List<AudioQuestionResponse> getRandomAudioQuestions(
            @RequestParam(defaultValue = "1") @Min(1) int size
    ) {
        return questionService.getRandomAudioQuestions(size);
    }

    @PostMapping(value = "/{questionId}/verify-answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AudioAnswerResponse verifyAnswer(
            @PathVariable String questionId,
            @RequestParam("audioFile") @NotNull MultipartFile audioFile
    ) {
        return audioProcessingService.verifyAnswer(questionId, audioFile);
    }
}
