package projcet.neverland.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import projcet.neverland.service.ChatService;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map>> askChat(@RequestBody Map<String, String> request) {
        String authKeyId = request.get("auth_key_id");
        String userId = request.get("user_id");
        String userInput = request.get("user_input");

        return chatService.sendChatRequest(authKeyId, userId, userInput)
                .map(ResponseEntity::ok)
                .doOnNext(res -> System.out.println("📥 FastAPI 응답: " + res));
    }
}
