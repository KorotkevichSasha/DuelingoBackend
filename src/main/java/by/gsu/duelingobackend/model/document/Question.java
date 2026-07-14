package by.gsu.duelingobackend.model.document;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {

    @Id
    private ObjectId id;

    @NotNull(message = "topic cannot be null")
    private String topic;

    @Indexed
    @NotNull(message = "difficulty cannot be null")
    private QuestionDifficulty difficulty;

    @Indexed
    @NotNull(message = "Question type cannot be null")
    private QuestionType type;

    @NotBlank(message = "Question text cannot be blank")
    private String questionText;

    @NotNull(message = "Options cannot be null")
    @Size(min = 1, message = "There must be at least one option")
    private List<String> options;

    @NotNull(message = "Correct answers cannot be null")
    @Size(min = 1, message = "There must be at least one correct answer")
    private List<String> correctAnswers;

    @URL(message = "Invalid audio URL format")
    private String audioUrl;

}
