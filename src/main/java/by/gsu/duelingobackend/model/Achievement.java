package by.gsu.duelingobackend.model;

import by.gsu.duelingobackend.model.enums.AchievementConditionType;
import by.gsu.duelingobackend.model.enums.AchievementLevel;
import by.gsu.duelingobackend.model.enums.AchievementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "achievements",
        indexes = {
                @Index(name = "idx_achievements_title", columnList = "title"),
                @Index(name = "idx_achievements_condition_type", columnList = "condition_type")
        }
)
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters long")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters long")
    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AchievementType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private AchievementLevel level;

    @Column(name = "required_value", nullable = false)
    private Integer requiredValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private AchievementConditionType conditionType;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}