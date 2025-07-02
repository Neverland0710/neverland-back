package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projcet.neverland.entity.Deceased;

public interface DeceasedRepository extends JpaRepository<Deceased, String> {
    // 필요 시 추가 조회 메서드 선언 가능
}
