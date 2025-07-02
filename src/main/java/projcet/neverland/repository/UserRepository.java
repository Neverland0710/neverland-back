package projcet.neverland.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projcet.neverland.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findBySocialProviderAndSocialId(String provider, String socialId);
}