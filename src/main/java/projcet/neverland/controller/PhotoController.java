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
import projcet.neverland.repository.AuthKeyRepository;
import projcet.neverland.repository.PhotoAlbumRepository;
import projcet.neverland.service.PhotoMemorySyncService;
import projcet.neverland.service.StatisticsService;
import projcet.neverland.service.VectorSyncService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/photo")
@Tag(name = "📷 사진 앨범", description = "사진 업로드, 삭제, 목록 조회, 벡터 연동 API")
public class PhotoController {

    private final PhotoAlbumRepository photoAlbumRepository;
    private final AuthKeyRepository authKeyRepository;
    private final StatisticsService statisticsService;
    private final VectorSyncService vectorSyncService;
    private final PhotoMemorySyncService photoMemorySyncService;

    private static final String UPLOAD_DIR = "C:/neverland-uploads/images/";

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "📤 사진 업로드", description = "사진 파일과 정보를 업로드하고 벡터DB에 등록합니다.")
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
                    .fileFormat(file.getContentType() != null ? file.getContentType() : "unknown")
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            photoAlbumRepository.save(photo);

            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey ->
                    statisticsService.recalculatePhotoCount(authKey.getUserId()));

            photoMemorySyncService.registerPhoto(photo.getPhotoId(), authKeyId).subscribe();

            return ResponseEntity.ok("✅ 업로드 성공");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 업로드 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "🗑️ 사진 삭제", description = "이미지 경로를 기준으로 DB, 파일, 벡터DB에서 삭제합니다.")
    public ResponseEntity<?> deletePhoto(@RequestParam("imageUrl") String imageUrl) {
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            File file = new File(UPLOAD_DIR + filename);
            if (file.exists()) file.delete();

            Optional<PhotoAlbum> target = photoAlbumRepository.findByImagePathContaining(filename);
            if (target.isPresent()) {
                PhotoAlbum photo = target.get();
                photoAlbumRepository.delete(photo);

                authKeyRepository.findByAuthKeyId(photo.getAuthKeyId()).ifPresent(authKey -> {
                    String userId = authKey.getUserId();
                    vectorSyncService.deleteMemory(photo.getPhotoId(), "photo", userId).subscribe();
                    statisticsService.recalculatePhotoCount(userId);
                });

                return ResponseEntity.ok("✅ 삭제 완료");
            } else {
                return ResponseEntity.status(404).body("❌ 해당 사진 없음");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "📄 사진 목록 조회", description = "auth_key_id 기준으로 업로드된 사진 목록을 반환합니다.")
    public ResponseEntity<List<PhotoAlbum>> getPhotoList(@RequestParam("auth_key_id") String authKeyId) {
        return ResponseEntity.ok(photoAlbumRepository.findByAuthKeyId(authKeyId));
    }
}
