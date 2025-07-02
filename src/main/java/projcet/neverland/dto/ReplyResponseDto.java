package projcet.neverland.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyResponseDto {
    private String status;
    private String message;
    private String timestamp;
    private String response;         // ✅ 답장 내용
    private String summary_stored;
    private String rag_result;
}