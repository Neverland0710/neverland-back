package projcet.neverland.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import projcet.neverland.dto.LetterDto;
import projcet.neverland.dto.ReplyResponseDto;
import projcet.neverland.entity.AuthKey;
import projcet.neverland.entity.Letter;
import projcet.neverland.entity.Statistics;
import projcet.neverland.repository.AuthKeyRepository;
import projcet.neverland.repository.LetterRepository;
import projcet.neverland.repository.StatisticsRepository;
import projcet.neverland.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final StatisticsRepository statisticsRepository;
    private final AuthKeyRepository authKeyRepository;
    private final WebClient fastapiWebClient;
    private final UserRepository userRepository;


    public Mono<Void> sendLetterAndReply(LetterDto dto) {
        String letterId = UUID.randomUUID().toString();
        LocalDateTime createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now();

        // ✅ null 방어 처리
        if (dto.getUserId() == null || dto.getAuthKeyId() == null) {
            throw new IllegalArgumentException("❌ user_id 또는 auth_key_id가 누락되었습니다.");
        }

        AuthKey authKey = authKeyRepository.findById(dto.getAuthKeyId())
                .orElseThrow(() -> new IllegalArgumentException("❌ 유효하지 않은 인증키 ID입니다."));

        // ✅ 편지 저장
        Letter letter = Letter.builder()
                .letterId(letterId)
                .authKey(authKey)
                .title(dto.getTitle())
                .content(dto.getContent())
                .deliveryStatus(Letter.DeliveryStatus.SENT)
                .createdAt(createdAt)
                .build();
        letterRepository.save(letter);

        // ✅ FastAPI로 보낼 JSON 생성
        Map<String, Object> request = new HashMap<>();
        request.put("letter_id", letterId);
        request.put("user_id", dto.getUserId());
        request.put("authKeyId", dto.getAuthKeyId());
        request.put("letter_text", Optional.ofNullable(dto.getContent()).orElse(""));

        // ✅ 디버깅용 로그 출력
        try {
            String jsonLog = new ObjectMapper().writeValueAsString(request);
            System.out.println("📨 FastAPI로 전송할 JSON: " + jsonLog);
        } catch (Exception e) {
            System.out.println("❌ JSON 변환 실패: " + e.getMessage());
        }

        return fastapiWebClient.post()
                .uri("/api/letter/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReplyResponseDto.class)
                .map(apiResp -> {
                    String reply = apiResp.getResponse();
                    letter.setReplyContent(reply);
                    letter.setDeliveryStatus(Letter.DeliveryStatus.DELIVERED);
                    letterRepository.save(letter);

                    // ✅ 통계 업데이트
                    Statistics stat = statisticsRepository.findByUserId(dto.getUserId())
                            .orElseGet(() -> Statistics.builder()
                                    .userId(dto.getUserId())
                                    .photoCount(0)
                                    .sentLetterCount(0)
                                    .keepsakeCount(0)
                                    .totalConversations(0)
                                    .build());
                    stat.setSentLetterCount(stat.getSentLetterCount() + 1);
                    statisticsRepository.save(stat);

                    return reply;
                })
                .doOnError(e -> {
                    System.out.println("❌ FastAPI 통신 중 에러 발생: " + e.getMessage());
                })
                .then();
    }

    public List<Letter> getLettersByAuthKey(String authKeyId) {
        AuthKey authKey = authKeyRepository.findById(authKeyId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 유효하지 않은 인증키 ID입니다."));
        return letterRepository.findByAuthKeyOrderByCreatedAtDesc(authKey);
    }
    public String getRelationByUserId(String userId) {
        return userRepository.findRelationToDeceased(userId);
    }

    public void deleteLetter(String letterId) {
        letterRepository.deleteById(letterId);
    }
}
