package projcet.neverland.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthKeyResponseDto {
    private String authKeyId;
    private String userId;
    private String deceasedId;
}