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
public class AdminUserStatisticsResponse {
    private long totalUsers;
    private long activeUsersLast24h;
    private long activeUsersLast7d;
    private long activeUsersLast30d;
    private List<UserRegistrationStats> registrationStats;
    private Map<String, Integer> usersByRole;
} 