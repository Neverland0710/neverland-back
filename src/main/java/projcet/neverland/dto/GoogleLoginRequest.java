package projcet.neverland.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String provider;
    private String accessToken;
}
