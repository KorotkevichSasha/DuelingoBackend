package by.gsu.duelingobackend.dto.response;

public record MatchmakingEstimateResponse(
        String difficulty,
        long averageWaitSeconds,
        long playersWaiting
) {
}
