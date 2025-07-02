package projcet.neverland.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_TB")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "USER_ID")
    @Builder.Default
    private String userId = UUID.randomUUID().toString();

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "SOCIAL_PROVIDER", nullable = false)
    private String socialProvider;

    @Column(name = "SOCIAL_ID", nullable = false)
    private String socialId;

    @Column(name = "JOINED_AT")
    private LocalDateTime joinedAt;

    @Column(name = "RELATION_TO_DECEASED")
    private String relationToDeceased;
}
