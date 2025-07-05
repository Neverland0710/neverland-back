package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
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

    // FastAPI 챗 요청
    public Mono<Map> sendChatRequest(String authKeyId, String userId, String userInput) {
        Map<String, Object> requestBody = Map.of(
                "authKeyId", authKeyId,
                "user_id", userId,
                "user_input", userInput
        );

        return fastapiWebClient.post()
                .uri("/api/chat/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);
    }

    // 유저 ID로 고인과의 관계 조회
    public String getRelationByUserId(String userId) {
        return userRepository.findRelationToDeceased(userId);
    }

    // 대화 기록 조회 (Entity 버전 - 내부용)
    public List<TextConversation> getChatHistory(String authKeyId) {
        return textConversationRepository.findByAuthKeyIdOrderBySentAtAsc(authKeyId);
    }

    // 대화 기록 DTO 버전 (Flutter 응답용)
    public List<ChatMessageDto> getChatHistoryAsDto(String authKeyId) {
        return textConversationRepository.findByAuthKeyIdOrderBySentAtAsc(authKeyId)
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
