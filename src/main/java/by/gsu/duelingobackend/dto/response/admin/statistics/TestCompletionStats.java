package by.gsu.duelingobackend.dto.response.admin.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCompletionStats {
    private String date;
    private int completions;
    private double averageScore;
} 