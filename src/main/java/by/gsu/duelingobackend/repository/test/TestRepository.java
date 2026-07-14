package by.gsu.duelingobackend.repository.test;

import by.gsu.duelingobackend.model.document.Test;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TestRepository extends MongoRepository<Test, String>, CustomTestRepository {

    @Query(value = "{ 'topic' : ?0 }", fields = "{ 'questions' : 0 }")
    List<Test> findByTopicExcludingQuestions(String topic);
}
