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
public class AdminContentStatisticsResponse {
    private long totalTests;
    private long totalQuestions;
    private Map<String, Integer> questionsByType;
    private Map<String, Integer> questionsByDifficulty;
    private Map<String, Integer> testsByTopic;
    private List<TopicPopularityStats> topicPopularityStats;
} 