package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projcet.neverland.entity.AuthKey;
import projcet.neverland.entity.Letter;

import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, String> {
    List<Letter> findByAuthKeyOrderByCreatedAtDesc(AuthKey authKey); // ✅ 객체 기반 조회
}
