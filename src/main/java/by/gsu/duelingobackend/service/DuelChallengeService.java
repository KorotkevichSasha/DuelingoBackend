package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.event.DuelChallengeEvent;
import by.gsu.duelingobackend.dto.event.DuelFoundEvent;
import by.gsu.duelingobackend.dto.request.DuelChallengeRequest;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.model.enums.RelationshipStatus;
import by.gsu.duelingobackend.repository.UserRelationshipRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.service.matchmaking.MatchmakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DuelChallengeService {
    private static final String CHALLENGE = "duel:challenge:";
    private static final String INCOMING = "duel:challenge:incoming:";
    private static final long TTL_SECONDS = 120;

    private final StringRedisTemplate redis;
    private final UserRepository users;
    private final UserRelationshipRepository relationships;
    private final MatchmakingService matchmaking;
    private final SimpMessagingTemplate messaging;

    public DuelChallengeEvent create(UUID challengerId, DuelChallengeRequest request) {
        User challenger = user(challengerId);
        User friend = user(request.friendId());
        if (relationships.findBetweenUsersWithStatus(challengerId, friend.getId(), RelationshipStatus.FRIEND).isEmpty()) {
            throw new InvalidOperationException("You can challenge only a friend");
        }
        QuestionDifficulty difficulty = parse(request.difficulty());
        UUID id = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(TTL_SECONDS);
        redis.opsForValue().set(CHALLENGE + id,
                challengerId + "|" + friend.getId() + "|" + difficulty.name() + "|" + expiresAt.toEpochMilli(),
                TTL_SECONDS, TimeUnit.SECONDS);
        redis.opsForSet().add(INCOMING + friend.getId(), id.toString());
        redis.expire(INCOMING + friend.getId(), TTL_SECONDS, TimeUnit.SECONDS);
        DuelChallengeEvent event = new DuelChallengeEvent(id, challengerId, challenger.getUsername(), difficulty.name(), expiresAt);
        messaging.convertAndSendToUser(friend.getUsername(), "/queue/duel-challenge", event);
        return event;
    }

    public List<DuelChallengeEvent> pending(UUID targetId) {
        List<DuelChallengeEvent> result = new ArrayList<>();
        var ids = redis.opsForSet().members(INCOMING + targetId);
        if (ids == null) return result;
        for (String id : ids) {
            read(UUID.fromString(id), targetId).ifPresentOrElse(result::add,
                    () -> redis.opsForSet().remove(INCOMING + targetId, id));
        }
        return result;
    }

    public DuelFoundEvent respond(UUID targetId, UUID challengeId, boolean accept) {
        PendingChallenge challenge = readRaw(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge expired"));
        if (!challenge.targetId().equals(targetId)) throw new InvalidOperationException("This challenge is not yours");
        redis.delete(CHALLENGE + challengeId);
        redis.opsForSet().remove(INCOMING + targetId, challengeId.toString());
        if (accept) return matchmaking.createDuelAndNotify(
                challenge.challengerId(), targetId, challenge.difficulty(), true);
        return null;
    }

    private java.util.Optional<DuelChallengeEvent> read(UUID id, UUID targetId) {
        return readRaw(id).filter(c -> c.targetId().equals(targetId)).map(c ->
                new DuelChallengeEvent(id, c.challengerId(), user(c.challengerId()).getUsername(),
                        c.difficulty().name(), c.expiresAt()));
    }

    private java.util.Optional<PendingChallenge> readRaw(UUID id) {
        String value = redis.opsForValue().get(CHALLENGE + id);
        if (value == null) return java.util.Optional.empty();
        String[] parts = value.split("\\|", 4);
        if (parts.length != 4) return java.util.Optional.empty();
        return java.util.Optional.of(new PendingChallenge(UUID.fromString(parts[0]), UUID.fromString(parts[1]),
                parse(parts[2]), Instant.ofEpochMilli(Long.parseLong(parts[3]))));
    }

    private User user(UUID id) { return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")); }
    private QuestionDifficulty parse(String value) {
        try { return QuestionDifficulty.valueOf(value == null ? "MEDIUM" : value.toUpperCase()); }
        catch (IllegalArgumentException ignored) { return QuestionDifficulty.MEDIUM; }
    }
    private record PendingChallenge(UUID challengerId, UUID targetId, QuestionDifficulty difficulty, Instant expiresAt) {}
}
