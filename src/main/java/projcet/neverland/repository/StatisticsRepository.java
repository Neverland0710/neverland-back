package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projcet.neverland.entity.Statistics;

import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, String> {
    Optional<Statistics> findByUserId(String userId);
}
