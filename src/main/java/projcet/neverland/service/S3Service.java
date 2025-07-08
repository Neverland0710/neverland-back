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

    // ✅ 폴더명 없는 기본 업로드
    public String uploadFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    // ✅ 폴더명 지정 가능한 업로드
    public String uploadFile(MultipartFile file, String folderName) {
        String fileName = folderName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    // ✅ URL 전체에서 S3 키 추출 후 삭제
    public void deleteFile(String imageUrl) {
        try {
            // URL에서 key 부분 추출
            String baseUrl = amazonS3.getUrl(bucketName, "").toString(); // ex: https://bucket-name.s3.../
            String key = imageUrl.replace(baseUrl, "");

            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }
}
