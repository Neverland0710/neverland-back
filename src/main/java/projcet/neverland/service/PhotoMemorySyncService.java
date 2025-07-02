package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhotoMemorySyncService {

    private final WebClient fastapiWebClient;

    public Mono<Void> registerPhoto(String photoId, String authKeyId) {
        Map<String, String> request = Map.of(
                "photo_id", photoId,
                "auth_key_id", authKeyId
        );

        return fastapiWebClient.post()
                .uri("/api/photo/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
