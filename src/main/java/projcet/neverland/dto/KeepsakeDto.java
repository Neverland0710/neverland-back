package projcet.neverland.dto;

import lombok.Data;

@Data
public class KeepsakeDto {
    private String keepsakeId;
    private String itemName;
    private String acquisitionPeriod;
    private String description;
    private String specialStory;
    private Long estimatedValue;
    private String imagePath;
    private String createdAt;
}
