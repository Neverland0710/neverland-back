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

@RestController
@RequiredArgsConstructor
@RequestMapping("/letter")
public class LetterController {

    private final LetterService letterService;
    private final VectorSyncService vectorSyncService;

    @PostMapping("/send")
    public Mono<ResponseEntity<String>> sendLetter(@RequestBody LetterDto dto) {
        return letterService.sendLetterAndReply(dto)
                .thenReturn(ResponseEntity.ok("✅ 편지와 답장이 저장되었습니다"));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Letter>> getLetters(@RequestParam String authKeyId) {
        return ResponseEntity.ok(letterService.getLettersByAuthKey(authKeyId));
    }

    @DeleteMapping("/delete/{letterId}")
    public ResponseEntity<?> deleteLetter(@PathVariable String letterId, @RequestParam String userId) {
        letterService.deleteLetter(letterId);
        vectorSyncService.deleteMemory(letterId, "letter", userId).subscribe();
        return ResponseEntity.ok("✅ 편지 삭제 완료");
    }
}
