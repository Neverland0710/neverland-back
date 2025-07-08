package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import projcet.neverland.dto.ChatMessageDto;
import projcet.neverland.entity.TextConversation;
import projcet.neverland.repository.TextConversationRepository;
import projcet.neverland.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final WebClient fastapiWebClient;
    private final UserRepository userRepository;
    private final TextConversationRepository textConversationRepository;

    public Mono<Map> sendChatRequest(String authKeyId, String userId, String userInput) {
        Map<String, Object> requestBody = Map.of(
                "authKeyId", authKeyId,       // ✅ 카멜케이스 그대로 유지
                "user_id", userId,
                "user_input", userInput
        );

        return fastapiWebClient.post()
                .uri("/api/chat/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(error -> {
                            System.err.println("🔥 FastAPI 500 에러 내용: " + error);
                            return Mono.error(new RuntimeException("FastAPI Internal Error: " + error));
                        })
                )
                .bodyToMono(Map.class);
    }

    public String getRelationByUserId(String userId) {
        return userRepository.findRelationToDeceased(userId);
    }

    // Pagination 적용된 대화 기록 조회
    public List<ChatMessageDto> getChatHistoryAsDto(String authKeyId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sentAt"));
        return textConversationRepository.findByAuthKeyIdOrderBySentAtDesc(authKeyId, pageable)
                .getContent()
                .stream()
                .map(conv -> ChatMessageDto.builder()
                        .sender(conv.getSender().toString())
                        .message(conv.getMessage())
                        .sentAt(conv.getSentAt())
                        .authKeyId(conv.getAuthKeyId())
                        .build())
                .toList();
    }
}
