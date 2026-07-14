package by.gsu.duelingobackend.model.document;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tests")
public class Test {

    @Id
    private ObjectId id;

    @Indexed
    @NotBlank(message = "Topic cannot be blank")
    private String topic;

    @NotNull(message = "difficulty cannot be null")
    private QuestionDifficulty difficulty;

    @DocumentReference
    @NotNull(message = "Test must have at least one question")
    private List<Question> questions;
}
