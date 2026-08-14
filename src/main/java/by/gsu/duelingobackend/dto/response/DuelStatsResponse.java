package by.gsu.duelingobackend.dto.response;

public record DuelStatsResponse(long total, long wins, long losses, long draws, int winRate) {
}
