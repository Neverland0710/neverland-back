package projcet.neverland.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase_adminsdk.json");

            if (serviceAccount == null) {
                System.err.println("❌ firebase_adminsdk.json 파일을 찾을 수 없습니다.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase Admin SDK 초기화 완료");
            }

        } catch (Exception e) {
            System.err.println("❌ Firebase 초기화 실패: " + e.getMessage());
        }
    }
}
