package projcet.neverland.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "keepsake_TB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Keepsake {
    @Id
    @Column(name = "KEEPSAKE_ID", nullable = false)
    private String keepsakeId;

    @Column(name = "AUTH_KEY_ID", nullable = false)
    private String authKeyId;

    @Column(name = "ITEM_NAME", nullable = false)
    private String itemName;

    @Column(name = "ACQUISITION_PERIOD")
    private String acquisitionPeriod;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SPECIAL_STORY")
    private String specialStory;

    @Column(name = "ESTIMATED_VALUE")
    private Long estimatedValue;

    @Column(name = "IMAGE_PATH")
    private String imagePath;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now();
}
