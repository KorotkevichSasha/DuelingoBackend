package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.user.UserInLeaderboardResponse;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @Mock
    private UserRepository userRepository;

    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        service = new LeaderboardService(redisTemplate, userRepository);
    }

    @Test
    void usersWithEqualPointsReceiveTheSameRank() {
        UUID userId = UUID.randomUUID();
        when(zSetOperations.score("leaderboard", userId.toString())).thenReturn(420.0);
        when(zSetOperations.count("leaderboard", Math.nextUp(420.0), Double.POSITIVE_INFINITY))
                .thenReturn(3L);

        assertThat(service.getUserRank(userId)).isEqualTo(4L);
    }

    @Test
    void reportsTheRealPointGapToTheNextHigherScore() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("learner").points(420).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(zSetOperations.score("leaderboard", userId.toString())).thenReturn(420.0);
        when(zSetOperations.count("leaderboard", Math.nextUp(420.0), Double.POSITIVE_INFINITY))
                .thenReturn(3L);
        when(zSetOperations.rangeByScoreWithScores(
                "leaderboard", Math.nextUp(420.0), Double.POSITIVE_INFINITY, 0, 1
        )).thenReturn(Set.of(new DefaultTypedTuple<>("next-user", 437.0)));

        UserInLeaderboardResponse response = service.getUserInLeaderboard(userId);

        assertThat(response.rank()).isEqualTo(4L);
        assertThat(response.pointsToNextRank()).isEqualTo(17);
    }
}
