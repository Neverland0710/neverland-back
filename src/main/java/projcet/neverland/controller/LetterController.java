package projcet.neverland.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projcet.neverland.dto.LetterDto;
import projcet.neverland.entity.Letter;
import projcet.neverland.service.LetterService;
import projcet.neverland.service.VectorSyncService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/letter")
public class LetterController {

    private final LetterService letterService;
    private final VectorSyncService vectorSyncService;

    @PostMapping("/send")
    public Mono<ResponseEntity<String>> sendLetter(@RequestBody LetterDto dto) {
        return letterService.sendLetterAndReply(dto)
                .thenReturn(ResponseEntity.ok("편지와 답장이 저장되었습니다"));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Letter>> getLetters(@RequestParam String authKeyId) {
        return ResponseEntity.ok(letterService.getLettersByAuthKey(authKeyId));
    }

    @DeleteMapping("/delete/{letterId}")
    public ResponseEntity<?> deleteLetter(@PathVariable String letterId, @RequestParam String userId) {
        try {
            letterService.deleteLetter(letterId); // 편지 삭제 서비스 호출
            vectorSyncService.deleteMemory(letterId, "letter", userId).subscribe(); // 메모리 삭제
            return ResponseEntity.ok("편지 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage()); // 예외 처리
        }
    }
    @GetMapping("/relation")
    public ResponseEntity<Map<String, String>> getRelationByUserId(@RequestParam String userId) {
        String relation = letterService.getRelationByUserId(userId);
        return ResponseEntity.ok(Map.of("relation", relation));
    }
}
