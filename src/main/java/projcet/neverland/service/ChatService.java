package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final WebClient fastapiWebClient; // ✅ 전역으로 주입받음

    public Mono<Map> sendChatRequest(String authKeyId, String userId, String userInput) {
        Map<String, Object> requestBody = Map.of(
                "auth_key_id", authKeyId,
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
}
