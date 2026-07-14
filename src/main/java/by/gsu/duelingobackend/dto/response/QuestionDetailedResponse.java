package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;

import java.io.Serializable;
import java.util.List;

public record QuestionDetailedResponse(

        QuestionDifficulty difficulty,
        QuestionType type,
        String questionText,
        List<String> options,
        List<String> correctAnswers,
        String audioUrl
) implements Serializable {
}
