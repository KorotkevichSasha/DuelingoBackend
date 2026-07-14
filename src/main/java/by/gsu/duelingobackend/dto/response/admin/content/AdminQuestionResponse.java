package by.gsu.duelingobackend.dto.response.admin.content;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionResponse {
    private String id;
    private String topic;
    private QuestionType type;
    private QuestionDifficulty difficulty;
    private double correctRate;
} 