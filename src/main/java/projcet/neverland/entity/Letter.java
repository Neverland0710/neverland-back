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
public class Letter {

    @Id
    private String letterId;

    @Column(nullable = false)
    private String authKeyId;

    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    private LocalDateTime createdAt;

    public enum DeliveryStatus {
        DRAFT, SENT, DELIVERED
    }

    @PrePersist
    public void onCreate() {
        this.letterId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
}
