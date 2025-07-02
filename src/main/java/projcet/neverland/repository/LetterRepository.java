package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projcet.neverland.entity.Letter;

import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, String> {
    List<Letter> findByAuthKeyIdOrderByCreatedAtDesc(String authKeyId);
}
