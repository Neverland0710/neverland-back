package projcet.neverland.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projcet.neverland.dto.KeepsakeDto;
import projcet.neverland.entity.Keepsake;
import projcet.neverland.repository.AuthKeyRepository;
import projcet.neverland.repository.KeepsakeRepository;
import projcet.neverland.service.KeepsakeMemorySyncService;
import projcet.neverland.service.S3Service;
import projcet.neverland.service.StatisticsService;
import projcet.neverland.service.VectorSyncService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/keepsake")
@Tag(name = "🎁 유품 관리", description = "유품 업로드, 삭제, 목록 조회, 벡터 연동 API")
public class KeepsakeController {

    private final KeepsakeRepository keepsakeRepository;
    private final AuthKeyRepository authKeyRepository;
    private final StatisticsService statisticsService;
    private final VectorSyncService vectorSyncService;
    private final KeepsakeMemorySyncService keepsakeMemorySyncService;
    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "🎁 유품 등록", description = "유품 정보 및 이미지를 S3에 업로드하고 벡터DB에 등록합니다.")
    public ResponseEntity<?> uploadKeepsake(
            @RequestParam("authKeyId") String authKeyId,
            @RequestParam("item_name") String itemName,
            @RequestParam(value = "acquisition_period", required = false) String acquisitionPeriod,
            @RequestParam("description") String description,
            @RequestParam(value = "special_story", required = false) String specialStory,
            @RequestParam(value = "estimated_value", required = false) Long estimatedValue,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            System.out.println("✅ 유품 업로드 요청 도착");
            String imageUrl = null;

            if (file != null && !file.isEmpty()) {
                imageUrl = s3Service.uploadFile(file, "keepsakes");
                System.out.println("✅ S3 업로드 URL: " + imageUrl);
            }

            Keepsake keepsake = new Keepsake(
                    UUID.randomUUID().toString(),
                    authKeyId,
                    itemName,
                    acquisitionPeriod,
                    description,
                    specialStory,
                    estimatedValue,
                    imageUrl,
                    LocalDateTime.now()
            );

            keepsakeRepository.save(keepsake);
            System.out.println("✅ DB 저장 완료");

            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey ->
                    statisticsService.recalculateKeepsakeCount(authKey.getUserId()));

            keepsakeMemorySyncService.registerKeepsake(keepsake.getKeepsakeId(), authKeyId).subscribe();

            return ResponseEntity.ok(Map.of(
                    "message", "유품 등록 성공",
                    "imageUrl", imageUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "📄 유품 목록 조회", description = "authKeyId 기준 유품 리스트를 정렬 기준과 함께 반환합니다.")
    public ResponseEntity<List<KeepsakeDto>> getKeepsakeList(
            @RequestParam("authKeyId") String authKeyId,
            @RequestParam(value = "sort", defaultValue = "latest") String sort
    ) {
        List<Keepsake> keepsakes;
        switch (sort) {
            case "oldest":
                keepsakes = keepsakeRepository.findByAuthKeyIdOrderByCreatedAtAsc(authKeyId);
                break;
            case "name":
                keepsakes = keepsakeRepository.findByAuthKeyIdOrderByItemNameAsc(authKeyId);
                break;
            case "latest":
            default:
                keepsakes = keepsakeRepository.findByAuthKeyIdOrderByCreatedAtDesc(authKeyId);
                break;
        }

        List<KeepsakeDto> result = keepsakes.stream().map(k -> {
            KeepsakeDto dto = new KeepsakeDto();
            BeanUtils.copyProperties(k, dto);
            dto.setImagePath(k.getImagePath());
            dto.setCreatedAt(k.getCreatedAt().toString().substring(0, 10));
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "🗑️ 유품 삭제",
            description = "authKeyId와 imageUrl을 쿼리 파라미터로 받아, S3, DB, 벡터DB에서 유품 정보 삭제합니다.\n\n" +
                    "예시: /keepsake/delete?authKeyId=xxx&imageUrl=https://s3.../image.jpg"
    )
    public ResponseEntity<?> deleteKeepsake(
            @RequestParam("authKeyId") String authKeyId,
            @RequestParam("imageUrl") String imageUrl
    ) {
        try {
            if (authKeyId == null || authKeyId.isBlank() || imageUrl == null || imageUrl.isBlank()) {
                return ResponseEntity.badRequest().body("authKeyId와 imageUrl은 필수입니다.");
            }

            Optional<Keepsake> keepsakeOpt = keepsakeRepository.findByAuthKeyIdAndImagePath(authKeyId, imageUrl);
            if (keepsakeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유품을 찾을 수 없습니다.");
            }

            Keepsake keepsake = keepsakeOpt.get();

            // S3 이미지 삭제
            if (keepsake.getImagePath() != null) {
                s3Service.deleteFile(keepsake.getImagePath());
            }

            // DB 삭제
            keepsakeRepository.delete(keepsake);

            // 통계, 벡터DB 연동
            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey -> {
                String userId = authKey.getUserId();
                statisticsService.recalculateKeepsakeCount(userId);
                vectorSyncService.deleteMemory(keepsake.getKeepsakeId(), "keepsake", userId).subscribe();
            });

            return ResponseEntity.ok("삭제 완료");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패: " + e.getMessage());
        }
    }
}
