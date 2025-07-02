package projcet.neverland.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class VectorSyncService {

    private final WebClient webClient;

    public VectorSyncService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://192.168.0.123:8000")  // ✅ FastAPI IP 맞게 수정!
                .build();
    }

    public Mono<Void> deleteVector(String type, String id) {
        String vectorId = type + ":" + id;

        return webClient.post()
                .uri("/vector/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("id", vectorId))
                .retrieve()
                .bodyToMono(Void.class);
    }
}
