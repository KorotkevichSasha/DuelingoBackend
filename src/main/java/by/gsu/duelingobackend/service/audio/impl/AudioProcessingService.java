package by.gsu.duelingobackend.service.audio.impl;

import by.gsu.duelingobackend.dto.response.AudioAnswerResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.service.audio.SpeechToTextService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static by.gsu.duelingobackend.util.Constants.QUESTION_NOT_FOUND_BY_ID_ERR_MSG;

@Service
@RequiredArgsConstructor
public class AudioProcessingService {

    private final SpeechToTextService sttService;
    private final QuestionRepository questionRepository;
    private final JaccardSimilarity textSimilarity = new JaccardSimilarity();

    @Transactional
    public AudioAnswerResponse verifyAnswer(String questionId, MultipartFile audioFile) {
        String recognizedText = sttService.recognize(audioFile);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(QUESTION_NOT_FOUND_BY_ID_ERR_MSG, questionId)));

        double score = textSimilarity.apply(question.getQuestionText().toLowerCase(), recognizedText.toLowerCase());

        return new AudioAnswerResponse(
                score >= 0.7,
                recognizedText,
                generateFeedback(score)
        );
    }

    private String generateFeedback(double score) {
        if (score >= 0.9) return "Perfect!";
        if (score >= 0.7) return "Almost there! Check pronunciation";
        return "Needs more practice";
    }
}
