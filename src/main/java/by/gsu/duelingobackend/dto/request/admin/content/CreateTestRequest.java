package by.gsu.duelingobackend.dto.request.admin.content;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
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
public class CreateTestRequest {
    @NotBlank(message = "Topic cannot be empty")
    private String topic;
    
    @NotNull(message = "Difficulty cannot be null")
    private QuestionDifficulty difficulty;
    
    private String description;
    
    private List<String> questions; // Question IDs
} 