package by.gsu.duelingobackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserWordProgressId.class)
@Table(name = "user_word_progress",
        indexes = {
                @Index(name = "idx_progress_review", columnList = "next_review_date"),
                @Index(name = "idx_progress_user", columnList = "user_id")
        })
public class UserWordProgress {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "word_id")
    private UUID wordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", insertable = false, updatable = false)
    private UserWord word;

    @Builder.Default
    @Column(nullable = false)
    private Integer repetitions = 0;

    @Builder.Default
    @Column(name = "easiness_factor", nullable = false)
    private Double easinessFactor = 2.5;

    @Builder.Default
    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 1;

    @Builder.Default
    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate = LocalDate.now();

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

}
