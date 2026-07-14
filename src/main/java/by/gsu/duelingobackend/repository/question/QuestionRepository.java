package by.gsu.duelingobackend.repository.question;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String>, CustomQuestionRepository {

    List<Question> findByDifficulty(QuestionDifficulty questionDifficulty);

    List<Question> findByType(QuestionType type);

}
