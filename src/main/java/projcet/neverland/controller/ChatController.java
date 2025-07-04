package projcet.neverland.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projcet.neverland.dto.ChatMessageDto;
import projcet.neverland.entity.TextConversation;
import projcet.neverland.service.ChatService;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    // ✅ 기존 FastAPI 대화 연동 API (그대로 유지)
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map>> askChat(@RequestBody Map<String, String> request) {
        String authKeyId = request.get("authKeyId");
        String userId = request.get("user_id");
        String userInput = request.get("user_input");

        return chatService.sendChatRequest(authKeyId, userId, userInput)
                .map(ResponseEntity::ok)
                .doOnNext(res -> System.out.println("📥 FastAPI 응답: " + res));
    }

    // ✅ 새로 추가된: 유저 ID로 고인 관계 조회
    @GetMapping("/relation")
    public ResponseEntity<Map<String, String>> getRelation(@RequestParam String userId) {
        String relation = chatService.getRelationByUserId(userId);
        return ResponseEntity.ok(Map.of("relation", relation));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(@RequestParam String authKeyId) {
        List<ChatMessageDto> result = chatService.getChatHistoryAsDto(authKeyId);
        return ResponseEntity.ok(result);
    }
}