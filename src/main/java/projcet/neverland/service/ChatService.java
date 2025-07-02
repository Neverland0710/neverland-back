package projcet.neverland.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;

    public ChatService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://192.168.219.44:8000") // ✅ 실제 FastAPI 주소
                .build();
    }

    public Mono<Map> sendChatRequest(String authKeyId, String userId, String userInput) {
        Map<String, Object> requestBody = Map.of(
                "auth_key_id", authKeyId,
                "user_id", userId,
                "user_input", userInput,
                "context", ""
        );

        return webClient.post()
                .uri("/api/chat/generate") // ✅ 새로운 엔드포인트
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);  // ✅ 응답 전체를 JSON Map으로 받음
    }
}
