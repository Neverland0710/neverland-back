package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import projcet.neverland.entity.User;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // 🔹 소셜 로그인용: provider + socialId 로 사용자 조회
    Optional<User> findBySocialProviderAndSocialId(String provider, String socialId);

    // 🔹 userId로 고인과의 관계(relation_to_deceased) 조회
    @Query("SELECT u.relationToDeceased FROM User u WHERE u.userId = :userId")
    String findRelationToDeceased(@Param("userId") String userId);

    // 🔹 userId 기준으로 전체 유저 조회
    Optional<User> findByUserId(String userId);
}
