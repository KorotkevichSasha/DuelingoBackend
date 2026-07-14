package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserId(UUID userId);
    List<UserAchievement> findByAchievementId(UUID achievementId);
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);
}

