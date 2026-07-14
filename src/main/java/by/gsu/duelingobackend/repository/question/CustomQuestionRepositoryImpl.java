package by.gsu.duelingobackend.repository.question;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

@RequiredArgsConstructor
public class CustomQuestionRepositoryImpl implements CustomQuestionRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Question> findRandomQuestions(String topic, QuestionDifficulty questionDifficulty, QuestionType questionType, int size) {
        Criteria criteria = new Criteria();
        if (topic != null) criteria.and("topic").is(topic);
        if (questionDifficulty != null) criteria.and("questionDifficulty").is(questionDifficulty);
        if (questionType != null) {
            criteria.and("type").is(questionType);
        } else {
            criteria.and("type").ne(QuestionType.AUDIO_RECOGNITION);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sample(size)
        );

        return mongoTemplate.aggregate(aggregation, Question.class, Question.class)
                .getMappedResults();
    }
}
