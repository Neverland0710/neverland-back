package projcet.neverland.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projcet.neverland.dto.ChatMessageDto;
import projcet.neverland.service.ChatService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    // FastAPI 대화 연동
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map>> askChat(@RequestBody Map<String, String> request) {
        String authKeyId = request.get("authKeyId");
        String userId = request.get("user_id");
        String userInput = request.get("user_input");

        return chatService.sendChatRequest(authKeyId, userId, userInput)
                .map(ResponseEntity::ok)
                .doOnNext(res -> System.out.println("📥 FastAPI 응답: " + res));
    }

    // 고인 관계 조회
    @GetMapping("/relation")
    public ResponseEntity<Map<String, String>> getRelation(@RequestParam String userId) {
        String relation = chatService.getRelationByUserId(userId);
        return ResponseEntity.ok(Map.of("relation", relation));
    }

    // 페이징된 대화 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @RequestParam String authKeyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        List<ChatMessageDto> result = chatService.getChatHistoryAsDto(authKeyId, page, size);
        return ResponseEntity.ok(result);
    }
}
