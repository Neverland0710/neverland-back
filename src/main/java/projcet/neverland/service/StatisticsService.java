package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projcet.neverland.entity.Statistics;
import projcet.neverland.entity.AuthKey;
import projcet.neverland.repository.AuthKeyRepository;
import projcet.neverland.repository.KeepsakeRepository;
import projcet.neverland.repository.PhotoAlbumRepository;
import projcet.neverland.repository.StatisticsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final AuthKeyRepository authKeyRepository;
    private final KeepsakeRepository keepsakeRepository;
    private final PhotoAlbumRepository photoAlbumRepository;

    public Statistics getStatistics(String userId) {
        return statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("통계 정보 없음"));
    }

    // DB 기준으로 유품 개수 다시 계산
    public void recalculateKeepsakeCount(String userId) {
        List<String> authKeyIds = authKeyRepository.findByUserId(userId)
                .stream().map(AuthKey::getAuthKeyId).toList();

        int count = keepsakeRepository.countByAuthKeyIdIn(authKeyIds);

        Statistics stat = statisticsRepository.findByUserId(userId)
                .orElseGet(() -> initializeStatistics(userId));

        stat.setKeepsakeCount(count);
        stat.setLastUpdated(LocalDateTime.now());
        statisticsRepository.save(stat);
    }

    // DB 기준으로 사진 개수 다시 계산
    public void recalculatePhotoCount(String userId) {
        List<String> authKeyIds = authKeyRepository.findByUserId(userId)
                .stream().map(AuthKey::getAuthKeyId).toList();

        int count = photoAlbumRepository.countByAuthKeyIdIn(authKeyIds);

        Statistics stat = statisticsRepository.findByUserId(userId)
                .orElseGet(() -> initializeStatistics(userId));

        stat.setPhotoCount(count);
        stat.setLastUpdated(LocalDateTime.now());
        statisticsRepository.save(stat);
    }

    // 통계 초기화용
    private Statistics initializeStatistics(String userId) {
        Statistics newStat = Statistics.builder()
                .statId(UUID.randomUUID().toString())
                .userId(userId)
                .photoCount(0)
                .sentLetterCount(0)
                .keepsakeCount(0)
                .totalConversations(0)
                .lastUpdated(LocalDateTime.now())
                .build();
        return statisticsRepository.save(newStat);
    }
}
