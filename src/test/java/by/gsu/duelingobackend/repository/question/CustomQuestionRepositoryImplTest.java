package by.gsu.duelingobackend.repository.question;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomQuestionRepositoryImplTest {

    @Test
    void treatsDifferentExerciseTypesForTheSameSentenceAsOneQuestion() {
        Question choice = question(
                QuestionType.FILL_IN_CHOICE,
                "Anna ___ English every week.",
                List.of("studies")
        );
        Question input = question(
                QuestionType.FILL_IN_INPUT,
                "Anna ___ English every week.",
                List.of("studies")
        );
        Question construction = question(
                QuestionType.SENTENCE_CONSTRUCTION,
                "Put the words in the correct order.",
                List.of("Anna studies English every week.")
        );

        assertThat(CustomQuestionRepositoryImpl.semanticKey(choice))
                .isEqualTo(CustomQuestionRepositoryImpl.semanticKey(input))
                .isEqualTo(CustomQuestionRepositoryImpl.semanticKey(construction));
    }

    private Question question(QuestionType type, String text, List<String> answers) {
        return Question.builder()
                .topic("Present Simple")
                .difficulty(QuestionDifficulty.EASY)
                .type(type)
                .questionText(text)
                .options(List.of("studies"))
                .correctAnswers(answers)
                .build();
    }
}
