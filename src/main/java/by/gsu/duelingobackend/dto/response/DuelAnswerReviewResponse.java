package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.model.enums.QuestionType;

public record DuelAnswerReviewResponse(
        int questionNumber,
        String questionText,
        QuestionType type,
        String submittedAnswer,
        String correctAnswer,
        boolean correct
) {
}
