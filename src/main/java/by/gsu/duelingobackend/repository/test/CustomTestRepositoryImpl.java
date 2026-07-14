package by.gsu.duelingobackend.repository.test;

import by.gsu.duelingobackend.model.document.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@RequiredArgsConstructor
public class CustomTestRepositoryImpl implements CustomTestRepository {

    public static final String TOPIC = "topic";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<String> findDistinctTopics() {
        return mongoTemplate.findDistinct(
                TOPIC,
                Test.class,
                String.class
        );
    }
}
