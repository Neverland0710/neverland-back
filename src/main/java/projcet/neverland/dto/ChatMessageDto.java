package projcet.neverland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ChatMessageDto {
    private String sender;
    private String message;
    private LocalDateTime sentAt;
    private String authKeyId;
}
