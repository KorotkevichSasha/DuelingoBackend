package by.gsu.duelingobackend.repository;

import by.gsu.duelingobackend.model.Achievement;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.AchievementLevel;
import by.gsu.duelingobackend.model.enums.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findByTypeAndLevel(AchievementType type, AchievementLevel level);
    List<Achievement> findByConditionType(AchievementConditionType conditionType);
}
