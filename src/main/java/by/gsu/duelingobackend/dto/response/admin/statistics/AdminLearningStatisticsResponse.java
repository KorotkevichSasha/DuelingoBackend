package by.gsu.duelingobackend.dto.response.admin.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLearningStatisticsResponse {
    private long totalWordsLearned;
    private long totalTestsCompleted;
    private long totalDuelsPlayed;
    private Map<String, Integer> completionsByDifficulty;
    private List<UserProgressStats> topUsersByProgress;
    private List<TestCompletionStats> testCompletionStats;
} 