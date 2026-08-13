package by.gsu.duelingobackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_report")
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reporter_id")
    private User reporter;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;
    @Column(nullable = false, length = 80)
    private String reason;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
