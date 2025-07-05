package projcet.neverland.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import projcet.neverland.dto.MemoryDeleteRequestDto;
import reactor.core.publisher.Mono;



@Service
@RequiredArgsConstructor
public class VectorSyncService {

    private final WebClient fastapiWebClient;

    public Mono<Void> deleteMemory(String itemId, String itemType, String userId) {
        MemoryDeleteRequestDto dto = new MemoryDeleteRequestDto(itemId, itemType, userId);

        return fastapiWebClient
                .method(HttpMethod.DELETE)
                .uri("/api/admin/memory/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto) // 이제 DTO 사용
                .retrieve()
                .bodyToMono(Void.class);
    }
}
