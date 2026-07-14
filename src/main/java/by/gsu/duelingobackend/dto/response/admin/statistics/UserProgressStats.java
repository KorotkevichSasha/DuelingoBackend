package by.gsu.duelingobackend.dto.response.admin.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressStats {
    private String username;
    private int wordsLearned;
    private int testsCompleted;
    private int duelsWon;
} 