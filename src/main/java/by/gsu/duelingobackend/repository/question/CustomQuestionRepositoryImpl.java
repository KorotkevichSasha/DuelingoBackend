package by.gsu.duelingobackend.repository.question;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class CustomQuestionRepositoryImpl implements CustomQuestionRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Question> findRandomQuestions(String topic, QuestionDifficulty questionDifficulty, QuestionType questionType, int size) {
        Criteria criteria = new Criteria();
        if (topic != null) criteria.and("topic").is(topic);
        if (questionDifficulty != null) criteria.and("difficulty").is(questionDifficulty);
        if (questionType != null) {
            criteria.and("type").is(questionType);
        } else {
            criteria.and("type").ne(QuestionType.AUDIO_RECOGNITION);
        }

        List<Question> candidates = new ArrayList<>(mongoTemplate.find(Query.query(criteria), Question.class));
        Collections.shuffle(candidates);

        Map<String, Question> uniqueBySentence = new LinkedHashMap<>();
        for (Question question : candidates) {
            uniqueBySentence.putIfAbsent(semanticKey(question), question);
            if (uniqueBySentence.size() == size) {
                break;
            }
        }
        return List.copyOf(uniqueBySentence.values());
    }

    /**
     * Different exercise types may be built around the same sentence.  A test
     * must never contain two of those variants, so sentence-construction tasks
     * are keyed by their answer and cloze tasks by the completed sentence.
     */
    static String semanticKey(Question question) {
        String text;
        if (question.getType() == QuestionType.SENTENCE_CONSTRUCTION) {
            text = String.join(" ", question.getCorrectAnswers());
        } else {
            text = question.getQuestionText();
            if (text != null && text.contains("___") && !question.getCorrectAnswers().isEmpty()) {
                text = text.replaceFirst("_+", question.getCorrectAnswers().get(0));
            }
        }
        return normalize(text);
    }

    private static String normalize(String value) {
        return value == null ? "" : value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();
    }
}
