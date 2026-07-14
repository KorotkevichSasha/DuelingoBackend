package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.AudioQuestionResponse;
import by.gsu.duelingobackend.dto.response.QuestionDetailedResponse;
import by.gsu.duelingobackend.mapper.QuestionMapper;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    public List<QuestionDetailedResponse> getRandomQuestions(String topic, QuestionDifficulty questionDifficulty, QuestionType questionType, int size) {
        return questionRepository.findRandomQuestions(topic, questionDifficulty, questionType, size).stream()
                .map(questionMapper::toDetailedResponse).toList();
    }

    public List<AudioQuestionResponse> getRandomAudioQuestions(@Min(1) int size) {
        return questionRepository.findRandomQuestions(null, null, QuestionType.AUDIO_RECOGNITION, size).stream()
                .map(questionMapper::toAudioResponse).toList();
    }
}
