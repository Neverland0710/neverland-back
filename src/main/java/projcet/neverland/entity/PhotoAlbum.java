package projcet.neverland.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photo_album_TB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoAlbum {

    @Id
    @Column(name = "photo_id")
    private String photoId;

    @Column(name = "AUTH_KEY_ID", nullable = false)
    private String authKeyId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_date")
    private LocalDate photoDate;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "file_format")
    private String fileFormat;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
