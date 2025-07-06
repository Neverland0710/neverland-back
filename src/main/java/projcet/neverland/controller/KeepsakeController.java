package projcet.neverland.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
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
public class KeepsakeController {

    private final KeepsakeRepository keepsakeRepository;
    private final AuthKeyRepository authKeyRepository;
    private final StatisticsService statisticsService;
    private final VectorSyncService vectorSyncService;
    private final KeepsakeMemorySyncService keepsakeMemorySyncService;
    private final S3Service s3Service;

    @PostMapping("/upload")
    @Operation(summary = "유품 등록", description = "유품 정보 및 이미지를 S3에 등록합니다.")
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
            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                // S3에 파일 업로드
                imageUrl = s3Service.uploadFile(file, "keepsakes");
            }

            Keepsake keepsake = new Keepsake(
                    UUID.randomUUID().toString(),
                    authKeyId,
                    itemName,
                    acquisitionPeriod,
                    description,
                    specialStory,
                    estimatedValue,
                    imageUrl, // S3 URL 저장
                    LocalDateTime.now()
            );
            keepsakeRepository.save(keepsake);

            // 통계 연동
            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey -> {
                statisticsService.recalculateKeepsakeCount(authKey.getUserId());
            });

            // FastAPI 연동 - 벡터 등록
            keepsakeMemorySyncService.registerKeepsake(keepsake.getKeepsakeId(), authKeyId).subscribe();

            return ResponseEntity.ok("유품 등록 성공");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "유품 목록 조회", description = "authKeyId 기준 유품 리스트를 정렬 기준과 함께 반환합니다.")
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
            // S3 URL을 그대로 사용
            dto.setImagePath(k.getImagePath());
            dto.setCreatedAt(k.getCreatedAt().toString().substring(0, 10));
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "유품 삭제", description = "imagePath(S3 URL) 기준으로 유품 DB 레코드 및 S3 파일 삭제")
    public ResponseEntity<?> deleteKeepsake(@RequestParam("imageUrl") String imageUrl) {
        try {
            // DB에서 유품 찾기
            Optional<Keepsake> keepsakeOpt = keepsakeRepository.findByImagePath(imageUrl);

            if (keepsakeOpt.isPresent()) {
                Keepsake keepsake = keepsakeOpt.get();

                // S3에서 파일 삭제
                if (keepsake.getImagePath() != null) {
                    s3Service.deleteFile(keepsake.getImagePath());
                }

                // DB에서 삭제
                keepsakeRepository.delete(keepsake);

                // 사용자 ID 조회 후 FastAPI 벡터 삭제 및 통계 감소
                authKeyRepository.findByAuthKeyId(keepsake.getAuthKeyId()).ifPresent(authKey -> {
                    String userId = authKey.getUserId();
                    vectorSyncService.deleteMemory(
                            keepsake.getKeepsakeId(),
                            "keepsake",
                            userId
                    ).subscribe();

                    statisticsService.recalculateKeepsakeCount(userId);
                });

                return ResponseEntity.ok("유품 삭제 완료");
            } else {
                return ResponseEntity.status(404).body("해당 유품 DB 레코드 없음");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 중 예외 발생: " + e.getMessage());
        }
    }
}