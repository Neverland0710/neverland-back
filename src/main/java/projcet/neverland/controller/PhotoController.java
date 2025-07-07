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
import projcet.neverland.service.S3Service;
import projcet.neverland.service.StatisticsService;
import projcet.neverland.service.VectorSyncService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사진 업로드", description = "사진 파일과 정보를 S3에 업로드하고 벡터DB에 등록합니다.")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("authKeyId") String authKeyId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("photo_date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate photoDate,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            // S3에 파일 업로드
            String imageUrl = s3Service.uploadFile(file, "photos");

            if (imageUrl == null) {
                return ResponseEntity.status(500).body("파일 업로드 실패");
            }

            PhotoAlbum photo = PhotoAlbum.builder()
                    .photoId(UUID.randomUUID().toString())
                    .authKeyId(authKeyId)
                    .title(title)
                    .description(description)
                    .photoDate(photoDate)
                    .imagePath(imageUrl)
                    .fileFormat(file.getContentType() != null ? file.getContentType() : "unknown")
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            photoAlbumRepository.save(photo);

            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey ->
                    statisticsService.recalculatePhotoCount(authKey.getUserId()));

            photoMemorySyncService.registerPhoto(photo.getPhotoId(), authKeyId).subscribe();

            return ResponseEntity.ok(Map.of(
                    "message", "업로드 성공",
                    "imageUrl", imageUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "🗑️ 사진 삭제", description = "이미지 경로를 기준으로 DB, S3 파일, 벡터DB에서 삭제합니다.")
    public ResponseEntity<?> deletePhoto(@RequestParam("imageUrl") String imageUrl) {
        try {
            // S3 URL에서 파일명 추출
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // DB에서 사진 찾기 (기존 메서드 사용)
            Optional<PhotoAlbum> target = photoAlbumRepository.findByImagePathContaining(filename);

            if (target.isPresent()) {
                PhotoAlbum photo = target.get();

                // S3에서 파일 삭제
                s3Service.deleteFile(imageUrl);

                // DB에서 삭제
                photoAlbumRepository.delete(photo);

                authKeyRepository.findByAuthKeyId(photo.getAuthKeyId()).ifPresent(authKey -> {
                    String userId = authKey.getUserId();
                    vectorSyncService.deleteMemory(photo.getPhotoId(), "photo", userId).subscribe();
                    statisticsService.recalculatePhotoCount(userId);
                });

                return ResponseEntity.ok("삭제 완료");
            } else {
                return ResponseEntity.status(404).body("해당 사진 없음");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "📄 사진 목록 조회", description = "authKeyId 기준으로 업로드된 사진 목록을 반환합니다.")
    public ResponseEntity<List<PhotoAlbum>> getPhotoList(@RequestParam("authKeyId") String authKeyId) {
        return ResponseEntity.ok(photoAlbumRepository.findByAuthKeyId(authKeyId));
    }
}