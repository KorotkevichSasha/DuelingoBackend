package by.gsu.duelingobackend.repository.question;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;

import java.util.List;

public interface CustomQuestionRepository {

    List<Question> findRandomQuestions(String topic, QuestionDifficulty questionDifficulty, QuestionType questionType, int size);
}
