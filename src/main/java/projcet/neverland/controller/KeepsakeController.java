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
import projcet.neverland.service.StatisticsService;
import projcet.neverland.service.VectorSyncService;

import java.io.File;
import java.nio.file.*;
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

    private final String uploadDir = "C:/neverland-uploads/keeps";
    private final String urlPrefix = "/keeps/";

    @PostMapping("/upload")
    @Operation(summary = "유품 등록", description = "유품 정보 및 이미지를 등록합니다.")
    public ResponseEntity<?> uploadKeepsake(
            @RequestParam("auth_key_id") String authKeyId,
            @RequestParam("item_name") String itemName,
            @RequestParam(value = "acquisition_period", required = false) String acquisitionPeriod,
            @RequestParam("description") String description,
            @RequestParam(value = "special_story", required = false) String specialStory,
            @RequestParam(value = "estimated_value", required = false) Long estimatedValue,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            String filePath = null;
            if (file != null && !file.isEmpty()) {
                String uuid = UUID.randomUUID().toString();
                String fileName = uuid + "_" + file.getOriginalFilename();
                Path fullPath = Paths.get(uploadDir, fileName);
                Files.createDirectories(fullPath.getParent());
                Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
                filePath = fullPath.toString();
            }

            Keepsake keepsake = new Keepsake(
                    UUID.randomUUID().toString(),
                    authKeyId,
                    itemName,
                    acquisitionPeriod,
                    description,
                    specialStory,
                    estimatedValue,
                    filePath,
                    LocalDateTime.now()
            );
            keepsakeRepository.save(keepsake);

            // ✅ 통계 연동
            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey -> {
                statisticsService.recalculateKeepsakeCount(authKey.getUserId());
            });

            // ✅ FastAPI 연동 - 벡터 등록
            keepsakeMemorySyncService.registerKeepsake(keepsake.getKeepsakeId(), authKeyId).subscribe();

            return ResponseEntity.ok("✅ 유품 등록 성공");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "유품 목록 조회", description = "auth_key_id 기준 유품 리스트를 정렬 기준과 함께 반환합니다.")
    public ResponseEntity<List<KeepsakeDto>> getKeepsakeList(
            @RequestParam("auth_key_id") String authKeyId,
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
            if (k.getImagePath() != null) {
                String fileName = Paths.get(k.getImagePath()).getFileName().toString();
                dto.setImagePath(urlPrefix + fileName);
            }
            dto.setCreatedAt(k.getCreatedAt().toString().substring(0, 10));
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "유품 삭제", description = "imagePath(URL) 기준으로 유품 DB 레코드 및 파일 삭제")
    public ResponseEntity<?> deleteKeepsake(@RequestParam("imageUrl") String imageUrl) {
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            File file = new File(uploadDir + "/" + filename);
            if (file.exists()) file.delete();

            Optional<Keepsake> keepsakeOpt = keepsakeRepository.findByImagePathContaining(filename);
            if (keepsakeOpt.isPresent()) {
                Keepsake keepsake = keepsakeOpt.get();
                keepsakeRepository.delete(keepsake);

                // ✅ 사용자 ID 조회 후 FastAPI 벡터 삭제 및 통계 감소
                authKeyRepository.findByAuthKeyId(keepsake.getAuthKeyId()).ifPresent(authKey -> {
                    String userId = authKey.getUserId();
                    vectorSyncService.deleteMemory(
                            keepsake.getKeepsakeId(),
                            "keepsake",
                            userId
                    ).subscribe();

                    statisticsService.recalculateKeepsakeCount(userId);
                });

                return ResponseEntity.ok("✅ 유품 삭제 완료");
            } else {
                return ResponseEntity.status(404).body("❌ 해당 유품 DB 레코드 없음");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 삭제 중 예외 발생: " + e.getMessage());
        }
    }

}
