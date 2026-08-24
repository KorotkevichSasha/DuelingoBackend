package by.gsu.duelingobackend.dto.response;

public record LeagueResponse(
        String id,
        String name,
        int minimumPoints,
        Integer nextLeaguePoints,
        int progressPercent,
        Integer pointsToNextLeague
) {
}
