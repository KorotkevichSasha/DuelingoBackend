package by.gsu.duelingobackend.dto.request.admin.content;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {
    @NotBlank(message = "Topic cannot be empty")
    private String topic;
    
    @NotNull(message = "Type cannot be null")
    private QuestionType type;
    
    @NotNull(message = "Difficulty cannot be null")
    private QuestionDifficulty difficulty;
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    private List<String> options;
    
    @NotBlank(message = "Correct answer cannot be empty")
    private String correctAnswer;
} 