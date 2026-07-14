package by.gsu.duelingobackend.service.matchmaking;

import by.gsu.duelingobackend.dto.event.DuelFoundEvent;
import by.gsu.duelingobackend.dto.event.MatchmakingFailedEvent;
import by.gsu.duelingobackend.dto.response.DuelResponse;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.service.DuelService;
import by.gsu.duelingobackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchmakingService {

    private static final String MATCHMAKING_QUEUE = "matchmaking:queue";
    private static final String MATCHMAKING_USER = "matchmaking:user:";
    private static final int INITIAL_RANGE = 50;
    private static final int MAX_RANGE = 300;
    private static final int RANGE_STEP = 50;
    private static final long SEARCH_TIMEOUT_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final DuelService duelService;
    private final UserRepository userRepository;

    public void joinMatchmakingQueue(User user) {
        log.info("User with id {} trying to join matchmaking", user.getId());
        UUID userId = user.getId();
        int userPoints = user.getPoints();
        redisTemplate.opsForZSet().add(MATCHMAKING_QUEUE, userId.toString(), userPoints);
        redisTemplate.opsForValue().set(
                MATCHMAKING_USER + userId,
                String.valueOf(userPoints),
                SEARCH_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
        );

        startMatchmakingProcess(userId, userPoints, INITIAL_RANGE);
    }

    public void cancelMatchmaking(UUID userId) {
        cleanupUserFromQueue(userId);
    }

    private void startMatchmakingProcess(UUID userId, int initialElo, int currentRange) {
        log.info("starting matchmaking");
        try {
            while (currentRange <= MAX_RANGE) {
                Optional<UUID> opponent = findSuitableOpponent(userId, initialElo, currentRange);

                if (opponent.isPresent()) {
                    createDuelAndNotify(userId, opponent.get());
                    return;
                }

                TimeUnit.SECONDS.sleep(5);
                currentRange += RANGE_STEP;
            }

            notifyMatchmakingFailed(userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cleanupUserFromQueue(userId);
        }
    }

    private Optional<UUID> findSuitableOpponent(UUID userId, int initialElo, int range) {
        Set<String> candidates = redisTemplate.opsForZSet().rangeByScore(
                MATCHMAKING_QUEUE,
                initialElo - range,
                initialElo + range
        );

        return Objects.requireNonNull(candidates).stream()
                .filter(id -> !id.equals(userId.toString()))
                .min(Comparator.comparingDouble(id ->
                        Math.abs(getUserPoints(id) - initialElo)
                ))
                .map(UUID::fromString);
    }

    private int getUserPoints(String userId) {
        String points = redisTemplate.opsForValue().get(MATCHMAKING_USER + userId);
        return points != null ? Integer.parseInt(points) : 0;
    }

    private void createDuelAndNotify(UUID player1Id, UUID player2Id) {
        DuelResponse duel = duelService.createDuel(player1Id, player2Id);
        cleanupUserFromQueue(player1Id);
        cleanupUserFromQueue(player2Id);

        User player1 = userRepository.findById(player1Id).get();
        User player2 = userRepository.findById(player2Id).get();

        messagingTemplate.convertAndSendToUser(
                player1.getUsername(),
                "/queue/duel-found",
                new DuelFoundEvent(duel, player2.getId())
        );

        messagingTemplate.convertAndSendToUser(
                player2.getUsername(),
                "/queue/duel-found",
                new DuelFoundEvent(duel, player1.getId())
        );
    }

    private void notifyMatchmakingFailed(UUID userId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/matchmaking-failed",
                new MatchmakingFailedEvent("No suitable opponent found")
        );
    }

    private void cleanupUserFromQueue(UUID userId) {
        redisTemplate.opsForZSet().remove(MATCHMAKING_QUEUE, userId.toString());
        redisTemplate.delete(MATCHMAKING_USER + userId);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void cleanupExpiredSearches() {
        Set<String> users = redisTemplate.opsForZSet().range(MATCHMAKING_QUEUE, 0, -1);
        Objects.requireNonNull(users).forEach(userId -> {
            if (Boolean.FALSE.equals(redisTemplate.hasKey(MATCHMAKING_USER + userId))) {
                redisTemplate.opsForZSet().remove(MATCHMAKING_QUEUE, userId);
            }
        });
    }
}