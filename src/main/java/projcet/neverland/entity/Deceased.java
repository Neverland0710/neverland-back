package projcet.neverland.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "deceased_TB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deceased {

    @Id
    @Column(name = "deceased_id", nullable = false)
    private String deceasedId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "death_date")
    private LocalDate deathDate;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "speaking_style")
    private String speakingStyle;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "personality")
    private String personality;

    @Column(name = "hobbies")
    private String hobbies;

    @Column(name = "registered_at")
    private LocalDate registeredAt;

    @Column(name = "creator_user_id", nullable = false)
    private String creatorUserId; // 유족(사용자) ID
}
