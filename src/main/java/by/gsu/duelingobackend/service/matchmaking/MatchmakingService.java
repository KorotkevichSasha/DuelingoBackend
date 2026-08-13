package by.gsu.duelingobackend.service.matchmaking;

import by.gsu.duelingobackend.dto.event.DuelFoundEvent;
import by.gsu.duelingobackend.dto.event.MatchmakingFailedEvent;
import by.gsu.duelingobackend.dto.response.DuelResponse;
import by.gsu.duelingobackend.dto.response.MatchmakingEstimateResponse;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.service.DuelService;
import by.gsu.duelingobackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchmakingService {

    private static final String MATCHMAKING_QUEUE = "matchmaking:queue:";
    private static final String MATCHMAKING_USER = "matchmaking:user:";
    private static final String MATCHMAKING_STARTED = "matchmaking:started:";
    private static final String MATCHMAKING_STATS = "matchmaking:stats:";
    private static final int INITIAL_RANGE = 50;
    private static final int MAX_RANGE = 300;
    private static final int RANGE_STEP = 50;
    private static final long SEARCH_TIMEOUT_MINUTES = 5;
    private static final long VIRTUAL_OPPONENT_AFTER_MS = 20_000L;
    private static final List<String> VIRTUAL_OPPONENTS = List.of(
            "FastLearner", "WordSmith", "SyntaxStar", "VocabViking", "PhrasePhantom",
            "GrammarNinja", "WordWarrior", "QuizKing", "DuelMaster"
    );

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final DuelService duelService;
    private final UserRepository userRepository;
    private final Map<UUID, CompletableFuture<Void>> searchTasks = new ConcurrentHashMap<>();

    public void joinMatchmakingQueue(User user, String requestedDifficulty) {
        log.info("User with id {} trying to join matchmaking", user.getId());
        UUID userId = user.getId();
        int userPoints = user.getPoints();
        QuestionDifficulty difficulty;
        try {
            difficulty = QuestionDifficulty.valueOf(
                    requestedDifficulty == null ? "MEDIUM" : requestedDifficulty.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            difficulty = QuestionDifficulty.MEDIUM;
        }
        redisTemplate.opsForZSet().add(queueFor(difficulty), userId.toString(), userPoints);
        redisTemplate.opsForValue().set(
                MATCHMAKING_USER + userId,
                userPoints + ":" + difficulty.name(),
                SEARCH_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(
                MATCHMAKING_STARTED + userId,
                String.valueOf(System.currentTimeMillis()),
                SEARCH_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
        );

        scheduleMatchmakingAttempt(userId, userPoints, INITIAL_RANGE, difficulty, 0);
    }

    public void cancelMatchmaking(UUID userId) {
        cleanupUserFromQueue(userId);
    }

    public MatchmakingEstimateResponse getEstimate(String requestedDifficulty) {
        QuestionDifficulty difficulty = parseDifficulty(requestedDifficulty);
        Long waiting = redisTemplate.opsForZSet().size(queueFor(difficulty));
        Object totalValue = redisTemplate.opsForHash().get(MATCHMAKING_STATS + difficulty.name(), "totalMs");
        Object countValue = redisTemplate.opsForHash().get(MATCHMAKING_STATS + difficulty.name(), "matches");
        long count = parseLong(countValue);
        long historicalSeconds = count > 0 ? Math.max(3, parseLong(totalValue) / count / 1000) : defaultEstimate(difficulty);
        long estimate = waiting != null && waiting > 0 ? Math.min(5, historicalSeconds) : historicalSeconds;
        return new MatchmakingEstimateResponse(difficulty.name(), estimate, waiting == null ? 0 : waiting);
    }

    private void scheduleMatchmakingAttempt(UUID userId, int elo, int range,
            QuestionDifficulty difficulty, long delaySeconds) {
        CompletableFuture<Void> task = CompletableFuture.runAsync(
                () -> runMatchmakingAttempt(userId, elo, range, difficulty),
                CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS)
        ).exceptionally(error -> {
            log.error("Matchmaking attempt failed for user {}", userId, error);
            notifyMatchmakingFailed(userId);
            cleanupUserFromQueue(userId);
            return null;
        });
        searchTasks.put(userId, task);
    }

    private void runMatchmakingAttempt(UUID userId, int elo, int range, QuestionDifficulty difficulty) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(MATCHMAKING_USER + userId))) return;
        Optional<UUID> opponent = findSuitableOpponent(userId, elo, range, difficulty);
        if (opponent.isPresent()) {
            createDuelAndNotify(userId, opponent.get(), difficulty);
            return;
        }
        String started = redisTemplate.opsForValue().get(MATCHMAKING_STARTED + userId);
        if (started != null && System.currentTimeMillis() - Long.parseLong(started) >= VIRTUAL_OPPONENT_AFTER_MS) {
            createDuelWithVirtualOpponent(userId, difficulty);
            return;
        }
        if (range >= MAX_RANGE) {
            notifyMatchmakingFailed(userId);
            cleanupUserFromQueue(userId);
            return;
        }
        scheduleMatchmakingAttempt(userId, elo, range + RANGE_STEP, difficulty, 5);
    }

    private Optional<UUID> findSuitableOpponent(UUID userId, int initialElo, int range, QuestionDifficulty difficulty) {
        Set<String> candidates = redisTemplate.opsForZSet().rangeByScore(
                queueFor(difficulty),
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
        return points != null ? Integer.parseInt(points.split(":", 2)[0]) : 0;
    }

    public DuelFoundEvent createDuelAndNotify(UUID player1Id, UUID player2Id, QuestionDifficulty difficulty) {
        return createDuelAndNotify(player1Id, player2Id, difficulty, false);
    }

    public DuelFoundEvent createDuelAndNotify(
            UUID player1Id,
            UUID player2Id,
            QuestionDifficulty difficulty,
            boolean friendChallenge
    ) {
        int questionCount = difficulty == QuestionDifficulty.EASY ? 15 : 10;
        long duration = switch (difficulty) {
            case EASY -> 180_000L;
            case MEDIUM -> 120_000L;
            case HARD -> 75_000L;
        };
        DuelResponse duel = duelService.createDuel(player1Id, player2Id, difficulty, questionCount);
        recordWaitTime(player1Id, difficulty);
        recordWaitTime(player2Id, difficulty);
        cleanupUserFromQueue(player1Id);
        cleanupUserFromQueue(player2Id);

        User player1 = userRepository.findById(player1Id).get();
        User player2 = userRepository.findById(player2Id).get();

        DuelFoundEvent player1Event = new DuelFoundEvent(
                duel, player2.getId(), difficulty.name(), duration, friendChallenge);
        DuelFoundEvent player2Event = new DuelFoundEvent(
                duel, player1.getId(), difficulty.name(), duration, friendChallenge);
        messagingTemplate.convertAndSendToUser(
                player1.getUsername(),
                "/queue/duel-found",
                player1Event
        );

        messagingTemplate.convertAndSendToUser(
                player2.getUsername(),
                "/queue/duel-found",
                player2Event
        );
        return player2Event;
    }

    private void notifyMatchmakingFailed(UUID userId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/matchmaking-failed",
                new MatchmakingFailedEvent("No suitable opponent found")
        );
    }

    private void cleanupUserFromQueue(UUID userId) {
        for (QuestionDifficulty difficulty : QuestionDifficulty.values()) {
            redisTemplate.opsForZSet().remove(queueFor(difficulty), userId.toString());
        }
        redisTemplate.delete(MATCHMAKING_USER + userId);
        redisTemplate.delete(MATCHMAKING_STARTED + userId);
        CompletableFuture<Void> task = searchTasks.remove(userId);
        if (task != null && !task.isDone()) task.cancel(false);
    }

    private void createDuelWithVirtualOpponent(UUID playerId, QuestionDifficulty difficulty) {
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new IllegalStateException("Player disappeared during matchmaking"));
        User opponent = VIRTUAL_OPPONENTS.stream()
                .map(userRepository::findByUsername)
                .flatMap(Optional::stream)
                .filter(candidate -> !candidate.getId().equals(playerId))
                .min(Comparator.comparingInt(candidate -> Math.abs(candidate.getPoints() - player.getPoints())))
                .orElseGet(() -> createVirtualProfile(difficulty, player.getPoints()));

        int questionCount = difficulty == QuestionDifficulty.EASY ? 15 : 10;
        long duration = durationFor(difficulty);
        DuelResponse duel = duelService.createDuel(playerId, opponent.getId(), difficulty, questionCount);
        duelService.submitSimulatedResult(duel.id(), opponent.getId(), difficulty);
        recordWaitTime(playerId, difficulty);
        cleanupUserFromQueue(playerId);
        messagingTemplate.convertAndSendToUser(
                player.getUsername(), "/queue/duel-found",
                new DuelFoundEvent(duel, opponent.getId(), difficulty.name(), duration, false)
        );
    }

    private User createVirtualProfile(QuestionDifficulty difficulty, int points) {
        String username = switch (difficulty) {
            case EASY -> "MiaSpark";
            case MEDIUM -> "AlexNova";
            case HARD -> "WordPilot";
        };
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) return existing.get();
        User profile = User.builder()
                .username(username)
                .email(username.toLowerCase() + "@players.duelrush.app")
                .password("$2a$10$U3tGZ.IsUQwf4D4v4Z.4QO2e2jJZsAM2q3k7pB5vQ7bB6s1YzYdW2")
                .role(Role.USER)
                .points(Math.max(0, points))
                .avatarUrl("default:" + (difficulty.ordinal() + 4))
                .build();
        try {
            return userRepository.saveAndFlush(profile);
        } catch (DataIntegrityViolationException race) {
            return userRepository.findByUsername(username).orElseThrow(() -> race);
        }
    }

    private long durationFor(QuestionDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 180_000L;
            case MEDIUM -> 120_000L;
            case HARD -> 75_000L;
        };
    }

    private String queueFor(QuestionDifficulty difficulty) {
        return MATCHMAKING_QUEUE + difficulty.name().toLowerCase();
    }

    private QuestionDifficulty parseDifficulty(String requestedDifficulty) {
        try {
            return QuestionDifficulty.valueOf(requestedDifficulty == null ? "MEDIUM" : requestedDifficulty.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return QuestionDifficulty.MEDIUM;
        }
    }

    private void recordWaitTime(UUID userId, QuestionDifficulty difficulty) {
        String value = redisTemplate.opsForValue().get(MATCHMAKING_STARTED + userId);
        if (value == null) return;
        long elapsed = Math.max(0, System.currentTimeMillis() - Long.parseLong(value));
        redisTemplate.opsForHash().increment(MATCHMAKING_STATS + difficulty.name(), "totalMs", elapsed);
        redisTemplate.opsForHash().increment(MATCHMAKING_STATS + difficulty.name(), "matches", 1L);
    }

    private long parseLong(Object value) {
        if (value == null) return 0;
        try { return Long.parseLong(value.toString()); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private long defaultEstimate(QuestionDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 20;
            case MEDIUM -> 15;
            case HARD -> 25;
        };
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void cleanupExpiredSearches() {
        for (QuestionDifficulty difficulty : QuestionDifficulty.values()) {
            Set<String> users = redisTemplate.opsForZSet().range(queueFor(difficulty), 0, -1);
            Objects.requireNonNull(users).forEach(userId -> {
                if (Boolean.FALSE.equals(redisTemplate.hasKey(MATCHMAKING_USER + userId))) {
                    redisTemplate.opsForZSet().remove(queueFor(difficulty), userId);
                }
            });
        }
    }
}
