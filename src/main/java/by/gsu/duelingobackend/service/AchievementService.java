package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.response.UserAchievementResponse;
import by.gsu.duelingobackend.exceptions.EntityNotFoundException;
import by.gsu.duelingobackend.mapper.UserAchievementMapper;
import by.gsu.duelingobackend.model.Achievement;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.UserAchievement;
import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.AchievementLevel;
import by.gsu.duelingobackend.model.enums.AchievementType;
import by.gsu.duelingobackend.repository.AchievementRepository;
import by.gsu.duelingobackend.repository.UserAchievementRepository;
import by.gsu.duelingobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static by.gsu.duelingobackend.util.Constants.USER_NOT_FOUND_BY_ID_ERR_MSG;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserAchievementMapper userAchievementMapper;
    private final UserRepository userRepository;

    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    public List<Achievement> getAchievementsByTypeAndLevel(AchievementType type, AchievementLevel level) {
        return achievementRepository.findByTypeAndLevel(type, level);
    }

    public List<Achievement> getAchievementsByConditionType(AchievementConditionType conditionType) {
        return achievementRepository.findByConditionType(conditionType);
    }

    public List<UserAchievementResponse> getUserAchievements(UUID userId) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);

        Map<UUID, UserAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua));

        return allAchievements.stream()
                .map(achievement -> {
                    UserAchievement ua = userAchievementMap.get(achievement.getId());

                    int currentValue = ua != null ? ua.getCurrentValue() : 0;
                    boolean isAchieved = ua != null && ua.isAchieved();

                    return new UserAchievementResponse(
                            achievement.getId(),
                            achievement.getTitle(),
                            achievement.getDescription(),
                            achievement.getType(),
                            achievement.getLevel(),
                            achievement.getRequiredValue(),
                            currentValue,
                            isAchieved,
                            achievement.getIconUrl()
                    );
                })
                .toList();
    }

    @Transactional
    public void updateProgress(UUID userId, AchievementConditionType conditionType, int increment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID_ERR_MSG, userId)));

        List<Achievement> achievements = achievementRepository.findByConditionType(conditionType);

        for (Achievement achievement : achievements) {
            UserAchievement ua = userAchievementRepository.findByUserIdAndAchievementId(userId, achievement.getId())
                    .orElse(null);

            if (ua == null) {
                ua = UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .currentValue(0)
                        .isAchieved(false)
                        .build();
            }

            if (!ua.isAchieved()) {
                ua.setCurrentValue(ua.getCurrentValue() + increment);

                if (ua.getCurrentValue() >= achievement.getRequiredValue()) {
                    ua.setAchieved(true);
                    ua.setAchievedAt(LocalDateTime.now());
                }

                userAchievementRepository.save(ua);
            }
        }
    }

}

