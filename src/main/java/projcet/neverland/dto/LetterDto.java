package projcet.neverland.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LetterDto {

    @JsonProperty("auth_key_id")       // ✅ JSON의 snake_case → Java camelCase 매핑
    private String authKeyId;

    @JsonProperty("user_id")
    private String userId;

    private String title;
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
