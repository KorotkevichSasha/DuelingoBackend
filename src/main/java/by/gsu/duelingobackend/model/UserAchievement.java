package by.gsu.duelingobackend.model;

import jakarta.persistence.*;
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
        name = "user_achievements",
        indexes = {
                @Index(name = "idx_user_achievements_user_id", columnList = "user_id"),
                @Index(name = "idx_user_achievements_achievement_id", columnList = "achievement_id")
        }
)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "current_value", nullable = false)
    private int currentValue;

    @Column(name = "is_achieved", nullable = false)
    private boolean isAchieved;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;
}

