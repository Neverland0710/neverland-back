package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import projcet.neverland.dto.AuthKeyResponseDto;
import projcet.neverland.entity.AuthKey;
import projcet.neverland.repository.AuthKeyRepository;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthKeyService {

    private final AuthKeyRepository authKeyRepository;

    public Optional<AuthKeyResponseDto> getAuthKeyInfo(String authCode) {
        String trimmedCode = authCode.trim();
        log.info("🔍 조회 요청된 인증코드: '{}'", trimmedCode);

        return authKeyRepository.findByAuthCode(trimmedCode)
                .map(authKey -> {
                    log.info("✅ 인증코드 일치: {}", authKey.getAuthKeyId());
                    return new AuthKeyResponseDto(
                            authKey.getAuthKeyId(),
                            authKey.getUserId(),
                            authKey.getDeceasedId()
                    );
                });
    }
}