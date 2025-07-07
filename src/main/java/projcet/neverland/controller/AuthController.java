package projcet.neverland.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projcet.neverland.JWT.JwtTokenProvider;
import projcet.neverland.entity.User;
import projcet.neverland.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/social-login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("🟣 [/auth/social-login] 요청 도착");

        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.error("Authorization 헤더가 없거나 잘못된 형식입니다: {}", authorizationHeader);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(" Authorization 헤더 오류");
            }

            String idToken = authorizationHeader.replace("Bearer ", "").trim();
            log.info("받은 ID Token (앞부분): {}", idToken.length() > 30 ? idToken.substring(0, 30) + "..." : idToken);

            // Firebase ID Token 검증
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = token.getUid();
            String email = token.getEmail();
            String name = token.getName();

            log.info("Firebase 인증 성공: uid={}, email={}, name={}", uid, email, name);

            // DB 조회 or 저장
            User user = userRepository.findBySocialProviderAndSocialId("GOOGLE", uid)
                    .orElseGet(() -> {
                        log.info("신규 사용자, DB 저장");
                        return userRepository.save(User.builder()
                                .socialProvider("GOOGLE")
                                .socialId(uid)
                                .email(email)
                                .name(name != null ? name : "unknown")
                                .joinedAt(LocalDateTime.now())
                                .build());
                    });

            // JWT 발급
            String jwt = jwtTokenProvider.createToken(user.getUserId());

            Map<String, String> response = new HashMap<>();
            response.put("access_token", jwt);
            response.put("user_id", user.getUserId());

            log.info("JWT 발급 완료: {}", jwt.substring(0, 20) + "...");

            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            log.error("Firebase ID Token 검증 실패: {}, code: {}", e.getMessage(), e.getAuthErrorCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Firebase ID Token 검증 실패: " + e.getMessage());
        } catch (Exception ex) {
            log.error("예외 발생", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류: " + ex.getMessage());
        }
    }
}
