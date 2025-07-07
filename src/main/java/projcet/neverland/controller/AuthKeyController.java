package projcet.neverland.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projcet.neverland.dto.AuthKeyResponseDto;
import projcet.neverland.service.AuthKeyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "AuthKey 인증", description = "인증코드 기반 사용자-고인 연결 정보 조회 API")
public class AuthKeyController {

    private final AuthKeyService authKeyService;

    @Operation(
            summary = "인증코드로 연결 정보 조회",
            description = "인증코드를 기반으로 auth_key_id, user_id, deceased_id를 반환합니다."
    )
    @GetMapping("/lookup")
    public ResponseEntity<AuthKeyResponseDto> getAuthKeyInfo(
            @RequestParam("auth_code") String authCode
    ) {
        return authKeyService.getAuthKeyInfo(authCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }
}
