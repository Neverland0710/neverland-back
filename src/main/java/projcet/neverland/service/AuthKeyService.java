package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import projcet.neverland.dto.AuthKeyResponseDto;
import projcet.neverland.entity.AuthKey;
import projcet.neverland.repository.AuthKeyRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthKeyService {

    private final AuthKeyRepository authKeyRepository;

    public Optional<AuthKeyResponseDto> getAuthKeyInfo(String authCode) {
        return authKeyRepository.findByAuthCode(authCode)
                .map(authKey -> new AuthKeyResponseDto(
                        authKey.getAuthKeyId(),
                        authKey.getUserId(),
                        authKey.getDeceasedId()
                ));
    }
}
