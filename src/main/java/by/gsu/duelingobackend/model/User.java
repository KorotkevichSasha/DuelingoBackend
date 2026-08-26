package by.gsu.duelingobackend.model;

import by.gsu.duelingobackend.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_points", columnList = "points DESC"),
                @Index(name = "idx_users_username_trgm", columnList = "username"),
        }
)
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 5, max = 50, message = "Username must be between 5 and 50 characters long")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Email address must be in the format user@example.com")
    @Size(min = 5, max = 255, message = "Email address must be between 5 and 255 characters long")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "role", nullable = false)
    private Role role;

    @Min(0)
    @Builder.Default
    @Column(name = "points", nullable = false)
    private Integer points = 0;

    @Min(0)
    @Builder.Default
    @Column(name = "gold", nullable = false)
    private Integer gold = 100;

    @Min(0)
    @Builder.Default
    @Column(name = "rush_charges", nullable = false)
    private Integer rushCharges = 10;

    @Builder.Default
    @Column(name = "rush_charges_updated_at", nullable = false)
    private LocalDateTime rushChargesUpdatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "virtual_player", nullable = false)
    private boolean virtualPlayer = false;

    @Min(0)
    @Builder.Default
    @Column(name = "highest_league_rewarded", nullable = false)
    private Integer highestLeagueRewarded = 0;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = true;

    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Column(name = "last_daily_tip_reward_at")
    private LocalDate lastDailyTipRewardAt;

    @Column(name = "listening_reward_date")
    private LocalDate listeningRewardDate;

    @Builder.Default
    @Column(name = "listening_gold_today", nullable = false)
    private Integer listeningGoldToday = 0;

    @OneToMany(mappedBy = "user")
    private List<UserTestProgress> progress;

}
