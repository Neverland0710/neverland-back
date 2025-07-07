package projcet.neverland.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/neverland-uploads/images/");
        // 유품 이미지 경로 추가
        registry.addResourceHandler("/keeps/**")
                .addResourceLocations("file:///C:/neverland-uploads/keeps/");
    }
}

