package projcet.neverland.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AudioWebSocketHandler extends TextWebSocketHandler {

    private final WebClient fastapiWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("✅ WebSocket 연결 완료: {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("📩 handleTextMessage() 호출됨 - session: {}, payload: {}", session.getId(), message.getPayload());

        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.path("type").asText();

            switch (type) {
                case "auth":
                    String authKeyIdFromPayload = json.path("authKeyId").asText();
                    if (authKeyIdFromPayload != null && !authKeyIdFromPayload.isEmpty()) {
                        session.getAttributes().put("authKeyId", authKeyIdFromPayload);
                        log.info("✅ 인증 성공 - authKeyId: {}", authKeyIdFromPayload);
                    } else {
                        log.warn("❌ auth 메시지에 authKeyId 없음");
                    }
                    break;

                case "connect":
                    log.info("🔗 연결 초기화 메시지 수신");
                    break;

                case "ping":
                    log.info("📡 ping 수신");
                    break;

                case "transcription":
                case "user_message":
                    String userText = json.path("text").asText();
                    log.info("📝 수신된 텍스트: {}", userText);

                    String authKeyId = (String) session.getAttributes().get("authKeyId");
                    if (authKeyId == null || authKeyId.isEmpty()) {
                        log.error("❌ 인증되지 않은 세션입니다. authKeyId 없음. 연결 종료");
                        session.close();
                        return;
                    }

                    callVoiceProcessEndpoint(session, authKeyId, userText);
                    break;

                default:
                    log.warn("❓ 알 수 없는 메시지 타입: {}", type);
            }

        } catch (Exception e) {
            log.error("❌ JSON 파싱 에러", e);
        }
    }

    private void callVoiceProcessEndpoint(WebSocketSession session, String authKeyId, String userText) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("authKeyId", authKeyId);  // ✅ FastAPI가 요구하는 대소문자 정확히 일치
        formData.add("user_text", userText);

        fastapiWebClient.post()
                .uri("/api/voice/process")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.parseMediaType("audio/mpeg"))
                .bodyValue(formData)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        String encodedText = response.headers().asHttpHeaders().getFirst("X-AI-Response");

                        Mono<byte[]> audioMono = response.bodyToFlux(DataBuffer.class)
                                .reduce(new ByteArrayOutputStream(), (out, buffer) -> {
                                    try {
                                        byte[] bytes = new byte[buffer.readableByteCount()];
                                        buffer.read(bytes);
                                        out.write(bytes);
                                    } catch (IOException e) {
                                        log.error("❌ Byte 쓰기 실패", e);
                                    } finally {
                                        org.springframework.core.io.buffer.DataBufferUtils.release(buffer);
                                    }
                                    return out;
                                })
                                .map(ByteArrayOutputStream::toByteArray);

                        return audioMono.flatMap(audioBytes -> {
                            try {
                                if (encodedText != null) {
                                    String decodedText = URLDecoder.decode(encodedText, StandardCharsets.UTF_8);
                                    session.sendMessage(new TextMessage(decodedText));
                                    log.info("📨 텍스트 응답 전송: {}", decodedText);
                                }

                                session.sendMessage(new BinaryMessage(audioBytes));
                                log.info("📤 오디오 전송: {} bytes", audioBytes.length);

                            } catch (IOException e) {
                                log.error("❌ WebSocket 전송 실패", e);
                            }

                            return Mono.empty();
                        });

                    } else {
                        log.error("🔥 FastAPI 응답 실패: {}", response.statusCode());
                        return Mono.empty();
                    }
                })
                .doOnError(error -> log.error("❌ FastAPI 통신 에러", error))
                .subscribe();
    }

}