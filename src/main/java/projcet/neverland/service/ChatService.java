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

    public String getRelationByUserId(String userId) {
        return userRepository.findRelationToDeceased(userId);
    }

    // Pagination 적용된 대화 기록 조회
    public List<ChatMessageDto> getChatHistoryAsDto(String authKeyId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
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
