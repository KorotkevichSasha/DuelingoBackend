package by.gsu.duelingobackend.dto.response.admin.content;

import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTestResponse {
    private String id;
    private String topic;
    private QuestionDifficulty difficulty;
    private int questionCount;
    private double completionRate;
    private double averageScore;
} 