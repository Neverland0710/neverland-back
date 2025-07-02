package projcet.neverland.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true); // Authorization 헤더 허용
        config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*")); // 모든 요청 헤더 허용
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 주요 HTTP 메서드 허용
        config.setExposedHeaders(Arrays.asList("*")); // 응답 헤더 전부 노출 (필요 시)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 URL에 대해 CORS 적용

        return new CorsFilter(source);
    }
}