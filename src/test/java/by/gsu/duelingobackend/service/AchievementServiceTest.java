package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.model.Achievement;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserAchievement;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.AchievementLevel;
import by.gsu.duelingobackend.model.enums.AchievementType;
import by.gsu.duelingobackend.repository.AchievementRepository;
import by.gsu.duelingobackend.repository.UserAchievementRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock private AchievementRepository achievements;
    @Mock private UserAchievementRepository userAchievements;
    @Mock private UserRepository users;

    @Test
    void claimsAchievementGoldOnlyOnce() {
        UUID userId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").gold(100).build();
        Achievement achievement = Achievement.builder()
                .id(achievementId)
                .title("First teammate")
                .description("Invite your first teammate")
                .type(AchievementType.INVITES)
                .level(AchievementLevel.BRONZE)
                .conditionType(AchievementConditionType.FRIEND_INVITED)
                .requiredValue(1)
                .rewardGold(20)
                .build();
        UserAchievement progress = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .currentValue(1)
                .isAchieved(true)
                .build();
        when(userAchievements.findByUserIdAndAchievementIdForUpdate(userId, achievementId))
                .thenReturn(Optional.of(progress));
        when(users.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        AchievementService service = new AchievementService(achievements, userAchievements, users);

        var first = service.claimReward(userId, achievementId);
        var repeated = service.claimReward(userId, achievementId);

        assertThat(first.claimedGold()).isEqualTo(20);
        assertThat(first.totalGold()).isEqualTo(120);
        assertThat(first.achievement().rewardClaimed()).isTrue();
        assertThat(repeated.claimedGold()).isZero();
        assertThat(repeated.totalGold()).isEqualTo(120);
        assertThat(user.getGold()).isEqualTo(120);
    }
}
