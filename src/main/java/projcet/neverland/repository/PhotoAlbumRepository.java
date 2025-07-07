package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projcet.neverland.entity.PhotoAlbum;

import java.util.List;
import java.util.Optional;

public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, String> {
    List<PhotoAlbum> findByAuthKeyId(String authKeyId);
    Optional<PhotoAlbum> findByImagePath(String imagePath);

    int countByAuthKeyIdIn(List<String> authKeyIds);

    Optional<PhotoAlbum> findByImagePathContaining(String filename);
}
