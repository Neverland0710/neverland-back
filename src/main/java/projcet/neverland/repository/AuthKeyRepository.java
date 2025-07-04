package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projcet.neverland.entity.AuthKey;

import java.util.List;
import java.util.Optional;

public interface AuthKeyRepository extends JpaRepository<AuthKey, String> {

    // ✅ auth_key_id 기준 조회
    Optional<AuthKey> findByAuthKeyId(String authKeyId);

    // ✅ user_id 기준 인증키 전체 조회 (통계 갱신용)
    List<AuthKey> findByUserId(String userId);

    // ✅ auth_code 기준 조회 (신규 추가)
    Optional<AuthKey> findByAuthCode(String authCode);


}
