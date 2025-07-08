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
import java.util.*;

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
            @RequestParam("file") MultipartFile file
    ) {
        try {
            System.out.println("✅ 업로드 요청 도착");
            System.out.println("authKeyId: " + authKeyId);
            System.out.println("title: " + title);
            System.out.println("description: " + description);
            System.out.println("photo_date: " + photoDate);
            System.out.println("file: " + file.getOriginalFilename());

            // S3 업로드
            String imageUrl = s3Service.uploadFile(file, "photos");
            System.out.println("✅ S3 업로드 URL: " + imageUrl);

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

            System.out.println("✅ DB 저장 전 photo 객체: " + photo);

            photoAlbumRepository.save(photo);
            System.out.println("✅ DB 저장 완료");

            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey ->
                    statisticsService.recalculatePhotoCount(authKey.getUserId()));

            photoMemorySyncService.registerPhoto(photo.getPhotoId(), authKeyId).subscribe();

            return ResponseEntity.ok(Map.of(
                    "message", "업로드 성공",
                    "imageUrl", imageUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
        }
    }

    // ✅ 목록 조회 API 추가
    @GetMapping("/list")
    @Operation(summary = "📄 사진 목록 조회", description = "authKeyId 기준으로 업로드된 사진 목록을 반환합니다.")
    public ResponseEntity<List<PhotoAlbum>> getPhotoList(@RequestParam("authKeyId") String authKeyId) {
        List<PhotoAlbum> photoList = photoAlbumRepository.findByAuthKeyId(authKeyId);
        return ResponseEntity.ok(photoList);
    }
    @DeleteMapping("/delete")
    @Operation(summary = "🗑️ 사진 삭제", description = "imageUrl 기준으로 S3에서 파일을 삭제하고 DB 및 벡터DB에서도 제거합니다.")
    public ResponseEntity<?> deletePhoto(
            @RequestParam("authKeyId") String authKeyId,
            @RequestParam("imageUrl") String imageUrl
    ) {
        try {
            // 📌 1. DB에서 해당 사진 찾기
            Optional<PhotoAlbum> optionalPhoto = photoAlbumRepository.findByAuthKeyIdAndImagePath(authKeyId, imageUrl);
            if (optionalPhoto.isEmpty()) {
                return ResponseEntity.status(404).body("사진을 찾을 수 없습니다.");
            }

            PhotoAlbum photo = optionalPhoto.get();

            // 📌 2. S3에서 파일 삭제
            s3Service.deleteFile(imageUrl);

            // 📌 3. DB에서 삭제
            photoAlbumRepository.delete(photo);

            // 📌 4. 통계 갱신
            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey ->
                    statisticsService.recalculatePhotoCount(authKey.getUserId()));

            // 📌 5. FastAPI 벡터에서 삭제
            authKeyRepository.findByAuthKeyId(authKeyId).ifPresent(authKey ->
                    vectorSyncService.deleteMemory(photo.getPhotoId(), "photo", authKey.getUserId()).subscribe()
            );

            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

}
