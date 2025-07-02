package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeepsakeMemorySyncService {

    private final WebClient fastapiWebClient;

    public Mono<Void> registerKeepsake(String keepsakeId, String authKeyId) {
        Map<String, String> request = Map.of(
                "keepsake_id", keepsakeId,
                "auth_key_id", authKeyId
        );

        return fastapiWebClient.post()
                .uri("/api/keepsake/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
