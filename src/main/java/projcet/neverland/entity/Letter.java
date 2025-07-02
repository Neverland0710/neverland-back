package projcet.neverland.entity;

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
@Table(name = "letter_TB")
public class Letter {

    @Id
    @Column(name = "LETTER_ID")
    private String letterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTH_KEY_ID", nullable = false)
    private AuthKey authKey; // ✅ 객체 참조로 변경

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "REPLY_CONTENT", columnDefinition = "TEXT")
    private String replyContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "DELIVERY_STATUS")
    private DeliveryStatus deliveryStatus;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public enum DeliveryStatus {
        DRAFT, SENT, DELIVERED
    }

    @PrePersist
    public void onCreate() {
        if (this.letterId == null) this.letterId = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
