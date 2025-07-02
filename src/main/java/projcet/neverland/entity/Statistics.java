package projcet.neverland.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "statistics_TB")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {

    @Id
    @Column(name = "STAT_ID", length = 36)
    private String statId;

    @Column(name = "USER_ID", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(name = "PHOTO_COUNT", nullable = false)
    private int photoCount;

    @Column(name = "SENT_LETTER_COUNT", nullable = false)
    private int sentLetterCount;

    @Column(name = "KEEPSAKE_COUNT", nullable = false)
    private int keepsakeCount;

    @Column(name = "TOTAL_CONVERSATIONS", nullable = false)
    private int totalConversations;

    @Column(name = "LAST_UPDATED", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    public void onCreate() {
        this.statId = UUID.randomUUID().toString();
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
