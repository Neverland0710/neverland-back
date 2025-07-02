package projcet.neverland.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projcet.neverland.entity.PhotoAlbum;
import projcet.neverland.repository.PhotoAlbumRepository;
import projcet.neverland.repository.AuthKeyRepository;
import projcet.neverland.service.StatisticsService;
import projcet.neverland.service.VectorSyncService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/photo")
@Tag(name = "📷 Photo 앨범", description = "사진 업로드 및 목록 조회 API")
public class PhotoController {

    private final PhotoAlbumRepository photoAlbumRepository;
    private final AuthKeyRepository authKeyRepository;
    private final StatisticsService statisticsService;
    private final VectorSyncService vectorSyncService;

    private static final String UPLOAD_DIR = "C:/neverland-uploads/images/";

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사진 업로드", description = "사진 1장을 업로드하고 제목, 설명, 날짜 정보를 저장합니다.")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("auth_key_id") String authKeyId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("photo_date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate photoDate,
            @RequestPart("file") MultipartFile file
    ) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String imagePath = "/images/" + filename;

        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) directory.mkdirs();

            file.transferTo(new File(UPLOAD_DIR + filename));
        } catch (Exception e) {
            imagePath = null;
        }

        try {
            PhotoAlbum photo = PhotoAlbum.builder()
                    .photoId(UUID.randomUUID().toString())
                    .authKeyId(authKeyId)
                    .title(title)
                    .description(description)
                    .photoDate(photoDate)
                    .imagePath(imagePath != null ? imagePath : "FILE_SAVE_FAILED")
                    .fileFormat(file.getContentType() != null ? file.getContentType() : "unknown") // ✅ 수정됨
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            photoAlbumRepository.save(photo);

            // ✅ 통계 연동
            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey -> {
                statisticsService.recalculatePhotoCount(authKey.getUserId());
            });

            return ResponseEntity.ok("✅ 업로드 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "사진 목록 조회", description = "auth_key_id로 등록된 모든 사진 목록을 조회합니다.")
    public ResponseEntity<?> getPhotosByAuthKeyId(@RequestParam("auth_key_id") String authKeyId) {
        List<PhotoAlbum> photos = photoAlbumRepository.findByAuthKeyId(authKeyId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (PhotoAlbum p : photos) {
            Map<String, Object> map = new HashMap<>();
            map.put("photoId", p.getPhotoId());
            map.put("title", p.getTitle());
            map.put("description", p.getDescription());
            map.put("date", p.getPhotoDate().toString());
            map.put("imageUrl", p.getImagePath());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "사진 삭제", description = "imageUrl 경로로 사진 파일과 DB 레코드를 삭제합니다.")
    public ResponseEntity<?> deletePhoto(@RequestParam("imageUrl") String imageUrl) {
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            File file = new File(UPLOAD_DIR + filename);
            if (file.exists()) file.delete();

            Optional<PhotoAlbum> target = photoAlbumRepository.findByImagePathContaining(filename);
            if (target.isPresent()) {
                PhotoAlbum photo = target.get();
                photoAlbumRepository.delete(photo);

                // ✅ FastAPI에 벡터 삭제 알림
                vectorSyncService.deleteVector("photo", photo.getPhotoId()).subscribe();

                // ✅ 통계 감소 처리
                authKeyRepository.findByAuthKeyId(photo.getAuthKeyId()).ifPresent(authKey -> {
                    statisticsService.recalculatePhotoCount(authKey.getUserId());
                });

                return ResponseEntity.ok("✅ 삭제 완료");
            } else {
                return ResponseEntity.status(404).body("❌ 해당 사진 없음");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}
