package projcet.neverland.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    /**
     * ✅ 폴더 없는 기본 업로드
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * ✅ 폴더명 지정 업로드 (e.g., photos/)
     */
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            String originalName = file.getOriginalFilename();
            String uniqueName = UUID.randomUUID() + "_" + (originalName != null ? originalName : "unnamed");
            String key = (folderName != null && !folderName.isEmpty()) ? folderName + "/" + uniqueName : uniqueName;

            amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            return amazonS3.getUrl(bucketName, key).toString(); // ✅ S3 전체 URL 반환
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    /**
     * 🗑️ 전체 S3 URL에서 key만 추출해 삭제
     */
    public void deleteFile(String imageUrl) {
        try {
            String bucketUrl = amazonS3.getUrl(bucketName, "").toString(); // https://bucket.s3.amazonaws.com/
            String key = imageUrl.replace(bucketUrl, "");

            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }
}
