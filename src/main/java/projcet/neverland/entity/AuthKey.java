package projcet.neverland.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_key_TB")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthKey {

    @Id
    @Column(name = "AUTH_KEY_ID", length = 36)
    private String authKeyId;

    @Column(name = "USER_ID", nullable = false, length = 36)
    private String userId;

    @Column(name = "DECEASED_ID", nullable = false, length = 36)
    private String deceasedId;

    @Column(name = "AUTH_CODE", nullable = false, unique = true, length = 20)
    private String authCode;

    @Column(name = "IS_VALID", nullable = false)
    private boolean isValid;

    @Column(name = "ISSUED_AT", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "EXPIRED_AT")
    private LocalDateTime expiredAt;
}
