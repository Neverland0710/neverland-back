package projcet.neverland.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryDeleteRequestDto {
    private String item_id;
    private String item_type; // "photo", "keepsake", "letter"
    private String user_id;
}