package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserId(UUID userId);
    List<UserAchievement> findByAchievementId(UUID achievementId);
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ua FROM UserAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId AND ua.achievement.id = :achievementId")
    Optional<UserAchievement> findByUserIdAndAchievementIdForUpdate(
            @Param("userId") UUID userId,
            @Param("achievementId") UUID achievementId
    );
}

