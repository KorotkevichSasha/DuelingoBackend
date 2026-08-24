package by.gsu.duelingobackend.dto.event;

public record DuelResultEvent(
        int player1Score,
        int player2Score,
        String winner,
        String forfeitedBy,
        int goldAwarded,
        int ratingDelta,
        String leagueId,
        boolean leaguePromoted,
        String previousLeagueId,
        int leagueBonusGold
) {
}
