package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projcet.neverland.entity.Keepsake;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeepsakeRepository extends JpaRepository<Keepsake, String> {
    List<Keepsake> findByAuthKeyIdOrderByCreatedAtDesc(String authKeyId);
    Optional<Keepsake> findByImagePathContaining(String filename);
    List<Keepsake> findByAuthKeyIdOrderByCreatedAtAsc(String authKeyId);
    List<Keepsake> findByAuthKeyIdOrderByItemNameAsc(String authKeyId);
    int countByAuthKeyIdIn(List<String> authKeyIds);
    Optional<Keepsake> findByImagePath(String imagePath);
}

