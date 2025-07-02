package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import projcet.neverland.dto.LetterDto;
import projcet.neverland.entity.Letter;
import projcet.neverland.entity.Letter.DeliveryStatus;
import projcet.neverland.repository.LetterRepository;
import projcet.neverland.repository.StatisticsRepository;
import projcet.neverland.entity.Statistics;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final StatisticsRepository statisticsRepository;

    public void sendLetterAndReply(LetterDto dto) {
        // 1. 편지 저장 (SENT)
        Letter letter = Letter.builder()
                .authKeyId(dto.getAuthKeyId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .deliveryStatus(DeliveryStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
        letterRepository.save(letter);

        // 2. FastAPI 요청 → 답장 생성
        String reply = requestReplyFromLLM(dto.getContent());

        // 3. 답장 저장 (DELIVERED)
        Letter replyLetter = Letter.builder()
                .authKeyId(dto.getAuthKeyId())
                .title("답장: " + dto.getTitle())
                .content(reply)
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .createdAt(LocalDateTime.now())
                .build();
        letterRepository.save(replyLetter);

        // 4. 통계 증가
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
    }

    private String requestReplyFromLLM(String message) {
        String url = "http://localhost:8000/letter/reply"; // FastAPI 주소

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("message", message);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();
    }

    public List<Letter> getLettersByAuthKey(String authKeyId) {
        return letterRepository.findByAuthKeyIdOrderByCreatedAtDesc(authKeyId);
    }

    // ✅ 추가된 삭제 기능
    public void deleteLetter(String letterId) {
        letterRepository.deleteById(letterId);
    }
}
