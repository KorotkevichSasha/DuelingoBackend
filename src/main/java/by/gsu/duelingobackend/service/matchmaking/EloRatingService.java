package by.gsu.duelingobackend.service.matchmaking;

import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EloRatingService {

    private static final int K_FACTOR = 32;
    private static final Logger log = LoggerFactory.getLogger(EloRatingService.class);

    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;

    @Transactional
    public void updateRatings(Duel duel) {
        log.info("Updating ratings for duel: {}", duel.getId());
        log.info("Player1: {} (score: {}, points: {})", duel.getPlayer1().getUsername(), duel.getPlayer1Score(), duel.getPlayer1().getPoints());
        log.info("Player2: {} (score: {}, points: {})", duel.getPlayer2().getUsername(), duel.getPlayer2Score(), duel.getPlayer2().getPoints());

        User player1 = duel.getPlayer1();
        User player2 = duel.getPlayer2();

        double expectedScore = getExpectedScore(player1.getPoints(), player2.getPoints());
        double actualScore = getActualScore(duel);

        int delta1 = (int) (K_FACTOR * (actualScore - expectedScore));
        int delta2 = (int) (K_FACTOR * ((1 - actualScore) - (1 - expectedScore)));

        log.info("Expected score: {}, Actual score: {}", expectedScore, actualScore);
        log.info("Delta1: {}, Delta2: {}", delta1, delta2);

        player1.setPoints(player1.getPoints() + delta1);
        player2.setPoints(player2.getPoints() + delta2);

        userRepository.saveAll(List.of(player1, player2));

        log.info("New points - Player1: {}, Player2: {}", player1.getPoints(), player2.getPoints());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        leaderboardService.updateUserScore(player1.getId(), player1.getPoints());
                        leaderboardService.updateUserScore(player2.getId(), player2.getPoints());
                    }
                }
        );
    }

    private double getExpectedScore(int ratingA, int ratingB) {
        return 1.0 / (1 + Math.pow(10, (ratingB - ratingA) / 400.0));
    }

    private double getActualScore(Duel duel) {
        if (Objects.equals(duel.getPlayer1Score(), duel.getPlayer2Score())) return 0.5;
        return duel.getPlayer1Score() > duel.getPlayer2Score() ? 1.0 : 0.0;
    }
}
